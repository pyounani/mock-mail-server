package com.pyounani.mailloadmonitor.repository;

import com.pyounani.mailloadmonitor.domain.LoadTestExecutionResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadTestExecutionResultRepository extends JpaRepository<LoadTestExecutionResult, Long> {
}
