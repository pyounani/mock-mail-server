package com.pyounani.mailloadmonitor.controller;

import com.pyounani.mailloadmonitor.domain.LoadTestResult;
import com.pyounani.mailloadmonitor.domain.LoadTest;
import com.pyounani.mailloadmonitor.dto.LoadTestResultSummaryDto;
import com.pyounani.mailloadmonitor.dto.ProcessTimeDto;
import com.pyounani.mailloadmonitor.dto.TpsDto;
import com.pyounani.mailloadmonitor.repository.LoadTestResultRepository;
import com.pyounani.mailloadmonitor.repository.LoadTestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
                .testParams(String.format("vUsers=%d/interval=%d/loop=%d", vUsers, interval, loop))
                .startTime(LocalDateTime.now())
                .description(description)
                .build());

        // 실제 부하 발생
        for (int loopIdx = 0; loopIdx < loop; loopIdx++) {
            generateVUsersLoadAsync(vUsers, loadTest, loopIdx);
            sleep(interval);
        }
    }

    /**
     * 테스트 결과로 시간대별로 TPS로 집계합니다.
     * @param id
     * @return
     */
    @GetMapping("/load-tests/{id}/tps")
    public List<TpsDto> getLoadTestTps(@PathVariable Long id) {
        LoadTest loadTest = getLoadTest(id);
        List<LoadTestResult> results = loadTestResultRepository.findByLoadTestOrderByRequestTime(loadTest);

        Map<Long, Double> tps = new TreeMap<>();
        for (LoadTestResult result : results) {
            if (result.getFinishTime() == null) {
                continue;
            }
            long elapsedTime = Duration.between(results.get(0).getRequestTime(), result.getRequestTime())
                    .getSeconds();
            long processingTime = Duration.between(result.getRequestTime(), result.getFinishTime())
                    .toMillis();

            // TPS 계산
            tps.merge(elapsedTime, 1.0 / processingTime, Double::sum);
        }
        return tps.entrySet().stream()
                .map(entry -> new TpsDto(entry.getKey(), entry.getValue() * 1000))
                .collect(Collectors.toList());
    }

    /**
     * 테스트 결과로 요청순서별 처리시간을 집계합니다.
     * @param id
     * @return
     */
    @GetMapping("/load-tests/{id}/process-time")
    public List<ProcessTimeDto> getLoadTestProcessTime(@PathVariable Long id) {
        LoadTest loadTest = getLoadTest(id);
        List<LoadTestResult> results = loadTestResultRepository.findByLoadTestOrderByRequestTime(loadTest);
        AtomicLong index = new AtomicLong(0);
        return results.stream().map(result -> ProcessTimeDto.from(result, index.getAndIncrement()))
                .collect(Collectors.toList());
    }

    private LoadTest getLoadTest(Long id) {
        return loadTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("결과가 없습니다."));
    }

    /**
     * 부하테스트 결과 삭제
     * @param id
     */
    @Transactional
    @DeleteMapping("/load-tests/{id}")
    public void deleteLoadTest(@PathVariable Long id) {
        loadTestResultRepository.deleteByLoadTest_Id(id);
        loadTestRepository.deleteById(id);
    }


    List<LoadTestResultSummaryDto> summaryResult(LoadTest loadTest) {
        Map<Integer, List<LoadTestResult>> groupedByLoopIdx = loadTest.getLoadTestResults().stream()
                .collect(Collectors.groupingBy(LoadTestResult::getLoopIdx));

        return groupedByLoopIdx.entrySet().stream()
                .map(entry -> LoadTestResultSummaryDto.from(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
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
                requestVerificationCode(loadTestResult.getId().toString() + "@example.com", loadTestResult.getId());

            }, taskExecutor);

            asyncTasks.add(task);
        }

        return CompletableFuture.allOf(asyncTasks.toArray(new CompletableFuture[0]));
    }

    private void requestVerificationCode(String email, Long loadTestResultId) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8080").build();

        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/emails/verification-requests")
                            .queryParam("email", email)
                            .queryParam("loadTestResultId", loadTestResultId)
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
