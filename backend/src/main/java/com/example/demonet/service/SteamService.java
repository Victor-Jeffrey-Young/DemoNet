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
        item.setType("game");
        item.setTitle(String.valueOf(data.getOrDefault("name", "Unknown")));
        item.setSlug("steam-" + appId);
        item.setCoverUrl(extractHeaderImage(data));
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

        item.setInfoJson(String.format(
                "{\"developer\":\"%s\",\"publisher\":\"%s\",\"genre\":\"%s\",\"platform\":\"%s\",\"demo_available\":false,\"free\":%s,\"screenshots\":%s,\"videos\":%s}",
                esc(developer), esc(publisher), esc(genres), esc(platforms), isFree, screenshots, movies));

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
}
