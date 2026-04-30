package com.hng.nameprocessing.services;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static class Bucket {
        int count;
        long windowStart;
    }

    private final Map<String, Bucket> store = new ConcurrentHashMap<>();

    public synchronized boolean isAllowed(String key, int limitPerMinute) {
        long now = System.currentTimeMillis();

        Bucket bucket = store.computeIfAbsent(key, k -> {
            Bucket b = new Bucket();
            b.windowStart = now;
            return b;
        });

        // reset window after 1 minute
        if (now - bucket.windowStart > 60_000) {
            bucket.count = 0;
            bucket.windowStart = now;
        }

        bucket.count++;

        return bucket.count <= limitPerMinute;
    }
}