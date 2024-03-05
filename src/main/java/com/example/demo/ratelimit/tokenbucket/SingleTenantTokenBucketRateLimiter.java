package com.example.demo.ratelimit.tokenbucket;

/**
 * This class maintains token bucket for a single tenant
 * and answers whether current request for its tenant is allowed or not
 * This class is thread-safe so multiple concurrent requests for its tenant are allowed
 */
public class SingleTenantTokenBucketRateLimiter {

    private final int bucketSize;
    private final int tokenFillRate;

    private int tokensAvailable;

    private long lastRequestTime;

    public SingleTenantTokenBucketRateLimiter(int requestsAllowed, int burst) {
        this.bucketSize = requestsAllowed + burst;
        this.tokenFillRate = requestsAllowed;
        this.tokensAvailable = this.bucketSize;
    }

    public synchronized boolean isRequestAllowedAtTime(long requestTime) {

        if (this.lastRequestTime == 0) {
            this.lastRequestTime = requestTime;
            this.tokensAvailable--;
            return true;
        }

        long timeElapsedSinceLastRequest = requestTime - this.lastRequestTime;
        int tokensToFill = (int) timeElapsedSinceLastRequest * tokenFillRate;

        this.tokensAvailable = Math.min(this.tokensAvailable + tokensToFill, this.bucketSize);

        if (this.tokensAvailable > 0) {
            this.lastRequestTime = requestTime;
            this.tokensAvailable--;
            return true;
        }

        return false;
    }

    public synchronized long getLastRequestTime() {
        return this.lastRequestTime;
    }
}
