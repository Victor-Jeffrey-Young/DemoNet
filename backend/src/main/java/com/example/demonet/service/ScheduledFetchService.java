package com.example.demonet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledFetchService {

    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyFetch() {
        log.info("Scheduled daily fetch triggered (2:00 AM) — ready for Steam/TMDB integration");
    }
}
