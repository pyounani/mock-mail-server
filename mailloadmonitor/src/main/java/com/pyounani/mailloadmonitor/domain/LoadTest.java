package com.pyounani.mailloadmonitor.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 부하 테스트 세션
 * 하나의 테스트 세션은 부하 테스트를 실행하기 위한 설정과 메타데이터를 저장
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description; // 테스트 세션에 대한 설명

    private String testParams; // 테스트에 사용된 파라미터

    private LocalDateTime startTime; // 테스트가 시작된 시간

    @OneToMany(mappedBy = "loadTest")
    private List<LoadTestResult> loadTestResults; // 테스트 세션에서 발생한 여러 실행 결과들의 목록
}
