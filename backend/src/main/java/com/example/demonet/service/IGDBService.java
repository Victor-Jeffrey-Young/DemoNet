package com.example.demonet.service;

import com.example.demonet.entity.Item;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@lombok.RequiredArgsConstructor
public class IGDBService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private final StringRedisTemplate redisTemplate;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Value("${app.igdb.client-id:}")
    private String clientIdFromConfig;

    @Value("${app.igdb.client-secret:}")
    private String clientSecretFromConfig;

    private String cachedClientId = null;
    private String cachedClientSecret = null;
    private long lastKeyFetchTime = 0;

    private static final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";
    private static final String API_BASE = "https://api.igdb.com/v4";

    /** 优先从 app_settings 表读取（管理后台可动态修改），回退到 .env 配置 */
    private String getClientId() {
        refreshCredentialsIfNeeded();
        return cachedClientId;
    }

    private String getClientSecret() {
        refreshCredentialsIfNeeded();
        return cachedClientSecret;
    }

    private void refreshCredentialsIfNeeded() {
        if (System.currentTimeMillis() - lastKeyFetchTime < 300000 && cachedClientId != null) {
            return;
        }
        try {
            cachedClientId = jdbcTemplate.queryForObject(
                "SELECT setting_value FROM app_settings WHERE setting_key = 'IGDB_CLIENT_ID'", String.class);
            cachedClientSecret = jdbcTemplate.queryForObject(
                "SELECT setting_value FROM app_settings WHERE setting_key = 'IGDB_CLIENT_SECRET'", String.class);
        } catch (Exception ignored) {}
        if (cachedClientId == null || cachedClientId.isBlank()) cachedClientId = clientIdFromConfig;
        if (cachedClientSecret == null || cachedClientSecret.isBlank()) cachedClientSecret = clientSecretFromConfig;
        lastKeyFetchTime = System.currentTimeMillis();
    }

    // ---- OAuth ----

    private String getAccessToken() {
        String cachedToken = redisTemplate.opsForValue().get("igdb:token");
        if (cachedToken != null && !cachedToken.isBlank()) {
            return cachedToken;
        }
        String cId = getClientId();
        String cSecret = getClientSecret();
        if (cId == null || cId.isBlank() || cSecret == null || cSecret.isBlank()) {
            log.warn("IGDB credentials missing — set IGDB_CLIENT_ID and IGDB_CLIENT_SECRET");
            return null;
        }
        try {
            Map<String, String> body = Map.of(
                    "client_id", cId,
                    "client_secret", cSecret,
                    "grant_type", "client_credentials"
            );
            Map<String, Object> resp = restClient.post()
                    .uri(TOKEN_URL)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            if (resp != null) {
                String token = String.valueOf(resp.get("access_token"));
                long expiresIn = ((Number) resp.get("expires_in")).longValue();
                redisTemplate.opsForValue().set("igdb:token", token, Math.max(expiresIn - 60, 60), TimeUnit.SECONDS);
                log.info("IGDB token obtained and cached, expires in {}s", expiresIn);
                return token;
            }
        } catch (Exception e) {
            log.error("IGDB auth failed: {}", e.getMessage());
        }
        return null;
    }

    // ---- Public API ----

    /**
     * 搜索游戏（名称模糊匹配）
     */
    public List<Item> searchGames(String query) {
        return searchGames(query, 10);
    }

    public List<Item> searchGames(String query, int limit) {
        List<Item> items = new ArrayList<>();
        String token = getAccessToken();
        if (token == null) return items;

        String escaped = query.replace("\"", "\\\"").replace("\n", " ");
        String bodyStr = String.format(
                "search \"%s\"; fields name,cover.url,first_release_date,genres.name,platforms.name,"
                        + "summary,rating,total_rating_count,involved_companies.company.name,"
                        + "involved_companies.publisher,involved_companies.developer,"
                        + "screenshots.url,videos.video_id,websites.url,similar_games.name,"
                        + "game_modes.name,themes.name,aggregated_rating; limit %d;",
                escaped, limit);

        return executeQuery(items, bodyStr, "game");
    }

    /**
     * 热门/高评分游戏
     */
    public List<Item> fetchPopularGames(int limit) {
        List<Item> items = new ArrayList<>();
        String token = getAccessToken();
        if (token == null) return items;

        String bodyStr = String.format(
                "fields name,cover.url,first_release_date,genres.name,platforms.name,"
                        + "summary,rating,total_rating_count,"
                        + "screenshots.url,videos.video_id,websites.url,"
                        + "game_modes.name,themes.name; "
                        + "where rating >= 80 & total_rating_count >= 50; "
                        + "sort rating desc; limit %d;",
                limit);

        return executeQuery(items, bodyStr, "game");
    }

    /**
     * 近一年新游戏
     */
    public List<Item> fetchRecentGames(int limit) {
        List<Item> items = new ArrayList<>();
        String token = getAccessToken();
        if (token == null) return items;

        long oneYearAgo = Instant.now().getEpochSecond() - 365L * 24 * 3600;

        String bodyStr = String.format(
                "fields name,cover.url,first_release_date,genres.name,platforms.name,"
                        + "summary,rating,total_rating_count,"
                        + "screenshots.url,videos.video_id,websites.url,"
                        + "game_modes.name,themes.name; "
                        + "where first_release_date >= %d & rating >= 70 & total_rating_count >= 10; "
                        + "sort first_release_date desc; limit %d;",
                oneYearAgo, limit);

        return executeQuery(items, bodyStr, "game");
    }

    /**
     * 指定 ID 获取游戏详情
     */
    public Item fetchGameById(int igdbId) {
        String token = getAccessToken();
        if (token == null) return null;

        String bodyStr = String.format(
                "fields name,cover.url,first_release_date,genres.name,platforms.name,"
                        + "summary,rating,total_rating_count,involved_companies.company.name,"
                        + "involved_companies.publisher,involved_companies.developer,"
                        + "screenshots.url,videos.video_id,websites.url,similar_games.name,"
                        + "game_modes.name,themes.name,aggregated_rating,storyline; "
                        + "where id = %d; limit 1;",
                igdbId);

        List<Item> results = new ArrayList<>();
        executeQuery(results, bodyStr, "game");
        return results.isEmpty() ? null : results.get(0);
    }

    // ---- Internal ----

    private List<Item> executeQuery(List<Item> items, String bodyStr, String targetType) {
        try {
            String token = getAccessToken();
            if (token == null) return items;

            String response = restClient.post()
                    .uri(API_BASE + "/games")
                    .header("Client-ID", getClientId())
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "text/plain")
                    .body(bodyStr)
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank() || response.equals("[]")) return items;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> games = objectMapper.readValue(response, List.class);
            for (Map<String, Object> g : games) {
                Item item = mapToItem(g);
                if (item != null) items.add(item);
            }
            log.info("IGDB: {} games retrieved", items.size());
        } catch (Exception e) {
            log.error("IGDB query failed: {}", e.getMessage());
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private Item mapToItem(Map<String, Object> g) {
        try {
            int id = ((Number) g.get("id")).intValue();
            String name = str(g, "name");
            if (name == null || name.isBlank()) return null;

            String summary = str(g, "summary", "");
            String coverUrl = extractCover(g);
            Integer firstReleaseDate = g.get("first_release_date") != null
                    ? ((Number) g.get("first_release_date")).intValue() : null;
            Double rating = g.get("rating") != null
                    ? ((Number) g.get("rating")).doubleValue() : null;
            Integer ratingCount = g.get("total_rating_count") != null
                    ? ((Number) g.get("total_rating_count")).intValue() : null;

            String genres = joinNames((List<Map<String, Object>>) g.get("genres"));
            String platforms = joinNames((List<Map<String, Object>>) g.get("platforms"));
            String themes = joinNames((List<Map<String, Object>>) g.get("themes"));
            String gameModes = joinNames((List<Map<String, Object>>) g.get("game_modes"));
            String developer = extractCompany((List<Map<String, Object>>) g.get("involved_companies"), true);
            String publisher = extractCompany((List<Map<String, Object>>) g.get("involved_companies"), false);
            String screenshots = extractScreenshots((List<Map<String, Object>>) g.get("screenshots"));
            String trailer = extractVideo((List<Map<String, Object>>) g.get("videos"));
            String websites = extractWebsites((List<Map<String, Object>>) g.get("websites"));
            String steamUrl = extractSteamUrl((List<Map<String, Object>>) g.get("websites"));
            String similarNames = joinNames((List<Map<String, Object>>) g.get("similar_games"));

            int year = firstReleaseDate != null
                    ? java.time.Instant.ofEpochSecond(firstReleaseDate).atZone(java.time.ZoneOffset.UTC).getYear()
                    : 0;

            Item item = new Item();
            item.setType("game");
            item.setTitle(name);
            // 如果 IGDB 提供了 Steam 链接，使用与 Steam 源相同的 slug（steam-{appId}），
            // 这样同一游戏只会有一条记录，第二次抓取自动触发 updateBySlug 合并
            String slug = "igdb-" + id;
            if (steamUrl != null) {
                int appStart = steamUrl.indexOf("/app/");
                if (appStart >= 0) {
                    int appEnd = steamUrl.indexOf("/", appStart + 5);
                    String steamAppId = appEnd > 0
                            ? steamUrl.substring(appStart + 5, appEnd)
                            : steamUrl.substring(appStart + 5);
                    if (!steamAppId.isBlank()) slug = "steam-" + steamAppId;
                }
            }
            item.setSlug(slug);
            item.setCoverUrl(coverUrl);
            item.setDescription(summary != null ? summary : "");
            item.setExternalId(String.valueOf(id));
            item.setExternalLink(steamUrl != null ? steamUrl : "https://www.igdb.com/games/" + id);
            item.setSource("igdb");
            item.setStatus(0);

            StringBuilder info = new StringBuilder("{");
            info.append(escJsonKV("developer", developer));
            info.append(",").append(escJsonKV("publisher", publisher));
            info.append(",").append(escJsonKV("genre", genres));
            info.append(",").append(escJsonKV("platform", platforms));
            info.append(",").append(escJsonKV("theme", themes));
            info.append(",").append(escJsonKV("game_mode", gameModes));
            info.append(",").append(escJsonKV("screenshots", screenshots));
            info.append(",").append(escJsonKV("trailer", trailer));
            info.append(",").append(escJsonKV("websites", websites));
            info.append(",").append(escJsonKV("similar_games", similarNames));
            info.append(",").append("\"demo_available\":false");
            info.append(",").append("\"year\":").append(year);
            info.append(",").append("\"rating\":").append(rating != null ? String.format("%.1f", rating) : "0");
            info.append(",").append("\"rating_count\":").append(ratingCount != null ? ratingCount : 0);
            info.append(",").append("\"videos\":{\"bilibili\":\"\",\"youtube\":\"" + esc(trailer) + "\"}");
            info.append("}");
            item.setInfoJson(info.toString());

            log.info("IGDB: {} id={} rating={} cover={}", name, id, rating, coverUrl);
            return item;
        } catch (Exception e) {
            log.error("IGDB mapToItem failed: {}", e.getMessage());
            return null;
        }
    }

    // ---- Helpers ----

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? String.valueOf(v) : null;
    }

    private String str(Map<String, Object> m, String key, String def) {
        String v = str(m, key);
        return v != null ? v : def;
    }

    private String joinNames(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return "";
        List<String> names = new ArrayList<>();
        for (Map<String, Object> item : list) {
            Object name = item.get("name");
            if (name != null) names.add(String.valueOf(name));
        }
        return String.join(", ", names);
    }

    @SuppressWarnings("unchecked")
    private String extractCover(Map<String, Object> data) {
        Map<String, Object> cover = (Map<String, Object>) data.get("cover");
        if (cover == null) return "";
        String url = str(cover, "url");
        if (url == null || url.isBlank()) return "";
        return url.startsWith("//") ? "https:" + url.replace("t_thumb", "t_cover_big") : url;
    }

    private String extractScreenshots(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return "";
        List<String> urls = new ArrayList<>();
        for (Map<String, Object> s : list) {
            String url = str(s, "url");
            if (url != null && !url.isBlank()) {
                urls.add(url.startsWith("//") ? "https:" + url.replace("t_thumb", "t_screenshot_big") : url);
            }
        }
        return String.join("|", urls);
    }

    private String extractVideo(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return "";
        for (Map<String, Object> v : list) {
            String videoId = str(v, "video_id");
            if (videoId != null && !videoId.isBlank()) {
                return "https://www.youtube.com/embed/" + videoId;
            }
        }
        return "";
    }

    private String extractWebsites(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return "";
        List<String> urls = new ArrayList<>();
        for (Map<String, Object> w : list) {
            String url = str(w, "url");
            if (url != null && !url.isBlank()) urls.add(url);
        }
        return String.join("|", urls);
    }

    private String extractSteamUrl(List<Map<String, Object>> list) {
        if (list == null) return null;
        for (Map<String, Object> w : list) {
            String url = str(w, "url");
            if (url != null && url.contains("store.steampowered.com")) return url;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractCompany(List<Map<String, Object>> companies, boolean developer) {
        if (companies == null || companies.isEmpty()) return "";
        for (Map<String, Object> c : companies) {
            boolean isDev = Boolean.TRUE.equals(c.get("developer"));
            boolean isPub = Boolean.TRUE.equals(c.get("publisher"));
            if ((developer && isDev) || (!developer && isPub && !isDev)) {
                Map<String, Object> company = (Map<String, Object>) c.get("company");
                if (company != null) {
                    return String.valueOf(company.getOrDefault("name", ""));
                }
            }
        }
        return "";
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String escJsonKV(String key, String value) {
        return "\"" + key + "\":\"" + esc(value) + "\"";
    }
}
