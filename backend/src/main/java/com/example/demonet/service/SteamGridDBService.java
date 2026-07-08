package com.example.demonet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * SteamGridDB API v2 — community-curated vertical game covers (600×900).
 * Free tier: rate-limited but sufficient for backfill + new fetches.
 * API key: set via admin panel → DB (app_settings), fallback to env var.
 * Docs: https://www.steamgriddb.com/api/v2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SteamGridDBService {

    private final RestClient restClient;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.steamgriddb.api-key:}")
    private String apiKeyFromConfig;

    private String cachedApiKey = null;
    private long lastKeyFetchTime = 0;

    /** Returns API key from DB (set via admin panel), falling back to env var. */
    private String getApiKey() {
        if (System.currentTimeMillis() - lastKeyFetchTime < 300000 && cachedApiKey != null) {
            return cachedApiKey;
        }
        try {
            String dbKey = jdbcTemplate.queryForObject(
                "SELECT setting_value FROM app_settings WHERE setting_key = 'STEAMGRIDDB_API_KEY'", String.class);
            if (dbKey != null && !dbKey.isBlank()) {
                cachedApiKey = dbKey;
                lastKeyFetchTime = System.currentTimeMillis();
                return dbKey;
            }
        } catch (Exception ignored) {}
        cachedApiKey = apiKeyFromConfig;
        lastKeyFetchTime = System.currentTimeMillis();
        return apiKeyFromConfig;
    }

    /** Returns vertical (600×900) cover URL for a Steam AppID, or null if not found. */
    public String findPosterUrl(Long steamAppId) {
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isBlank()) return null;
        try {
            // Step 1: resolve game id from Steam AppID
            Map<String, Object> gameResp = restClient.get()
                    .uri("https://www.steamgriddb.com/api/v2/games/steam/" + steamAppId)
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            if (gameResp == null || !Boolean.TRUE.equals(gameResp.get("success"))) return null;
            Object gameData = gameResp.get("data");
            Long gameId = null;
            if (gameData instanceof Map) {
                gameId = ((Number) ((Map<?,?>) gameData).get("id")).longValue();
            }
            if (gameId == null) return null;

            // Step 2: fetch grids (covers) for this game
            Map<String, Object> gridResp = restClient.get()
                    .uri("https://www.steamgriddb.com/api/v2/grids/game/" + gameId
                            + "?styles=alternate&dimensions=600x900&mimes=image/jpeg,image/png&types=static&limit=1")
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            if (gridResp == null || !Boolean.TRUE.equals(gridResp.get("success"))) return null;
            Object grids = gridResp.get("data");
            if (grids instanceof List && !((List<?>) grids).isEmpty()) {
                Object first = ((List<?>) grids).get(0);
                if (first instanceof Map) {
                    String url = (String) ((Map<?,?>) first).get("url");
                    if (url != null && !url.isBlank()) {
                        log.info("SteamGridDB poster found for appid {}: {}", steamAppId, url);
                        return url;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("SteamGridDB lookup failed for appid {}: {}", steamAppId, e.getMessage());
        }
        return null;
    }
}
