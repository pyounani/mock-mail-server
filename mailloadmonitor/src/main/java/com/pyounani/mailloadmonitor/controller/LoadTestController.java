package com.pyounani.mailloadmonitor.controller;

import com.pyounani.mailloadmonitor.domain.LoadTestResult;
import com.pyounani.mailloadmonitor.domain.LoadTest;
import com.pyounani.mailloadmonitor.repository.LoadTestResultRepository;
import com.pyounani.mailloadmonitor.repository.LoadTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LoadTestController {

    private final LoadTestResultRepository loadTestResultRepository;
    private final LoadTestRepository loadTestRepository;
    private final TaskExecutor taskExecutor;

    /**
     * 부하 발생
     * @param vUsers        동시사용자수
     * @param interval      요청 간격
     * @param loop          반복수
     * @param description   테스트 설명
     */
    @PostMapping("/generate/load-test")
    public void generateLoad(@RequestParam int vUsers,
                             @RequestParam long interval,
                             @RequestParam int loop,
                             @RequestParam String description) {

        // 부하 테스트 세션에 대한 정보 저장
        LoadTest loadTest = loadTestRepository.save(LoadTest.builder()
                .testParams(String.format("vUsers=%d, interval=%d, loop=%d", vUsers, interval, loop))
                .startTime(LocalDateTime.now())
                .description(description)
                .build());

        // 실제 부하 발생
        for (int loopIdx = 0; loopIdx < loop; loopIdx++) {
            generateVUsersLoadAsync(vUsers, loadTest, loopIdx);
            sleep(interval);
        }
    }

    private CompletableFuture<Void> generateVUsersLoadAsync(int vUsers, LoadTest loadTest, int loopIdx) {
        List<CompletableFuture<Void>> asyncTasks = new ArrayList<>();

        for (int i = 0; i < vUsers; i++) {
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                LoadTestResult loadTestResult = loadTestResultRepository.save(
                        loadTestResultRepository.save(LoadTestResult.builder()
                                .loadTest(loadTest)
                                .loopIdx(loopIdx)
                                .requestTime(LocalDateTime.now()) // 요청 시간 저장
                                .build())
                );

                // 인증코드 요청 API
                requestVerificationCode(loadTestResult.getId().toString() + "@example.com");

            }, taskExecutor);

            asyncTasks.add(task);
        }

        return CompletableFuture.allOf(asyncTasks.toArray(new CompletableFuture[0]));
    }

    private void requestVerificationCode(String email) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8080").build();

        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/emails/verification-requests")
                            .queryParam("email", email)
                            .build())
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException e) {
            log.error("Error occurred : {}", e);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("sleep {}", e);
        }
    }
}
