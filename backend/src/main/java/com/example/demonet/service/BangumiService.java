package com.example.demonet.service;

import com.example.demonet.entity.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Slf4j
@Service
public class BangumiService {

    private final RestClient restClient = RestClient.create();
    private static final String BASE = "https://api.bgm.tv/v0";

    public List<Item> searchSubjects(String keyword, String targetType) {
        List<Item> items = new ArrayList<>();
        try {
            Map<String, Object> resp = restClient.get()
                    .uri(BASE + "/search/subjects?keyword=" + java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8) + "&type=2&limit=10")
                    .header("User-Agent", "DemoNet/1.0 (demonet.local)")
                    .retrieve().body(Map.class);
            if (resp == null) return items;

            List<Map<String, Object>> data = (List<Map<String, Object>>) resp.get("data");
            if (data == null) return items;

            for (Map<String, Object> d : data) {
                Item item = buildItem(d, targetType != null ? targetType : "anime");
                if (item != null) items.add(item);
            }
            log.info("Bangumi: {} subjects fetched for '{}'", items.size(), keyword);
        } catch (Exception e) {
            log.error("Bangumi search failed: {}", e.getMessage());
        }
        return items;
    }

    private Item buildItem(Map<String, Object> d, String type) {
        try {
            int id = (int) d.get("id");
            String nameCn = String.valueOf(d.getOrDefault("name_cn", ""));
            String name = String.valueOf(d.getOrDefault("name", ""));
            String displayTitle = !nameCn.isEmpty() && !nameCn.equals("null") ? nameCn : name;
            String summary = String.valueOf(d.getOrDefault("summary", ""));
            summary = summary.length() > 800 ? summary.substring(0, 800) : summary;

            Map<String, Object> images = (Map<String, Object>) d.get("images");
            String coverUrl = "";
            String wideCoverUrl = "";
            if (images != null) {
                coverUrl = String.valueOf(images.getOrDefault("large", ""));
                wideCoverUrl = String.valueOf(images.getOrDefault("common", ""));
                if (wideCoverUrl.isEmpty() || wideCoverUrl.equals("null")) {
                    wideCoverUrl = coverUrl;
                }
            }

            String date = String.valueOf(d.getOrDefault("date", ""));
            int year = 0;
            if (!date.isEmpty() && !date.equals("null") && date.length() >= 4) {
                year = Integer.parseInt(date.substring(0, 4));
            }

            List<Map<String, Object>> tags = (List<Map<String, Object>>) d.get("tags");
            List<String> tagNames = new ArrayList<>();
            if (tags != null) {
                for (Map<String, Object> t : tags) {
                    String tagName = String.valueOf(t.get("name"));
                    if (tagName != null && !tagName.isEmpty()) tagNames.add(tagName);
                }
            }
            String genreStr = String.join(", ", tagNames);

            Item item = new Item();
            item.setType(type);
            item.setTitle(displayTitle);
            item.setSlug("bangumi-" + id);
            item.setCoverUrl(coverUrl);
            item.setWideCoverUrl(wideCoverUrl);
            item.setDescription(summary);
            item.setExternalId(String.valueOf(id));
            item.setExternalLink("https://bgm.tv/subject/" + id);
            item.setSource("bangumi");
            item.setStatus(0);

            String infoJson = String.format(
                    "{\"name_jp\":\"%s\",\"year\":%d,\"genre\":\"%s\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                    esc(name), year, esc(genreStr));
            item.setInfoJson(infoJson);

            log.info("Bangumi: {} (id={}) cn={}", displayTitle, id, nameCn);
            return item;
        } catch (Exception e) {
            log.warn("Bangumi buildItem failed: {}", e.getMessage());
            return null;
        }
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
