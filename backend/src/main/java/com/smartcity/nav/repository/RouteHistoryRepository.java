package com.smartcity.nav.repository;

import com.smartcity.nav.entity.RouteHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RouteHistoryRepository extends JpaRepository<RouteHistory, Long> {
    List<RouteHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}
