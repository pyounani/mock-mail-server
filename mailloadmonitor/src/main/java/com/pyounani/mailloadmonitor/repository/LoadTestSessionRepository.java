package com.pyounani.mailloadmonitor.repository;

import com.pyounani.mailloadmonitor.domain.LoadTestSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadTestSessionRepository extends JpaRepository<LoadTestSession, Long> {
}
