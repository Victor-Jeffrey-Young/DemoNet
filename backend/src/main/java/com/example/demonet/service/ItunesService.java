package com.example.demonet.service;

import com.example.demonet.entity.Item;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
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
    private final SpotifyService spotifyService;
    private final QQMusicService qqMusicService;
    private final ObjectMapper objectMapper;
    private static final String BASE = "https://itunes.apple.com/search";

    @SuppressWarnings("removal")
    public ItunesService(RestClient defaultRestClient, SpotifyService spotifyService, QQMusicService qqMusicService, ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                new MediaType("text", "javascript")
        ));
        this.restClient = defaultRestClient.mutate()
                .messageConverters(c -> c.add(0, converter))
                .build();
        this.spotifyService = spotifyService;
        this.qqMusicService = qqMusicService;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public List<Item> searchAlbums(String query, String targetType) {
        List<Item> items = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            Map<String, Object> resp = restClient.get()
                    .uri(BASE + "?term=" + encoded + "&country=cn&media=music&entity=album&limit=10")
                    .retrieve().body(new ParameterizedTypeReference<Map<String, Object>>() {});
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

            // Second pass: enrich with platform links
            for (Item item : items) {
                try {
                    enrichWithLinks(item);
                } catch (Exception e) {
                    log.warn("Failed to enrich '{}' with platform links: {}", item.getTitle(), e.getMessage());
                }
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

    @SuppressWarnings("unchecked")
    private void enrichWithLinks(Item item) throws Exception {
        Map<String, Object> info = objectMapper.readValue(item.getInfoJson(), Map.class);

        Map<String, Object> links = (Map<String, Object>) info.get("links");
        if (links == null) {
            links = new HashMap<>();
            info.put("links", links);
        }

        // Apple Music
        if (item.getExternalLink() != null && !item.getExternalLink().isEmpty()) {
            links.putIfAbsent("appleMusic", item.getExternalLink());
        }

        // Spotify
        if (!links.containsKey("spotify")) {
            String artist = (String) info.getOrDefault("artist", "");
            String spotifyUrl = spotifyService.searchAlbum(artist + " " + item.getTitle());
            if (spotifyUrl != null) {
                links.put("spotify", spotifyUrl);
            }
        }

        // QQ Music
        if (!links.containsKey("qqMusic")) {
            String artist = (String) info.getOrDefault("artist", "");
            String qqUrl = qqMusicService.searchAlbum(item.getTitle() + " " + artist);
            if (qqUrl != null) {
                links.put("qqMusic", qqUrl);
            }
        }

        item.setInfoJson(objectMapper.writeValueAsString(info));
    }
}
