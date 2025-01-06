package com.pyounani.mailloadmonitor.repository;

import com.pyounani.mailloadmonitor.domain.LoadTest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadTestRepository extends JpaRepository<LoadTest, Long> {
}
