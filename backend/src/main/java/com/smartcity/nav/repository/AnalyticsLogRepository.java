package com.smartcity.nav.repository;

import com.smartcity.nav.entity.AnalyticsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyticsLogRepository extends JpaRepository<AnalyticsLog, Long> {
    // TODO: aggregate queries for popular routes / most visited places
}
