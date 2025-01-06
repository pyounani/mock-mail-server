package com.pyounani.mailloadmonitor.repository;

import com.pyounani.mailloadmonitor.domain.LoadTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadTestResultRepository extends JpaRepository<LoadTestResult, Long> {
}
