package com.example.demonet.service;

import com.example.demonet.config.RabbitMQConfig;
import com.example.demonet.entity.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchConsumer {

    private final SteamService steamService;
    private final TMDBService tmdbService;
    private final AniListService aniListService;
    private final BangumiService bangumiService;
    private final ItunesService itunesService;
    private final IGDBService igdbService;
    private final ItemService itemService;
    private final TagService tagService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_STEAM)
    public void handleSteamFetch(Map<String, Object> payload) {
        List<Long> appIds = new ArrayList<>();
        Object rawIds = payload.get("appIds");
        if (rawIds instanceof List) {
            for (Object id : (List<?>) rawIds) {
                if (id instanceof Number) appIds.add(((Number) id).longValue());
            }
        }
        String targetType = (String) payload.getOrDefault("targetType", "game");
        log.info("Steam fetch: {} appIds → type={}", appIds.size(), targetType);
        List<Item> items = steamService.fetchByAppIds(appIds);
        saveItems(items, targetType, "Steam");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TMDB)
    public void handleTMDBFetch(Map<String, Object> payload) {
        String query = (String) payload.get("query");
        String targetType = (String) payload.getOrDefault("targetType", "movie");
        log.info("TMDB fetch: '{}' → type={}", query, targetType);
        List<Item> items = tmdbService.searchMovies(query);
        saveItems(items, targetType, "TMDB");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ANILIST)
    public void handleAniListFetch(Map<String, Object> payload) {
        String query = (String) payload.get("query");
        String targetType = (String) payload.getOrDefault("targetType", "anime");
        log.info("AniList fetch: '{}' → type={}", query, targetType);
        List<Item> items = aniListService.searchAnime(query, targetType);
        saveItems(items, targetType, "AniList");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_BANGUMI)
    public void handleBangumiFetch(Map<String, Object> payload) {
        String query = (String) payload.get("query");
        String targetType = (String) payload.getOrDefault("targetType", "anime");
        log.info("Bangumi fetch: '{}' → type={}", query, targetType);
        List<Item> items = bangumiService.searchSubjects(query, targetType);
        saveItems(items, targetType, "Bangumi");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TMDB_TV)
    public void handleTMDBTVFetch(Map<String, Object> payload) {
        String query = (String) payload.get("query");
        String targetType = (String) payload.getOrDefault("targetType", "anime");
        log.info("TMDB TV fetch: '{}' → type={}", query, targetType);
        List<Item> items = tmdbService.searchTV(query);
        saveItems(items, targetType, "TMDB-TV");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ITUNES)
    public void handleItunesFetch(Map<String, Object> payload) {
        String query = (String) payload.get("query");
        String targetType = (String) payload.getOrDefault("targetType", "music");
        log.info("iTunes fetch: '{}' → type={}", query, targetType);
        List<Item> items = itunesService.searchAlbums(query, targetType);
        saveItems(items, targetType, "iTunes");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_IGDB)
    public void handleIGDBFetch(Map<String, Object> payload) {
        String endpoint = (String) payload.getOrDefault("endpoint", "search");
        String query = (String) payload.get("query");
        Integer limit = payload.get("limit") != null ? ((Number) payload.get("limit")).intValue() : 10;
        String targetType = (String) payload.getOrDefault("targetType", "game");
        List<Item> items;
        switch (endpoint) {
            case "popular": items = igdbService.fetchPopularGames(limit); break;
            case "recent":  items = igdbService.fetchRecentGames(limit); break;
            default:        items = igdbService.searchGames(query != null ? query : "", limit);
        }
        saveItems(items, targetType, "IGDB");
    }

    private void saveItems(List<Item> items, String targetType, String source) {
        int saved = 0, updated = 0, failed = 0;
        for (Item item : items) {
            if (!targetType.equals(item.getType())) item.setType(targetType);
            try {
                itemService.createItem(item);
                autoTag(item);
                saved++;
            } catch (DuplicateKeyException e) {
                itemService.updateBySlug(item);
                updated++;
            } catch (Exception e) {
                failed++;
                log.error("{} item failed: {} — {}", source, item.getTitle(), e.getMessage());
            }
        }
        String msg = String.format("%s fetch: %d new, %d updated, %d failed → type=%s",
                source, saved, updated, failed, targetType);
        if (failed > 0) log.error(msg); else log.info(msg);
    }

    /** Auto-tag from genre & features in infoJson */
    @SuppressWarnings("unchecked")
    private void autoTag(Item item) {
        try {
            Map<String, Object> info = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(item.getInfoJson() != null ? item.getInfoJson() : "{}", Map.class);
            Set<String> keywords = new LinkedHashSet<>();
            String genre = (String) info.get("genre");
            if (genre != null) for (String g : genre.split(",")) {
                String t = g.trim(); if (!t.isEmpty() && t.length() <= 20) keywords.add(t);
            }
            Object features = info.get("features");
            if (features instanceof List) for (Object f : (List<?>) features) {
                if (f instanceof String s) { s = s.trim(); if (!s.isEmpty() && s.length() <= 20) keywords.add(s); }
            }
            for (String kw : keywords) tagService.addItemTag(item.getId(), kw);
        } catch (Exception ignored) {}
    }
}
