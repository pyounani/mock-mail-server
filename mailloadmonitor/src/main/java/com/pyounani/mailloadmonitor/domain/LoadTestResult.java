package com.pyounani.mailloadmonitor.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 각각의 부하 테스트들에 대한 실행 결과
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadTestExecutionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private LoadTestSession loadTestSession; // 실행결과가 속한 부하 테스트 세션

    private Integer loopIdx; // 실행 반복 횟수

    private LocalDateTime requestTime; // 요청이 시작된 시간

    private LocalDateTime finishTime; // 요청이 끝난 시간
}
