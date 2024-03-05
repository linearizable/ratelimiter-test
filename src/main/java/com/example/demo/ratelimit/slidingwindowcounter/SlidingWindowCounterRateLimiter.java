package com.example.demo.ratelimit.slidingwindowcounter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A sliding window counter rate limiter
 * Allows X requests per second per tenant (tenant is app specific - can be userId, IP address, etc.)
 * This class maintains a rate limiter instance (@SingleTenantSlidingWindowCounterRateLimiter) for each tenant
 * to avoid synchronization between tenants
 * It also has an in-built cleanup mechanism which removes tenants who haven't sent a request in more than 2 seconds
 * to keep memory usage in check
 */
public class SlidingWindowCounterRateLimiter {

    private final Map<String, SingleTenantSlidingWindowCounterRateLimiter> rateLimiters =
            new ConcurrentHashMap<>();

    private final int requestsAllowed;

    private final static long OLD_TENANT_CLEANUP_WINDOW_MS = 2000L;

    public SlidingWindowCounterRateLimiter(int requestsAllowed) {
        this.requestsAllowed = requestsAllowed;
        ScheduledExecutorService cleanupWorker = Executors.newScheduledThreadPool(1);
        cleanupWorker.scheduleAtFixedRate(this::cleanupOldTenants, 0, 1, TimeUnit.SECONDS);
    }

    public boolean isAllowed(String tenantId) {
        long requestTime = System.currentTimeMillis();
        return getTenantRateLimiterInstance(tenantId).isRequestAllowedAtTime(requestTime);
    }


    private void cleanupOldTenants() {
        long threshold = System.currentTimeMillis() - OLD_TENANT_CLEANUP_WINDOW_MS;
        this.rateLimiters.entrySet().removeIf(entry -> entry.getValue().getLastRequestTime() < threshold);
    }

    private SingleTenantSlidingWindowCounterRateLimiter getTenantRateLimiterInstance(String tenantId) {
        return rateLimiters.computeIfAbsent(tenantId, k -> new SingleTenantSlidingWindowCounterRateLimiter(requestsAllowed));
    }

    public int getCurrentlyRecordedTenants() {
        return this.rateLimiters.size();
    }
}
