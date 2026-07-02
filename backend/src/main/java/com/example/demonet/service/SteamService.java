package com.example.demonet.service;

import com.example.demonet.entity.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SteamService {

    private final RestClient restClient = RestClient.create();
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.steam.api-key:}")
    private String apiKey;

    public List<Item> fetchByAppIds(List<Long> appIds) {
        List<Item> items = new ArrayList<>();
        for (Long appId : appIds) {
            try {
                Item item = fetchAppDetail(appId);
                if (item != null) items.add(item);
            } catch (Exception e) {
                log.error("Steam fetch failed for appid {}: {}", appId, e.getMessage());
            }
        }
        return items;
    }

    public Item fetchAppDetail(Long appId) {
        Map<String, Object> resp = restClient.get()
                .uri("https://store.steampowered.com/api/appdetails?appids=" + appId + "&cc=cn&l=schinese")
                .retrieve()
                .body(Map.class);

        if (resp == null) return null;
        Map<String, Object> appData = (Map<String, Object>) resp.get(String.valueOf(appId));
        if (appData == null || !Boolean.TRUE.equals(appData.get("success"))) return null;
        Map<String, Object> data = (Map<String, Object>) appData.get("data");
        if (data == null) return null;

        Item item = new Item();
        String steamType = String.valueOf(data.getOrDefault("type", "game"));
        boolean isDlc = "dlc".equals(steamType);
        item.setType("game");
        item.setTitle(String.valueOf(data.getOrDefault("name", "Unknown")));
        item.setSlug("steam-" + appId);
        item.setCoverUrl(extractHeaderImage(data));
        item.setPosterUrl("https://steamcdn-a.akamaihd.net/steam/apps/" + appId + "/library_600x900.jpg");
        item.setDescription(String.valueOf(data.getOrDefault("short_description", "")));
        item.setExternalId(String.valueOf(appId));
        item.setExternalLink("https://store.steampowered.com/app/" + appId + "/");

        String developer = extractFirst(data, "developers");
        String publisher = extractFirst(data, "publishers");
        String genres = extractGenres(data);
        String platforms = buildPlatforms(data);
        boolean isFree = Boolean.TRUE.equals(data.get("is_free"));

        // Extract screenshots
        String screenshots = extractScreenshots(data);
        // Extract movies/videos
        String movies = extractMovies(data);
        // Extra details
        String releaseDate = esc(extractValue(data, "release_date", "date"));
        String supportedLanguages = esc(extractValue(data, "supported_languages", null));
        String price = extractPrice(data);
        String minRequirements = esc(extractRequirements(data, "minimum"));
        String recRequirements = esc(extractRequirements(data, "recommended"));
        String dlcStr = extractDLC(data);
        String featuresStr = extractFeatures(data);

        item.setInfoJson(String.format(
                "{\"developer\":\"%s\",\"publisher\":\"%s\",\"genre\":\"%s\",\"platform\":\"%s\"," +
                "\"demo_available\":false,\"free\":%s,\"is_dlc\":%s," +
                "\"release_date\":\"%s\",\"languages\":\"%s\",\"price\":\"%s\"," +
                "\"min_requirements\":\"%s\",\"rec_requirements\":\"%s\"," +
                "\"dlc\":%s,\"features\":%s," +
                "\"screenshots\":%s,\"videos\":%s}",
                esc(developer), esc(publisher), esc(genres), esc(platforms),
                isFree, isDlc, releaseDate, supportedLanguages, price,
                minRequirements, recRequirements,
                dlcStr, featuresStr,
                screenshots, movies));

        item.setSource("steam");
        item.setStatus(0);
        log.info("Steam: {} (id={}) cover={}", item.getTitle(), appId, item.getCoverUrl());
        return item;
    }

    public int updateExistingGames() {
        List<Map<String, Object>> games = jdbcTemplate.queryForList(
                "SELECT id, external_id FROM items WHERE type='game' AND source='manual' AND external_id IS NULL AND slug LIKE '%-%'");
        int updated = 0;
        for (Map<String, Object> row : games) {
            try {
                Long id = (Long) row.get("id");
                Long appId = Long.valueOf((String) row.get("external_id"));
                Item fresh = fetchAppDetail(appId);
                if (fresh == null) continue;
                jdbcTemplate.update(
                        "UPDATE items SET cover_url=?, description=?, info_json=?, external_link=? WHERE id=?",
                        fresh.getCoverUrl(), fresh.getDescription(), fresh.getInfoJson(), fresh.getExternalLink(), id);
                updated++;
                log.info("Updated {} with Steam data", fresh.getTitle());
            } catch (Exception e) {
                log.warn("Update failed for row {}: {}", row.get("id"), e.getMessage());
            }
        }
        return updated;
    }

    
    /** Backfill poster_url for existing games that have a Steam AppID */
    public int backfillPosterUrls() {
        List<Map<String, Object>> games = jdbcTemplate.queryForList(
                "SELECT id, external_id FROM items WHERE type='game' AND external_id IS NOT NULL AND (poster_url IS NULL OR poster_url = '')");
        int updated = 0;
        for (Map<String, Object> row : games) {
            try {
                Long id = (Long) row.get("id");
                String posterUrl = "https://steamcdn-a.akamaihd.net/steam/apps/" + row.get("external_id") + "/library_600x900.jpg";
                jdbcTemplate.update("UPDATE items SET poster_url=? WHERE id=?", posterUrl, id);
                updated++;
            } catch (Exception e) {
                log.warn("Backfill poster failed for row {}: {}", row.get("id"), e.getMessage());
            }
        }
        log.info("Backfilled poster_url for {} games", updated);
        return updated;
    }
private String extractHeaderImage(Map<String, Object> data) {
        Object img = data.get("header_image");
        if (img != null && !img.toString().isBlank()) {
            String s = img.toString();
            if (s.startsWith("http")) return s;
        }
        return "";
    }

    private String extractFirst(Map<String, Object> data, String key) {
        Object val = data.get(key);
        if (val instanceof List && !((List<?>) val).isEmpty()) {
            return String.valueOf(((List<?>) val).get(0));
        }
        return "";
    }

    private String extractGenres(Map<String, Object> data) {
        Object genres = data.get("genres");
        if (genres instanceof List) {
            List<String> names = new ArrayList<>();
            for (Object g : (List<?>) genres) {
                if (g instanceof Map) names.add(String.valueOf(((Map<?,?>) g).get("description")));
            }
            return String.join(", ", names);
        }
        return "";
    }

    private String buildPlatforms(Map<String, Object> data) {
        List<String> p = new ArrayList<>();
        if (Boolean.TRUE.equals(data.get("windows"))) p.add("PC");
        if (Boolean.TRUE.equals(data.get("mac"))) p.add("Mac");
        if (Boolean.TRUE.equals(data.get("linux"))) p.add("Linux");
        return String.join(",", p);
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String extractScreenshots(Map<String, Object> data) {
        Object raw = data.get("screenshots");
        if (!(raw instanceof List)) return "[]";
        List<String> urls = new ArrayList<>();
        for (Object s : (List<?>) raw) {
            if (s instanceof Map) {
                Object url = ((Map<?,?>) s).get("path_full");
                if (url != null) urls.add(esc(url.toString()));
            }
        }
        return "[\"" + String.join("\",\"", urls) + "\"]";
    }

    @SuppressWarnings("unchecked")
    private String extractMovies(Map<String, Object> data) {
        Object raw = data.get("movies");
        if (!(raw instanceof List)) return "{}";
        // Try mp4 → webm → construct from movie id
        for (Object m : (List<?>) raw) {
            if (m instanceof Map) {
                Map<String, Object> movie = (Map<String, Object>) m;
                // Check mp4 and webm first (older games)
                for (String fmt : new String[]{"mp4", "webm"}) {
                    Object fmtObj = movie.get(fmt);
                    if (fmtObj instanceof Map) {
                        Object max = ((Map<String, Object>) fmtObj).get("max");
                        if (max != null && !max.toString().isEmpty()) {
                            return "{\"steam\":\"" + esc(max.toString()) + "\"}";
                        }
                    }
                }
                // Construct mp4 URL from movie id (modern Steam games)
                Object movieId = movie.get("id");
                if (movieId instanceof Number) {
                    String url = "https://video.akamai.steamstatic.com/store_trailers/"
                            + ((Number) movieId).longValue() + "/movie_max.mp4";
                    return "{\"steam\":\"" + url + "\"}";
                }
            }
        }
        return "{}";
    }

    // ========== Steam detail extractors ==========

    private String extractValue(Map<String, Object> data, String parentKey, String childKey) {
        try {
            Object raw = data.get(parentKey);
            if (raw instanceof Map) {
                Object val = childKey != null ? ((Map<?,?>) raw).get(childKey) : raw;
                if (val != null) { String s = val.toString(); if (!s.isBlank() && !"null".equals(s)) return s; }
            } else if (raw instanceof String) {
                String s = (String) raw; if (!s.isBlank()) return s;
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String extractPrice(Map<String, Object> data) {
        try {
            Object raw = data.get("price_overview");
            if (raw instanceof Map) {
                Map<?,?> p = (Map<?,?>) raw;
                Object final_ = p.get("final"); Object currency = p.get("currency");
                if (final_ instanceof Number && currency != null) {
                    return String.format("%s %.2f", currency, ((Number) final_).longValue() / 100.0);
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String extractRequirements(Map<String, Object> data, String key) {
        try {
            Object raw = data.get("pc_requirements");
            if (raw instanceof Map) {
                Object req = ((Map<?,?>) raw).get(key);
                if (req != null) {
                    String s = req.toString()
                            .replaceAll("</li>", "\n")
                            .replaceAll("<br\\s*/?>", "\n")
                            .replaceAll("<li>", "")
                            .replaceAll("<strong>", "")
                            .replaceAll("</strong>", " ")
                            .replaceAll("<[^>]+>", "")
                            .replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">")
                            .replace("&#39;", "'").replace("&quot;", "\"").replace("&amp;", "&")
                            .replace("最低配置:", "").replace("推荐配置:", "")
                            .replaceAll("\n{2,}", "\n")
                            .replaceAll(" • ", "• ")
                            .trim();
                    return s;
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    @SuppressWarnings("unchecked")
    private String extractDLC(Map<String, Object> data) {
        Object raw = data.get("dlc");
        if (!(raw instanceof List)) return "[]";
        List<?> list = (List<?>) raw;
        if (list.isEmpty()) return "[]";
        
        // Take up to 10 DLCs and batch-fetch their names
        List<Long> ids = new ArrayList<>();
        for (Object o : list) {
            if (ids.size() >= 10) break;
            if (o instanceof Number) ids.add(((Number) o).longValue());
        }
        
        // Batch fetch DLC names from Steam API
        String idsParam = ids.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        try {
            Map<String, Object> dlcResp = restClient.get()
                    .uri("https://store.steampowered.com/api/appdetails?appids=" + idsParam + "&cc=cn&l=schinese")
                    .retrieve()
                    .body(Map.class);
            if (dlcResp != null) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < ids.size(); i++) {
                    if (i > 0) sb.append(",");
                    Long id = ids.get(i);
                    Map<String, Object> appData = (Map<String, Object>) dlcResp.get(String.valueOf(id));
                    String name = "";
                    if (appData != null && Boolean.TRUE.equals(appData.get("success"))) {
                        Map<String, Object> d = (Map<String, Object>) appData.get("data");
                        if (d != null) name = String.valueOf(d.getOrDefault("name", ""));
                    }
                    if (name.isEmpty()) name = "App " + id;
                    sb.append("{\"id\":").append(id).append(",\"name\":\"").append(esc(name)).append("\"}");
                }
                sb.append("]");
                return sb.toString();
            }
        } catch (Exception e) {
            log.warn("DLC batch fetch failed: {}", e.getMessage());
        }
        
        // Fallback: just IDs
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("{\"id\":").append(ids.get(i)).append(",\"name\":\"App ").append(ids.get(i)).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractFeatures(Map<String, Object> data) {
        Object raw = data.get("categories");
        if (!(raw instanceof List)) return "[]";
        List<String> names = new ArrayList<>();
        for (Object c : (List<?>) raw) {
            if (c instanceof Map) {
                String desc = (String) ((Map<String, Object>) c).get("description");
                if (desc != null && !desc.isBlank()) names.add("\"" + esc(desc) + "\"");
            }
        }
        return "[" + String.join(",", names) + "]";
    }
}
