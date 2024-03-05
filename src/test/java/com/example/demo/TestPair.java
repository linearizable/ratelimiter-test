package com.example.demo;

public record TestPair(long requestTime, boolean response) {
    public static TestPair of(long requestTime, boolean response) {
        return new TestPair(requestTime, response);
    }
}
