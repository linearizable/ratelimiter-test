package com.example.demo;

import com.example.demo.ratelimit.slidingwindowcounter.SingleTenantSlidingWindowCounterRateLimiter;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

public class SingleTenantSlidingWindowCounterRateLimiterTest {
    @Test
    void correctNumberOfRequestsAllowed() {
        // 5 requests allowed in a 1-second window
        SingleTenantSlidingWindowCounterRateLimiter rateLimiter = new SingleTenantSlidingWindowCounterRateLimiter(5);

        List<TestPair> testData = List.of(
                TestPair.of(1709537921000L, true), //allowed
                TestPair.of(1709537921015L, true), //allowed
                TestPair.of(1709537921015L, true), //allowed
                TestPair.of(1709537921120L, true), //allowed
                TestPair.of(1709537921350L, true), //allowed
                TestPair.of(1709537921780L, false), //blocked
                TestPair.of(1709537921960L, false), //blocked
                TestPair.of(1709537922010L, true), //allowed
                TestPair.of(1709537922012L, false), //blocked
                TestPair.of(1709537922015L, false), //blocked
                TestPair.of(1709537922110L, true), //allowed
                TestPair.of(1709537922530L, true) //allowed
        );

        List<Boolean> result = testData.stream().map(testPair -> rateLimiter.isRequestAllowedAtTime(testPair.requestTime())).toList();
        List<Boolean> expectedAllowed = testData.stream().map(TestPair::response).toList();

        assertThat(result).isEqualTo(expectedAllowed);
    }
}
