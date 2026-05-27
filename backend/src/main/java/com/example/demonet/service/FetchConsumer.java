package com.example.demonet.service;

import com.example.demonet.config.RabbitMQConfig;
import com.example.demonet.entity.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchConsumer {

    private final SteamService steamService;
    private final TMDBService tmdbService;
    private final ItemService itemService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_STEAM)
    public void handleSteamFetch(List<Long> appIds) {
        log.info("Steam fetch task received: {} appIds", appIds.size());
        List<Item> items = steamService.fetchByAppIds(appIds);
        for (Item item : items) {
            itemService.createItem(item);
        }
        log.info("Steam fetch completed: {} items imported", items.size());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TMDB)
    public void handleTMDBFetch(String query) {
        log.info("TMDB fetch task received: query='{}'", query);
        List<Item> items = tmdbService.searchMovies(query);
        for (Item item : items) {
            itemService.createItem(item);
        }
        log.info("TMDB fetch completed: {} items imported", items.size());
    }
}
