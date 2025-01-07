package com.pyounani.mailloadmonitor.dto;

import com.pyounani.mailloadmonitor.domain.LoadTestResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

@AllArgsConstructor
@Getter
public class ProcessTimeDto {
  private Long index;
  private Double second;

  public static ProcessTimeDto from(LoadTestResult loadTestResult, Long index) {
    double processTimeInSeconds = (loadTestResult.getFinishTime() == null)
        ? 0d
        : Duration.between(loadTestResult.getRequestTime(), loadTestResult.getFinishTime()).toMillis() / 1000.0;

    return new ProcessTimeDto(index, processTimeInSeconds);
  }
}
