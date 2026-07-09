package com.example.demonet.service;

import com.example.demonet.entity.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class QQMusicService {

    private final RestClient restClient;

    private static final String SEARCH_URL = "https://u.y.qq.com/cgi-bin/musicu.fcg";

    @SuppressWarnings("removal")
    public QQMusicService() {
        System.setProperty("https.protocols", "TLSv1.2");

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(15000);

        try {
            TrustManager[] trustAll = { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
            }};
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAll, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((h, s) -> true);
        } catch (Exception e) {
            log.warn("Failed to configure relaxed SSL: {}", e.getMessage());
        }

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                new MediaType("text", "javascript"),
                new MediaType("text", "plain")
        ));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .messageConverters(c -> c.add(0, converter))
                .build();
    }

    @SuppressWarnings("unchecked")
    public String searchAlbum(String query) {
        try {
            List<Map<String, Object>> albumList = fetchAlbums(query, 5);
            if (albumList == null || albumList.isEmpty()) return null;

            String albumMID = (String) albumList.get(0).get("albumMID");
            if (albumMID == null || albumMID.isEmpty()) return null;

            String url = "https://y.qq.com/n/ryqq/albumDetail/" + albumMID;
            log.info("QQMusic: found album at {}", url);
            return url;
        } catch (Exception e) {
            log.warn("QQMusic search failed for '{}': {}", query, e.getMessage());
            return null;
        }
    }

    /** Search QQ Music by artist name and return a list of Items */
    @SuppressWarnings("unchecked")
    public List<Item> searchAlbums(String query, String targetType) {
        List<Item> items = new ArrayList<>();
        try {
            List<Map<String, Object>> albumList = fetchAlbums(query, 20);
            if (albumList == null || albumList.isEmpty()) {
                log.info("QQMusic: no albums found for '{}'", query);
                return items;
            }

            for (Map<String, Object> r : albumList) {
                try {
                    Item item = buildItem(r, targetType != null ? targetType : "music");
                    if (item != null) items.add(item);
                } catch (Exception e) {
                    log.warn("QQMusic buildItem failed: {}", e.getMessage());
                }
            }
            log.info("QQMusic: {} albums fetched for '{}'", items.size(), query);
        } catch (Exception e) {
            log.error("QQMusic search failed: {}", e.getMessage());
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchAlbums(String query, int limit) {
        Map<String, Object> param = new HashMap<>();
        param.put("query", query);
        param.put("search_type", 2);
        param.put("page_num", 1);
        param.put("page_size", limit);

        Map<String, Object> reqData = new HashMap<>();
        reqData.put("module", "music.search.SearchCgiService");
        reqData.put("method", "DoSearchForQQMusicDesktop");
        reqData.put("param", param);

        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("req_0", reqData);

        Map<String, Object> resp = restClient.post()
                .uri(SEARCH_URL + "?format=json&inCharset=utf8")
                .contentType(MediaType.APPLICATION_JSON)
                .body(wrapper)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (resp == null) return null;
        Map<String, Object> req0 = (Map<String, Object>) resp.get("req_0");
        if (req0 == null) return null;
        Map<String, Object> data = (Map<String, Object>) req0.get("data");
        if (data == null) return null;
        Map<String, Object> body = (Map<String, Object>) data.get("body");
        if (body == null) return null;
        Map<String, Object> album = (Map<String, Object>) body.get("album");
        if (album == null) return null;
        return (List<Map<String, Object>>) album.get("list");
    }

    /** Search preview — returns simplified album list for dropdown */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchPreview(String query, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            List<Map<String, Object>> raw = fetchAlbums(query, limit);
            if (raw == null) return results;
            for (Map<String, Object> r : raw) {
                Map<String, Object> simple = new HashMap<>();
                simple.put("id", r.get("albumID"));
                simple.put("name", r.get("albumName"));
                simple.put("artist", r.get("singerName"));
                simple.put("cover", String.valueOf(r.getOrDefault("albumPic", "")).replace("T002R180x180M000", "T002R300x300M000"));
                simple.put("songCount", r.get("song_count"));
                simple.put("year", extractYear(String.valueOf(r.getOrDefault("publicTime", ""))));
                simple.put("albumMID", r.get("albumMID"));
                results.add(simple);
            }
        } catch (Exception e) {
            log.warn("QQMusic searchPreview failed: {}", e.getMessage());
        }
        return results;
    }

    /** Fetch and build a single album by artist name, filtering by albumMID */
    public Item fetchByAlbumId(String albumMID, String artist, String type) {
        List<Map<String, Object>> raw = fetchAlbums(artist, 20);
        if (raw == null) return null;
        for (Map<String, Object> r : raw) {
            if (albumMID.equals(String.valueOf(r.get("albumMID")))) {
                return buildItem(r, type);
            }
        }
        return null;
    }

    private Item buildItem(Map<String, Object> r, String type) {
        String albumName = String.valueOf(r.getOrDefault("albumName", "Unknown"));
        String singerName = String.valueOf(r.getOrDefault("singerName", ""));
        String albumPic = String.valueOf(r.getOrDefault("albumPic", ""));
        String date = String.valueOf(r.getOrDefault("publicTime", ""));
        Object songCountObj = r.get("song_count");
        int songCount = songCountObj instanceof Number ? ((Number) songCountObj).intValue() : 0;
        String albumMID = String.valueOf(r.getOrDefault("albumMID", ""));
        String albumID = String.valueOf(r.getOrDefault("albumID", ""));

        if (albumMID.isEmpty()) return null;

        // Upgrade cover to larger size: 180x180 → 600x600
        albumPic = albumPic.replace("T002R180x180M000", "T002R300x300M000");

        int albumIdInt = 0;
        try { albumIdInt = Integer.parseInt(albumID); } catch (Exception ignored) {}

        Item item = new Item();
        item.setType(type);
        item.setTitle(albumName);
        item.setSlug("qqmusic-" + albumIdInt);
        item.setCoverUrl(albumPic);
        item.setWideCoverUrl(albumPic);
        item.setDescription(singerName + (songCount > 0 ? " · " + songCount + " tracks" : ""));
        item.setExternalId(albumMID);
        item.setExternalLink("https://y.qq.com/n/ryqq/albumDetail/" + albumMID);
        item.setSource("qqmusic");
        item.setStatus(0);

        // infoJson: minimal to match music type
        int year = 0;
        if (!date.isEmpty() && date.length() >= 4) {
            try { year = Integer.parseInt(date.substring(0, 4)); } catch (Exception ignored) {}
        }
        String infoJson = String.format(
                "{\"artist\":\"%s\",\"year\":%d,\"tracks\":%d,\"preview_url\":\"\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"},\"links\":{\"qqMusic\":\"%s\"}}",
                esc(singerName), year, songCount, esc(item.getExternalLink()));
        item.setInfoJson(infoJson);

        log.info("QQMusic: {} by {} (mid={})", albumName, singerName, albumMID);
        return item;
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private int extractYear(String date) {
        if (date != null && date.length() >= 4) {
            try { return Integer.parseInt(date.substring(0, 4)); } catch (Exception ignored) {}
        }
        return 0;
    }
}
