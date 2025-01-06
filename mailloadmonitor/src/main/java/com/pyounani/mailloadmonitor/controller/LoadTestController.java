package com.pyounani.mailloadmonitor.controller;

import com.pyounani.mailloadmonitor.domain.LoadTestExecutionResult;
import com.pyounani.mailloadmonitor.domain.LoadTestSession;
import com.pyounani.mailloadmonitor.repository.LoadTestExecutionResultRepository;
import com.pyounani.mailloadmonitor.repository.LoadTestSessionRepository;
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

    private final LoadTestExecutionResultRepository loadTestExecutionResultRepository;
    private final LoadTestSessionRepository loadTestSessionRepository;
    private final TaskExecutor taskExecutor;

    /**
     * 부하 발생
     * @param vUsers        동시사용자수
     * @param interval      요청 간격
     * @param loop          반복수
     * @param description   테스트 설명
     */
    @PostMapping("/load-test/generate")
    public void generateLoad(@RequestParam int vUsers,
                             @RequestParam long interval,
                             @RequestParam int loop,
                             @RequestParam String description) {

        // 부하 테스트 세션에 대한 정보 저장
        LoadTestSession loadTestSession = loadTestSessionRepository.save(LoadTestSession.builder()
                .testParams(String.format("vUsers=%d, interval=%d, loop=%d", vUsers, interval, loop))
                .startTime(LocalDateTime.now())
                .description(description)
                .build());

        // 실제 부하 발생
        for (int loopIdx = 0; loopIdx < loop; loopIdx++) {
            generateVUsersLoadAsync(vUsers, loadTestSession, loopIdx);
            sleep(interval);
        }
    }

    private CompletableFuture<Void> generateVUsersLoadAsync(int vUsers, LoadTestSession loadTestSession, int loopIdx) {
        List<CompletableFuture<Void>> asyncTasks = new ArrayList<>();

        for (int i = 0; i < vUsers; i++) {
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                LoadTestExecutionResult loadTestExecutionResult = loadTestExecutionResultRepository.save(
                        loadTestExecutionResultRepository.save(LoadTestExecutionResult.builder()
                                .loadTestSession(loadTestSession)
                                .loopIdx(loopIdx)
                                .requestTime(LocalDateTime.now())
                                .build())
                );

                // 이메일 발송
//                sendMail(loadTestExecutionResult.getId());

            }, taskExecutor);

            asyncTasks.add(task);
        }

        return CompletableFuture.allOf(asyncTasks.toArray(new CompletableFuture[0]));
    }

    private void sendMail(Long loadTestExecutionResultId) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:56876").build();

        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("loadTestExecutionResultId", loadTestExecutionResultId)
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
