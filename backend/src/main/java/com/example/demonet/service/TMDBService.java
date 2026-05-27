package com.example.demonet.service;

import com.example.demonet.entity.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TMDBService {

    private final RestClient restClient = RestClient.create();

    @Value("${app.tmdb.api-key:}")
    private String apiKey;

    public List<Item> searchMovies(String query) {
        List<Item> items = new ArrayList<>();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("TMDB API key not configured, skipping search");
            return items;
        }

        try {
            Map<String, Object> resp = restClient.get()
                    .uri("https://api.themoviedb.org/3/search/movie?api_key=" + apiKey +
                         "&language=zh-CN&query=" + query + "&page=1")
                    .retrieve()
                    .body(Map.class);

            if (resp == null) return items;
            List<Map<String, Object>> results = (List<Map<String, Object>>) resp.get("results");
            if (results == null) return items;

            for (Map<String, Object> r : results) {
                Item item = new Item();
                item.setType("movie");
                item.setTitle(String.valueOf(r.getOrDefault("title", "Unknown")));
                Integer tmdbId = (Integer) r.get("id");
                item.setSlug("tmdb-" + tmdbId);
                item.setDescription(String.valueOf(r.getOrDefault("overview", "")));
                Object posterPath = r.get("poster_path");
                if (posterPath != null && !posterPath.toString().isBlank()) {
                    item.setCoverUrl("https://image.tmdb.org/t/p/w500" + posterPath);
                }
                item.setExternalId(String.valueOf(tmdbId));
                item.setSource("tmdb");
                item.setStatus(0);
                items.add(item);

                log.info("TMDB fetched: {} (id={})", item.getTitle(), tmdbId);
            }
        } catch (Exception e) {
            log.error("TMDB fetch failed: {}", e.getMessage());
        }
        return items;
    }
}
