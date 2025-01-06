package com.pyounani.mockmail.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class MockMailControllerTest {

    @Autowired
    private MockMailController mockMailController;

    private AtomicLong successfulRequests = new AtomicLong(0L);
    private AtomicLong failedRequests = new AtomicLong(0L);
    List<CompletableFuture<Void>> asyncTasks = new ArrayList<>();

    @Test
    @DisplayName("초당 5건 이하는 모의 메일 서버에서 제한되지 않음")
    void shouldNotLimitRequestsBelowThreshold() {
        int requestCount = 5;
        String email = "test@example.com";

        for (int i = 0; i < requestCount; i++) {
            sendEmailAsynchronously(email);
        }

        // 모든 작업이 완료될 때까지 기다림
        CompletableFuture.allOf(asyncTasks.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> null)
                .join();

        assertThat(failedRequests.longValue()).isZero();
        log.info("Successful requests: {}", successfulRequests.get());
        log.info("Failed requests: {}", failedRequests.get());
    }

    @Test
    @DisplayName("초당 5건 초과는 모의 메일 서버에서 제한됨")
    void shouldLimitRequestsAboveThreshold() {
        int requestCount = 6;
        String email = "test@example.com";

        for (int i = 0; i < requestCount; i++) {
            sendEmailAsynchronously(email);
        }

        // 모든 작업이 완료될 때까지 기다림
        CompletableFuture.allOf(asyncTasks.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> null)
                .join();

        assertThat(failedRequests.longValue()).isEqualTo(1);
        log.info("Successful requests: {}", successfulRequests.get());
        log.info("Failed requests: {}", failedRequests.get());
    }

    private void sendEmailAsynchronously(String email) {
        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
            ResponseEntity<Void> response = mockMailController.sendMail(email);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(response.getStatusCode().toString());
            }
        }).whenComplete((res, ex) -> {
            if (ex != null) {
                log.error("Error occurred : {}", ex);
                failedRequests.incrementAndGet();
            } else {
                successfulRequests.incrementAndGet();
            }
        });

        asyncTasks.add(task);
    }

}