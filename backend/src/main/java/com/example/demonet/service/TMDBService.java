package com.example.demonet.service;

import com.example.demonet.entity.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
            Map<String, Object> resp = restClient.get()
                    .uri(BASE + "/search/movie?api_key=" + apiKey + "&language=zh-CN&query=" + query)
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
        } catch (Exception e) { log.error("TMDB detail failed for {}: {}", tmdbId, e.getMessage()); return null; }
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
