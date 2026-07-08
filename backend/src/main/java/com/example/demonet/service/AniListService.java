package com.example.demonet.service;

import com.example.demonet.entity.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Slf4j
@Service
@lombok.RequiredArgsConstructor
public class AniListService {

    private final RestClient restClient;
    private static final String ENDPOINT = "https://graphql.anilist.co";

    @SuppressWarnings("unchecked")
    public List<Item> searchAnime(String query, String targetType) {
        List<Item> items = new ArrayList<>();
        try {
            String graphql = "{ \"query\": \"query ($search: String) { Page(perPage: 10) { media(search: $search, type: ANIME, sort: POPULARITY_DESC) { id title { romaji english native userPreferred } description coverImage { large extraLarge } bannerImage genres studios(isMain: true) { nodes { name } } seasonYear episodes averageScore synonyms } } }\", \"variables\": { \"search\": \"" + esc(query) + "\" } }";
            Map<String, Object> resp = restClient.post()
                    .uri(ENDPOINT)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(graphql)
                    .retrieve().body(new ParameterizedTypeReference<Map<String, Object>>() {});
            if (resp == null) return items;

            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            if (data == null) return items;
            Map<String, Object> page = (Map<String, Object>) data.get("Page");
            if (page == null) return items;
            List<Map<String, Object>> media = (List<Map<String, Object>>) page.get("media");
            if (media == null) return items;

            for (Map<String, Object> m : media) {
                Item item = buildItem(m, targetType != null ? targetType : "anime");
                if (item != null) items.add(item);
            }
            log.info("AniList: {} anime fetched for query '{}'", items.size(), query);
        } catch (Exception e) {
            log.error("AniList search failed: {}", e.getMessage());
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private Item buildItem(Map<String, Object> m, String type) {
        try {
            int id = (int) m.get("id");
            Map<String, Object> title = (Map<String, Object>) m.get("title");
            String nativeTitle = title != null ? String.valueOf(title.getOrDefault("native", "")) : "";
            String englishTitle = title != null ? String.valueOf(title.getOrDefault("english", "")) : "";
            String romajiTitle = title != null ? String.valueOf(title.getOrDefault("romaji", "")) : "";
            // Prefer native (kanji readable by Chinese users), fallback to romaji
            String displayTitle = !nativeTitle.isEmpty() && !nativeTitle.equals("null") ? nativeTitle
                    : !romajiTitle.isEmpty() && !romajiTitle.equals("null") ? romajiTitle : "Unknown";

            List<String> synonyms = (List<String>) m.get("synonyms");
            String synonymStr = synonyms != null && !synonyms.isEmpty() ? String.join(", ", synonyms) : "";
            String description = String.valueOf(m.getOrDefault("description", ""));
            description = description.length() > 500 ? description.substring(0, 500) : description;

            Map<String, Object> cover = (Map<String, Object>) m.get("coverImage");
            String coverUrl = cover != null ? String.valueOf(cover.getOrDefault("large", "")) : "";
            String wideCover = String.valueOf(m.getOrDefault("bannerImage", ""));

            List<String> genres = (List<String>) m.get("genres");
            String genreStr = genres != null ? String.join(", ", genres) : "";

            Map<String, Object> studios = (Map<String, Object>) m.get("studios");
            String studioName = "";
            if (studios != null) {
                List<Map<String, Object>> nodes = (List<Map<String, Object>>) studios.get("nodes");
                if (nodes != null && !nodes.isEmpty()) {
                    studioName = String.valueOf(nodes.get(0).get("name"));
                }
            }

            Integer year = (Integer) m.getOrDefault("seasonYear", 0);
            Integer episodes = (Integer) m.getOrDefault("episodes", 0);
            Integer score = (Integer) m.getOrDefault("averageScore", 0);

            Item item = new Item();
            item.setType(type);
            item.setTitle(displayTitle);
            item.setSlug("anilist-" + id);
            item.setCoverUrl(coverUrl);
            item.setWideCoverUrl(wideCover != null && !wideCover.equals("null") ? wideCover : coverUrl);
            item.setDescription(description);
            item.setExternalId(String.valueOf(id));
            item.setExternalLink("https://anilist.co/anime/" + id);
            item.setSource("anilist");
            item.setStatus(0);

            String infoJson = String.format(
                    "{\"studio\":\"%s\",\"year\":%d,\"genre\":\"%s\",\"episodes\":%d,\"score\":%d,\"english_title\":\"%s\",\"romaji_title\":\"%s\",\"synonyms\":\"%s\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                    esc(studioName), year, esc(genreStr), episodes, score,
                    esc(englishTitle), esc(romajiTitle), esc(synonymStr));
            item.setInfoJson(infoJson);

            return item;
        } catch (Exception e) {
            log.warn("AniList buildItem failed: {}", e.getMessage());
            return null;
        }
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
