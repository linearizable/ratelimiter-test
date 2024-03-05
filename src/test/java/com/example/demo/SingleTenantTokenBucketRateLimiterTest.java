package com.example.demo;

import com.example.demo.ratelimit.tokenbucket.SingleTenantTokenBucketRateLimiter;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class SingleTenantTokenBucketRateLimiterTest {
    @Test
    void testCorrectNumberOfRequestsAllowed() {
        SingleTenantTokenBucketRateLimiter rateLimiter = new SingleTenantTokenBucketRateLimiter(3, 2);

        List<TestPair> testData = List.of(
                TestPair.of(1709537921L, true), //allowed
                TestPair.of(1709537921L, true), //allowed
                TestPair.of(1709537921L, true), //allowed
                TestPair.of(1709537921L, true), //allowed - burst
                TestPair.of(1709537921L, true), //allowed - burst
                TestPair.of(1709537921L, false), //blocked
                TestPair.of(1709537921L, false), //blocked
                TestPair.of(1709537922L, true), //allowed
                TestPair.of(1709537922L, true), //allowed
                TestPair.of(1709537922L, true), //allowed
                TestPair.of(1709537922L, false), //block - no burst
                TestPair.of(1709537922L, false), //block - no burst
                TestPair.of(1709537923L, true), //allowed
                TestPair.of(1709537923L, true), //allowed
                TestPair.of(1709537924L, true), //allowed
                TestPair.of(1709537924L, true), //allowed
                TestPair.of(1709537924L, true), //allowed
                TestPair.of(1709537924L, true), //allowed - one burst allowed as only 2 tokens consumed from last window
                TestPair.of(1709537924L, false), //blocked
                TestPair.of(1709537925L, true), //allowed
                TestPair.of(1709537926L, true), //allowed
                TestPair.of(1709537927L, true), //allowed
                TestPair.of(1709537927L, true), //allowed
                TestPair.of(1709537927L, true), //allowed
                TestPair.of(1709537927L, true), //allowed - burst
                TestPair.of(1709537927L, true), //allowed - burst
                TestPair.of(1709537927L, false) //blocked
        );

        List<Boolean> result = testData.stream().map(testPair -> rateLimiter.isRequestAllowedAtTime(testPair.requestTime())).toList();
        List<Boolean> expectedAllowed = testData.stream().map(TestPair::response).toList();

        assertThat(result).isEqualTo(expectedAllowed);
    }
}
