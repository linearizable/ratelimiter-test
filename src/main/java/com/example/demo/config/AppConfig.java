package com.example.demo.config;

import com.example.demo.ratelimit.slidingwindowcounter.SlidingWindowCounterRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    private final static int SLIDING_WINDOW_COUNTER_RATELIMITER_REQUESTS_ALLOWED = 10;

    @Bean
    public SlidingWindowCounterRateLimiter getSlidingWindowCounterRateLimiter() {
        return new SlidingWindowCounterRateLimiter(SLIDING_WINDOW_COUNTER_RATELIMITER_REQUESTS_ALLOWED);
    }
}
