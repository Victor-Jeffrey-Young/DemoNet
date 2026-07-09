package com.example.demonet.service;

import com.example.demonet.entity.AppSetting;
import com.example.demonet.mapper.AppSettingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SpotifyService {

    private final RestClient restClient;
    private final AppSettingMapper appSettingMapper;

    private String accessToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public SpotifyService(RestClient restClient, AppSettingMapper appSettingMapper) {
        this.restClient = restClient;
        this.appSettingMapper = appSettingMapper;
    }

    private String clientId() {
        AppSetting s = appSettingMapper.selectById("SPOTIFY_CLIENT_ID");
        return s != null ? s.getSettingValue() : "";
    }

    private String clientSecret() {
        AppSetting s = appSettingMapper.selectById("SPOTIFY_CLIENT_SECRET");
        return s != null ? s.getSettingValue() : "";
    }

    public String searchAlbum(String query) {
        String cId = clientId();
        String cSecret = clientSecret();
        if (cId.isEmpty() || cSecret.isEmpty()) {
            log.warn("Spotify credentials not configured — skipping search");
            return null;
        }
        try {
            ensureToken(cId, cSecret);
            Map<String, Object> resp = restClient.get()
                    .uri("https://api.spotify.com/v1/search?q={q}&type=album&limit=1", query)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (resp == null) return null;

            Map<String, Object> albums = (Map<String, Object>) resp.get("albums");
            if (albums == null) return null;

            List<Map<String, Object>> items = (List<Map<String, Object>>) albums.get("items");
            if (items == null || items.isEmpty()) return null;

            Map<String, Object> externalUrls = (Map<String, Object>) items.get(0).get("external_urls");
            if (externalUrls == null) return null;

            String url = (String) externalUrls.get("spotify");
            if (url != null) log.info("Spotify: found album at {}", url);
            return url;
        } catch (Exception e) {
            log.warn("Spotify search failed for '{}': {}", query, e.getMessage());
            return null;
        }
    }

    private void ensureToken(String cId, String cSecret) {
        if (Instant.now().isBefore(tokenExpiry)) return;

        String credentials = cId + ":" + cSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        try {
            Map<String, Object> resp = restClient.post()
                    .uri("https://accounts.spotify.com/api/token")
                    .header("Authorization", "Basic " + encoded)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (resp == null) {
                log.warn("Spotify token response was empty");
                return;
            }

            accessToken = (String) resp.get("access_token");
            int expiresIn = resp.get("expires_in") instanceof Number
                    ? ((Number) resp.get("expires_in")).intValue() : 3600;
            tokenExpiry = Instant.now().plusSeconds(expiresIn - 60);
            log.info("Spotify token acquired, expires in {}s", expiresIn);
        } catch (Exception e) {
            log.warn("Spotify token refresh failed: {}", e.getMessage());
        }
    }
}
