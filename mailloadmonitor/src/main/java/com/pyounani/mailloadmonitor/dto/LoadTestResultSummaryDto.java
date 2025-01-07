package com.pyounani.mailloadmonitor.dto;

import com.pyounani.mailloadmonitor.domain.LoadTestResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor @Builder
@Getter
public class LoadTestResultSummaryDto {
  private Integer loopIdx;
  private LocalDateTime earliestRequestTime;
  private Double averageProcessTime;
  private Long maxProcessTime;
  private Integer vUsers;
  private Double tps;   // vUsers / averageProcessTime

  public static LoadTestResultSummaryDto from(Integer loopIdx, List<LoadTestResult> loadTestResults) {
    if (loadTestResults == null || loadTestResults.isEmpty()) {
      return zero();
    }

    // vUsers는 그룹의 크기
    int vUsers = loadTestResults.size();

    // 가장 이른 requestTime
    LocalDateTime earliestRequestTime = loadTestResults.stream()
        .map(LoadTestResult::getRequestTime)
        .min(LocalDateTime::compareTo)
        .orElse(null);

    // 평균 처리 시간
    long totalProcessTime = loadTestResults.stream()
        .filter(result -> result.getFinishTime() != null)
        .mapToLong(result -> Duration.between(result.getRequestTime(), result.getFinishTime()).toMillis())
        .sum();
    double averageProcessTime = (double)totalProcessTime / vUsers;

    // 최대 처리 시간
    long maxProcessTime = loadTestResults.stream()
        .filter(result -> result.getFinishTime() != null)
        .mapToLong(result -> Duration.between(result.getRequestTime(), result.getFinishTime()).toMillis())
        .max()
        .orElse(0L);

    // TPS 계산
    double tps = vUsers / (double) (averageProcessTime / 1000);

    // DTO 생성
    return LoadTestResultSummaryDto.builder()
        .loopIdx(loopIdx)
        .earliestRequestTime(earliestRequestTime)
        .averageProcessTime(averageProcessTime)
        .maxProcessTime(maxProcessTime)
        .vUsers(vUsers)
        .tps(tps)
        .build();
  }

  public static LoadTestResultSummaryDto zero() {
    return LoadTestResultSummaryDto.builder()
        .loopIdx(0)
        .earliestRequestTime(null)
        .averageProcessTime(0d)
        .maxProcessTime(0L)
        .vUsers(0)
        .tps(0d)
        .build();
  }
}
