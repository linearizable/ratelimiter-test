package com.example.demo.ratelimit.slidingwindowcounter;

import java.util.*;

/**
 * This class maintains sliding window counters for a single tenant
 * and answers whether current request for its tenant is allowed or not
 * This class is thread-safe so multiple concurrent requests for its tenant are allowed
 */
public class SingleTenantSlidingWindowCounterRateLimiter {
    // No. of requests allowed per second
    private final int requestsAllowed;

    // Track historical request counts for the tenant in form of
    // timestamp (in ms) => request count
    private final Map<Long, Integer> requestTracker;

    // Track last request time to facilitate cleanup
    private long lastRequestTime;

    public SingleTenantSlidingWindowCounterRateLimiter(int requestsAllowed) {
        this.requestsAllowed = requestsAllowed;
        this.requestTracker = new HashMap<>();
    }

    public synchronized boolean isRequestAllowedAtTime(long requestTime) {
        this.lastRequestTime = requestTime;

        // Aggregate request count from last 1-second window
        long lookbackWindow = requestTime - 1000L;
        int requestCount = getRequestCountSince(lookbackWindow);
        if (requestCount >= requestsAllowed) {
            return false;
        }

        this.requestTracker.merge(requestTime, 1, Integer::sum);
        return true;
    }

    /**
     * Aggregate request count starting from time given till current time (last 1 second in our case)
     * This method has built-in purge functionality to keep memory in check
     * i.e. it automatically removes entries older than given time as they are not required for
     * rate limit calculations
     */
    private int getRequestCountSince(long time) {
        Iterator<Map.Entry<Long, Integer>> iterator = this.requestTracker.entrySet().iterator();

        int requestCount = 0;
        while (iterator.hasNext()) {
            Map.Entry<Long, Integer> e = iterator.next();

            if (e.getKey() >= time) {
                requestCount += e.getValue();
            }
            else {
                iterator.remove();
            }
        }

        return  requestCount;
    }

    public synchronized long getLastRequestTime() {
        return this.lastRequestTime;
    }
}
