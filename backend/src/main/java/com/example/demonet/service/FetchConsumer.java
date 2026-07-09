package com.example.demonet.service;

import com.example.demonet.config.RabbitMQConfig;
import com.example.demonet.entity.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

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
    private final ObjectMapper objectMapper;

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
        String externalId = (String) payload.get("externalId");
        log.info("TMDB fetch: '{}' extId={} → type={}", query, externalId, targetType);
        List<Item> items;
        if (externalId != null && !externalId.isBlank()) {
            try { items = List.of(tmdbService.fetchMovieDetail(Integer.parseInt(externalId))); }
            catch (Exception e) { log.error("TMDB fetch by id failed: {}", e.getMessage()); items = List.of(); }
        } else {
            items = tmdbService.searchMovies(query);
        }
        saveItems(items, targetType, "TMDB");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ANILIST)
    public void handleAniListFetch(Map<String, Object> payload) {
        String query = (String) payload.get("query");
        String targetType = (String) payload.getOrDefault("targetType", "anime");
        String externalId = (String) payload.get("externalId");
        log.info("AniList fetch: '{}' extId={} → type={}", query, externalId, targetType);
        List<Item> items = aniListService.searchAnime(query, targetType);
        if (externalId != null && !externalId.isBlank()) {
            String eid = externalId;
            items = items.stream().filter(i -> eid.equals(i.getExternalId())).toList();
        }
        saveItems(items, targetType, "AniList");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_BANGUMI)
    public void handleBangumiFetch(Map<String, Object> payload) {
        String query = (String) payload.get("query");
        String targetType = (String) payload.getOrDefault("targetType", "anime");
        String externalId = (String) payload.get("externalId");
        log.info("Bangumi fetch: '{}' extId={} → type={}", query, externalId, targetType);
        List<Item> items = bangumiService.searchSubjects(query, targetType);
        if (externalId != null && !externalId.isBlank()) {
            String eid = externalId;
            items = items.stream().filter(i -> eid.equals(i.getExternalId())).toList();
        }
        saveItems(items, targetType, "Bangumi");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TMDB_TV)
    public void handleTMDBTVFetch(Map<String, Object> payload) {
        String query = (String) payload.get("query");
        String targetType = (String) payload.getOrDefault("targetType", "anime");
        String externalId = (String) payload.get("externalId");
        log.info("TMDB TV fetch: '{}' extId={} → type={}", query, externalId, targetType);
        List<Item> items;
        if (externalId != null && !externalId.isBlank()) {
            try { items = List.of(tmdbService.fetchTVDetail(Integer.parseInt(externalId))); }
            catch (Exception e) { log.error("TMDB TV fetch by id failed: {}", e.getMessage()); items = List.of(); }
        } else {
            items = tmdbService.searchTV(query);
        }
        saveItems(items, targetType, "TMDB-TV");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ITUNES)
    public void handleItunesFetch(Map<String, Object> payload) {
        String query = (String) payload.get("query");
        String targetType = (String) payload.getOrDefault("targetType", "music");
        String externalId = (String) payload.get("externalId");
        log.info("iTunes fetch: '{}' extId={} → type={}", query, externalId, targetType);
        List<Item> items = itunesService.searchAlbums(query, targetType);
        if (externalId != null && !externalId.isBlank()) {
            String eid = externalId;
            items = items.stream().filter(i -> eid.equals(i.getExternalId())).toList();
        }
        saveItems(items, targetType, "iTunes");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_IGDB)
    public void handleIGDBFetch(Map<String, Object> payload) {
        String endpoint = (String) payload.getOrDefault("endpoint", "search");
        String query = (String) payload.get("query");
        Integer limit = payload.get("limit") != null ? ((Number) payload.get("limit")).intValue() : 10;
        String targetType = (String) payload.getOrDefault("targetType", "game");
        String externalId = (String) payload.get("externalId");
        log.info("IGDB fetch: '{}' endpoint={} extId={} → type={}", query, endpoint, externalId, targetType);
        List<Item> items;
        if (externalId != null && !externalId.isBlank()) {
            try { items = List.of(igdbService.fetchGameById(Integer.parseInt(externalId))); }
            catch (Exception e) { log.error("IGDB fetch by id failed: {}", e.getMessage()); items = List.of(); }
        } else {
            switch (endpoint) {
                case "popular": items = igdbService.fetchPopularGames(limit); break;
                case "recent":  items = igdbService.fetchRecentGames(limit); break;
                default:        items = igdbService.searchGames(query != null ? query : "", limit);
            }
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
            Map<String, Object> info = objectMapper
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
        } catch (Exception e) {
            log.warn("自动打标签失败，itemId={} title={}: {}", item.getId(), item.getTitle(), e.getMessage());
        }
    }
}
