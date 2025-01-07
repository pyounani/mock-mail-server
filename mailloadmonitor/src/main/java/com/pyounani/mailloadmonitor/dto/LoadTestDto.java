package com.pyounani.mailloadmonitor.dto;

import com.pyounani.mailloadmonitor.domain.LoadTest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LoadTestDto {

    private Long id;
    private String description;
    private String testParams;
    private LocalDateTime startTime;

    public static LoadTestDto from(LoadTest loadTest) {
        return LoadTestDto.builder()
                .id(loadTest.getId())
                .description(loadTest.getDescription())
                .testParams(loadTest.getTestParams())
                .startTime(loadTest.getStartTime())
                .build();
    }
}
