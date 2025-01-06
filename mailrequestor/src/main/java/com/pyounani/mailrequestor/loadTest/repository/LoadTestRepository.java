package com.pyounani.mailrequestor.loadTest.repository;

import com.pyounani.mailrequestor.loadTest.domain.LoadTest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadTestRepository extends JpaRepository<LoadTest, Long> {
}
