package com.ardaslegends.service.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
@EnableAsync
public class ScheduleConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
