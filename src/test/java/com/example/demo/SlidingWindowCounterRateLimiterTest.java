package com.example.demo;

import com.example.demo.ratelimit.slidingwindowcounter.SlidingWindowCounterRateLimiter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;

public class SlidingWindowCounterRateLimiterTest {

    @Test
    void testCorrectNumberOfRequestsAllowedWhenCalledConcurrently() throws InterruptedException, ExecutionException {

        record RateLimitCallResult(String tenantId, boolean result) {
        }

        SlidingWindowCounterRateLimiter rateLimiter = new SlidingWindowCounterRateLimiter(5);

        // Simulate 50 concurrent requests from 5 different tenants - 10 requests for each tenant
        List<Callable<RateLimitCallResult>> tasks = new ArrayList<>();
        Map<String, Long> expectedResult = new HashMap<>();
        for (int i=1;i<=5;i++) {
            String tenantId = "tenant-"+i;
            expectedResult.put(tenantId, 5L);
            for (int j = 1; j <= 10; j++) {
                tasks.add(() -> new RateLimitCallResult(tenantId, rateLimiter.isAllowed(tenantId)));
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<RateLimitCallResult>> results = executor.invokeAll(tasks); // Start all tasks and collect their Future objects

        List<RateLimitCallResult> finalResults = new ArrayList<>();
        for (Future<RateLimitCallResult> result : results) {
            finalResults.add(result.get());
        }

        executor.shutdown();

        Map<String, Long> blockedPerTenant = finalResults.stream().
                        filter(x -> !x.result()).
                        collect(groupingBy(RateLimitCallResult::tenantId, Collectors.counting()));

        assertThat(blockedPerTenant).usingRecursiveComparison().isEqualTo(expectedResult);
    }

    @Test
    void testCleaningUpOldTenants() throws InterruptedException {
        SlidingWindowCounterRateLimiter rateLimiter = new SlidingWindowCounterRateLimiter(5);
        rateLimiter.isAllowed("1");
        assertThat(rateLimiter.getCurrentlyRecordedTenants()).isEqualTo(1);
        rateLimiter.isAllowed("1");
        assertThat(rateLimiter.getCurrentlyRecordedTenants()).isEqualTo(1);
        rateLimiter.isAllowed("2");
        assertThat(rateLimiter.getCurrentlyRecordedTenants()).isEqualTo(2);
        Thread.sleep(1000L);
        rateLimiter.isAllowed("2");
        assertThat(rateLimiter.getCurrentlyRecordedTenants()).isEqualTo(2);
        Thread.sleep(2100L);
        assertThat(rateLimiter.getCurrentlyRecordedTenants()).isEqualTo(1);
        Thread.sleep(1000L);
        assertThat(rateLimiter.getCurrentlyRecordedTenants()).isEqualTo(0);
    }
}
