package com.example.demonet.service;

import com.example.demonet.entity.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class ItunesService {

    private final RestClient restClient;
    private static final String BASE = "https://itunes.apple.com/search";

    public ItunesService(RestClient defaultRestClient) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                new MediaType("text", "javascript")
        ));
        this.restClient = defaultRestClient.mutate()
                .messageConverters(c -> c.add(0, converter))
                .build();
    }

    public List<Item> searchAlbums(String query, String targetType) {
        List<Item> items = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            Map<String, Object> resp = restClient.get()
                    .uri(BASE + "?term=" + encoded + "&country=cn&media=music&entity=album&limit=10")
                    .retrieve().body(Map.class);
            if (resp == null) return items;

            List<Map<String, Object>> results = (List<Map<String, Object>>) resp.get("results");
            if (results == null) return items;

            Set<String> seen = new HashSet<>();
            for (Map<String, Object> r : results) {
                String collectionName = String.valueOf(r.getOrDefault("collectionName", ""));
                if (collectionName.isEmpty() || seen.contains(collectionName)) continue;
                seen.add(collectionName);

                Item item = buildItem(r, targetType != null ? targetType : "music");
                if (item != null) items.add(item);
            }
            log.info("iTunes: {} albums fetched for '{}'", items.size(), query);
        } catch (Exception e) {
            log.error("iTunes search failed: {}", e.getMessage());
        }
        return items;
    }

    private Item buildItem(Map<String, Object> r, String type) {
        try {
            String collectionName = String.valueOf(r.getOrDefault("collectionName", "Unknown"));
            String artistName = String.valueOf(r.getOrDefault("artistName", ""));
            String artworkUrl = String.valueOf(r.getOrDefault("artworkUrl100", ""));
            String previewUrl = String.valueOf(r.getOrDefault("previewUrl", ""));
            String date = String.valueOf(r.getOrDefault("releaseDate", ""));
            Integer trackCount = (Integer) r.get("trackCount");
            String genre = String.valueOf(r.getOrDefault("primaryGenreName", ""));

            // Upgrade artwork to larger size: 100x100 → 600x600
            artworkUrl = artworkUrl.replace("100x100bb", "600x600bb");

            int collectionId = (int) r.get("collectionId");

            Item item = new Item();
            item.setType(type);
            item.setTitle(collectionName);
            item.setSlug("itunes-" + collectionId);
            item.setCoverUrl(artworkUrl);
            item.setWideCoverUrl(artworkUrl);
            item.setDescription(artistName + " · " + (trackCount != null ? trackCount + " tracks" : ""));
            item.setExternalId(String.valueOf(collectionId));
            item.setExternalLink("https://music.apple.com/cn/album/" + collectionId);
            item.setSource("itunes");
            item.setStatus(0);

            int year = 0;
            if (!date.isEmpty() && date.length() >= 4) year = Integer.parseInt(date.substring(0, 4));

            String infoJson = String.format(
                    "{\"artist\":\"%s\",\"year\":%d,\"genre\":\"%s\",\"tracks\":%d,\"preview_url\":\"%s\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                    esc(artistName), year, esc(genre),
                    trackCount != null ? trackCount : 0, esc(previewUrl));
            item.setInfoJson(infoJson);

            log.info("iTunes: {} by {} (id={})", collectionName, artistName, collectionId);
            return item;
        } catch (Exception e) {
            log.warn("iTunes buildItem failed: {}", e.getMessage());
            return null;
        }
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
