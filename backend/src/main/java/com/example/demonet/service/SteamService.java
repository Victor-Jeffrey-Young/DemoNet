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
public class SteamService {

    private final RestClient restClient = RestClient.create();

    @Value("${app.steam.api-key:}")
    private String apiKey;

    public List<Item> fetchByAppIds(List<Long> appIds) {
        List<Item> items = new ArrayList<>();
        for (Long appId : appIds) {
            try {
                Map<String, Object> resp = restClient.get()
                        .uri("https://store.steampowered.com/api/appdetails?appids=" + appId + "&cc=cn&l=zh")
                        .retrieve()
                        .body(Map.class);

                if (resp == null) continue;
                Map<String, Object> appData = (Map<String, Object>) resp.get(String.valueOf(appId));
                if (appData == null || !Boolean.TRUE.equals(appData.get("success"))) continue;
                Map<String, Object> data = (Map<String, Object>) appData.get("data");
                if (data == null) continue;

                Item item = new Item();
                item.setType("game");
                item.setTitle(String.valueOf(data.getOrDefault("name", "Unknown")));
                item.setSlug("steam-" + appId);
                item.setCoverUrl(String.valueOf(data.getOrDefault("header_image", "")));
                item.setDescription(String.valueOf(data.getOrDefault("short_description", "")));
                item.setExternalId(String.valueOf(appId));
                item.setSource("steam");
                item.setStatus(0);
                items.add(item);

                log.info("Steam fetched: {} (appid={})", item.getTitle(), appId);
            } catch (Exception e) {
                log.error("Steam fetch failed for appid {}: {}", appId, e.getMessage());
            }
        }
        return items;
    }
}
