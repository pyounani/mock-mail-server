package com.pyounani.mailloadmonitor.repository;

import com.pyounani.mailloadmonitor.domain.LoadTest;
import com.pyounani.mailloadmonitor.domain.LoadTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoadTestResultRepository extends JpaRepository<LoadTestResult, Long> {
    void deleteByLoadTest_Id(Long loadTestId);
    List<LoadTestResult> findByLoadTestOrderByRequestTime(LoadTest loadTest);
}
