package com.example.demonet.service;

import com.example.demonet.entity.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class TMDBService {

    private final RestClient restClient = RestClient.create();
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.tmdb.api-key:}")
    private String apiKey;

    private static final String BASE = "https://api.themoviedb.org/3";
    private static final String IMG_BASE = "https://image.tmdb.org/t/p";

    public TMDBService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Item> searchMovies(String query) {
        List<Item> items = new ArrayList<>();
        if (apiKey == null || apiKey.isBlank()) { log.warn("TMDB key missing"); return items; }
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            Map<String, Object> resp = restClient.get()
                    .uri(BASE + "/search/movie?api_key=" + apiKey + "&language=zh-CN&query=" + encoded)
                    .retrieve().body(Map.class);
            if (resp == null) return items;
            List<Map<String, Object>> results = (List<Map<String, Object>>) resp.get("results");
            if (results == null) return items;
            for (Map<String, Object> r : results) {
                Integer tmdbId = (Integer) r.get("id");
                Item item = fetchMovieDetail(tmdbId);
                if (item != null) items.add(item);
            }
        } catch (Exception e) { log.error("TMDB search failed: {}", e.getMessage()); }
        return items;
    }

    public Item fetchMovieDetail(int tmdbId) {
        try {
            Map<String, Object> resp = restClient.get()
                    .uri(BASE + "/movie/" + tmdbId + "?api_key=" + apiKey + "&language=zh-CN&append_to_response=videos,credits")
                    .retrieve().body(Map.class);
            if (resp == null) return null;

            String title = String.valueOf(resp.getOrDefault("title", "Unknown"));
            String overview = String.valueOf(resp.getOrDefault("overview", ""));
            String poster = extractImage(resp, "poster_path", "w500");
            String backdrop = extractImage(resp, "backdrop_path", "w1280");
            String date = String.valueOf(resp.getOrDefault("release_date", ""));
            Integer runtime = (Integer) resp.get("runtime");

            String director = extractDirector(resp);
            String genres = extractGenres(resp);
            String trailer = extractTrailer(resp);

            Item item = new Item();
            item.setType("movie");
            item.setTitle(title);
            item.setSlug("tmdb-" + tmdbId);
            item.setCoverUrl(poster);
            item.setWideCoverUrl(backdrop);
            item.setDescription(overview);
            item.setExternalId(String.valueOf(tmdbId));
            item.setExternalLink("https://www.themoviedb.org/movie/" + tmdbId);
            item.setSource("tmdb");
            item.setStatus(0);

            String infoJson = String.format(
                    "{\"director\":\"%s\",\"year\":%d,\"duration\":\"%s\",\"genre\":\"%s\",\"trailer\":\"%s\",\"videos\":{\"bilibili\":\"\"}}",
                    esc(director), date.isEmpty() ? 0 : Integer.parseInt(date.substring(0,4)),
                    runtime != null ? runtime + "min" : "",
                    esc(genres), esc(trailer));
            item.setInfoJson(infoJson);

            log.info("TMDB: {} (id={}) poster={}", title, tmdbId, poster);
            return item;
        } catch (Exception e) { log.error("TMDB movie detail failed for {}: {}", tmdbId, e.getMessage()); return null; }
    }

    // ========== TV Search ==========

    public List<Item> searchTV(String query) {
        List<Item> items = new ArrayList<>();
        if (apiKey == null || apiKey.isBlank()) { log.warn("TMDB key missing"); return items; }
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            Map<String, Object> resp = restClient.get()
                    .uri(BASE + "/search/tv?api_key=" + apiKey + "&language=zh-CN&query=" + encoded)
                    .retrieve().body(Map.class);
            if (resp == null) return items;
            List<Map<String, Object>> results = (List<Map<String, Object>>) resp.get("results");
            if (results == null) return items;
            for (Map<String, Object> r : results) {
                Integer tvId = (Integer) r.get("id");
                Item item = fetchTVDetail(tvId);
                if (item != null) items.add(item);
            }
        } catch (Exception e) { log.error("TMDB TV search failed: {}", e.getMessage()); }
        return items;
    }

    public Item fetchTVDetail(int tvId) {
        try {
            Map<String, Object> resp = restClient.get()
                    .uri(BASE + "/tv/" + tvId + "?api_key=" + apiKey + "&language=zh-CN&append_to_response=videos,credits")
                    .retrieve().body(Map.class);
            if (resp == null) return null;

            String title = String.valueOf(resp.getOrDefault("name", "Unknown"));
            String overview = String.valueOf(resp.getOrDefault("overview", ""));
            String poster = extractImage(resp, "poster_path", "w500");
            String backdrop = extractImage(resp, "backdrop_path", "w1280");
            String date = String.valueOf(resp.getOrDefault("first_air_date", ""));
            Integer episodes = (Integer) resp.get("number_of_episodes");
            Integer seasons = (Integer) resp.get("number_of_seasons");

            String creator = extractTVCreator(resp);
            String genres = extractGenres(resp);
            String network = extractTVNetwork(resp);
            String trailer = extractTrailer(resp);

            Item item = new Item();
            item.setType("anime");
            item.setTitle(title);
            item.setSlug("tmdb-tv-" + tvId);
            item.setCoverUrl(poster);
            item.setWideCoverUrl(backdrop);
            item.setDescription(overview);
            item.setExternalId(String.valueOf(tvId));
            item.setExternalLink("https://www.themoviedb.org/tv/" + tvId);
            item.setSource("tmdb");
            item.setStatus(0);

            int year = date.isEmpty() ? 0 : Integer.parseInt(date.substring(0, 4));
            String infoJson = String.format(
                    "{\"studio\":\"%s\",\"network\":\"%s\",\"year\":%d,\"genre\":\"%s\",\"episodes\":%d,\"seasons\":%d,\"trailer\":\"%s\",\"videos\":{\"bilibili\":\"\"}}",
                    esc(creator), esc(network), year, esc(genres),
                    episodes != null ? episodes : 0, seasons != null ? seasons : 0,
                    esc(trailer));
            item.setInfoJson(infoJson);

            log.info("TMDB TV: {} (id={})", title, tvId);
            return item;
        } catch (Exception e) { log.error("TMDB TV detail failed for {}: {}", tvId, e.getMessage()); return null; }
    }

    private String extractTVCreator(Map<String, Object> data) {
        try {
            List<Map<String, Object>> createdBy = (List<Map<String, Object>>) data.get("created_by");
            if (createdBy != null && !createdBy.isEmpty()) {
                return String.valueOf(createdBy.get(0).get("name"));
            }
        } catch (Exception e) { /* ignore */ }
        return "";
    }

    private String extractTVNetwork(Map<String, Object> data) {
        try {
            List<Map<String, Object>> networks = (List<Map<String, Object>>) data.get("networks");
            if (networks != null && !networks.isEmpty()) {
                return String.valueOf(networks.get(0).get("name"));
            }
        } catch (Exception e) { /* ignore */ }
        return "";
    }

    private String extractImage(Map<String, Object> data, String key, String size) {
        Object val = data.get(key);
        if (val == null || String.valueOf(val).isBlank()) return "";
        return IMG_BASE + "/" + size + val;
    }

    private String extractDirector(Map<String, Object> data) {
        try {
            Map<String, Object> credits = (Map<String, Object>) data.get("credits");
            if (credits == null) return "";
            List<Map<String, Object>> crew = (List<Map<String, Object>>) credits.get("crew");
            if (crew == null) return "";
            for (Map<String, Object> c : crew) {
                if ("Director".equals(c.get("job"))) return String.valueOf(c.get("name"));
            }
        } catch (Exception e) { /* ignore */ }
        return "";
    }

    private String extractGenres(Map<String, Object> data) {
        List<Map<String, Object>> genres = (List<Map<String, Object>>) data.get("genres");
        if (genres == null) return "";
        List<String> names = new ArrayList<>();
        for (Map<String, Object> g : genres) names.add(String.valueOf(g.get("name")));
        return String.join(", ", names);
    }

    private String extractTrailer(Map<String, Object> data) {
        try {
            Map<String, Object> videos = (Map<String, Object>) data.get("videos");
            if (videos == null) return "";
            List<Map<String, Object>> results = (List<Map<String, Object>>) videos.get("results");
            if (results == null) return "";
            for (Map<String, Object> v : results) {
                if ("YouTube".equals(v.get("site")) && "Trailer".equals(v.get("type"))) {
                    return "https://www.youtube.com/embed/" + v.get("key");
                }
            }
        } catch (Exception e) { /* ignore */ }
        return "";
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"");
    }
}
