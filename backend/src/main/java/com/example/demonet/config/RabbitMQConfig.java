package com.example.demonet.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_STEAM = "demonet.fetch.steam";
    public static final String QUEUE_TMDB = "demonet.fetch.tmdb";
    public static final String QUEUE_ANILIST = "demonet.fetch.anilist";
    public static final String QUEUE_BANGUMI = "demonet.fetch.bangumi";
    public static final String QUEUE_TMDB_TV = "demonet.fetch.tmdb-tv";
    public static final String QUEUE_ITUNES = "demonet.fetch.itunes";
    public static final String QUEUE_IGDB = "demonet.fetch.igdb";

    public static final String EXCHANGE_DLX = "demonet.dlx";
    public static final String QUEUE_DLQ = "demonet.dlq";

    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    @Bean
    public Queue dlq() {
        return new Queue(QUEUE_DLQ, true);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlq()).to(dlx()).with("dead.letter");
    }

    private Map<String, Object> dlxArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EXCHANGE_DLX);
        args.put("x-dead-letter-routing-key", "dead.letter");
        return args;
    }

    @Bean
    public Queue steamFetchQueue() {
        return new Queue(QUEUE_STEAM, true, false, false, dlxArgs());
    }

    @Bean
    public Queue tmdbFetchQueue() {
        return new Queue(QUEUE_TMDB, true, false, false, dlxArgs());
    }

    @Bean
    public Queue anilistFetchQueue() {
        return new Queue(QUEUE_ANILIST, true, false, false, dlxArgs());
    }

    @Bean
    public Queue bangumiFetchQueue() {
        return new Queue(QUEUE_BANGUMI, true, false, false, dlxArgs());
    }

    @Bean
    public Queue tmdbTVFetchQueue() {
        return new Queue(QUEUE_TMDB_TV, true, false, false, dlxArgs());
    }

    @Bean
    public Queue itunesFetchQueue() {
        return new Queue(QUEUE_ITUNES, true, false, false, dlxArgs());
    }

    @Bean
    public Queue igdbFetchQueue() {
        return new Queue(QUEUE_IGDB, true, false, false, dlxArgs());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
