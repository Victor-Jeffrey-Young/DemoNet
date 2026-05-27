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

    @Bean
    public Queue steamFetchQueue() {
        return new Queue(QUEUE_STEAM, true);
    }

    @Bean
    public Queue tmdbFetchQueue() {
        return new Queue(QUEUE_TMDB, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
