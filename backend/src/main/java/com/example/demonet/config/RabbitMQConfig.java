package com.example.demonet.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_STEAM = "demonet.fetch.steam";
    public static final String QUEUE_TMDB = "demonet.fetch.tmdb";
    public static final String QUEUE_ANILIST = "demonet.fetch.anilist";
    public static final String QUEUE_BANGUMI = "demonet.fetch.bangumi";
    public static final String QUEUE_TMDB_TV = "demonet.fetch.tmdb-tv";

    @Bean
    public Queue steamFetchQueue() {
        return new Queue(QUEUE_STEAM, true);
    }

    @Bean
    public Queue tmdbFetchQueue() {
        return new Queue(QUEUE_TMDB, true);
    }

    @Bean
    public Queue anilistFetchQueue() {
        return new Queue(QUEUE_ANILIST, true);
    }

    @Bean
    public Queue bangumiFetchQueue() {
        return new Queue(QUEUE_BANGUMI, true);
    }

    @Bean
    public Queue tmdbTVFetchQueue() {
        return new Queue(QUEUE_TMDB_TV, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
