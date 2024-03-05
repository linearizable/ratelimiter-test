package com.example.demo.ratelimit.tokenbucket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A token bucket rate limiter
 * Allows X requests per second per tenant with some burst
 * This class maintains a rate limiter instance (@SingleTenantTokenBucketRateLimiter) for each tenant
 * to avoid synchronization between tenants
 * It also has an in-built cleanup mechanism which removes tenants who haven't sent a request in more than 20 seconds
 * to keep memory usage in check
 */
public class TokenBucketRateLimiter {

    private final Map<String, SingleTenantTokenBucketRateLimiter> rateLimiters =
            new ConcurrentHashMap<>();

    private final int requestsAllowed;
    private final int burst;

    private final static long OLD_TENANT_CLEANUP_WINDOW_SECONDS = 20L;

    public TokenBucketRateLimiter(int requestsAllowed, int burst) {
        this.requestsAllowed = requestsAllowed;
        this.burst = burst;
        ScheduledExecutorService cleanupWorker = Executors.newScheduledThreadPool(1);
        cleanupWorker.scheduleAtFixedRate(this::cleanupOldTenants, 1, 1, TimeUnit.SECONDS);
    }

    public boolean isAllowed(String tenantId) {
        long requestTime = System.currentTimeMillis()/1000;
        return getTenantRateLimiterInstance(tenantId).isRequestAllowedAtTime(requestTime);
    }


    private void cleanupOldTenants() {
        long threshold = System.currentTimeMillis()/1000 - OLD_TENANT_CLEANUP_WINDOW_SECONDS;
        this.rateLimiters.entrySet().removeIf(entry -> entry.getValue().getLastRequestTime() < threshold);
    }

    private SingleTenantTokenBucketRateLimiter getTenantRateLimiterInstance(String tenantId) {
        return this.rateLimiters.computeIfAbsent(tenantId, k ->
                new SingleTenantTokenBucketRateLimiter(this.requestsAllowed, this.burst));
    }
}
