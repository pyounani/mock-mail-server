package com.pyounani.mockmail.service;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Bucket4JMailRateLimiter implements MailRateLimiter {

    private static final int LIMIT_COUNT = 5; // 초당 최대 5건의 요청을 허용
    private ConcurrentHashMap<String, Bucket> bucketMap = new ConcurrentHashMap<>();

    /**
     * 이메일별로 요청 처리 가능 여부를 판단
     * @param email 이메일 주소
     * @return 요청을 처리할 수 있으면 true, 그렇지 않으면 false를 반환
     */
    @Override
    public boolean isAllowed(String email) {
        Bucket bucket = bucketMap.computeIfAbsent(email, newBucket -> createNewBucket());
        return bucket.tryConsume(1);
    }

    /**
     * 새로운 버킷을 생성합니다.
     * @return 생성된 Bucket 객체
     */
    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(LIMIT_COUNT).refillGreedy(LIMIT_COUNT, Duration.ofSeconds(1)))
                .build();
    }
}
