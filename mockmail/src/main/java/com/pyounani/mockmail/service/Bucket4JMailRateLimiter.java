package com.pyounani.mockmail.service;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Bucket4JMailRateLimiter implements MailRateLimiter {

    private static final int LIMIT_COUNT = 10;
    private ConcurrentHashMap<String, Bucket> bucketMap = new ConcurrentHashMap<>();

    @Override
    public boolean isAllowed(String account) {
        Bucket bucket = bucketMap.computeIfAbsent(account, newBucket -> createNewBucket());
        return bucket.tryConsume(1);
    }

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(LIMIT_COUNT).refillGreedy(LIMIT_COUNT, Duration.ofSeconds(1)))
                .build();
    }
}
