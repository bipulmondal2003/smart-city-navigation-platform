package com.smartcity.nav.repository;

import com.smartcity.nav.entity.Traffic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrafficRepository extends JpaRepository<Traffic, Long> {
    List<Traffic> findByRoadIdOrderByCreatedAtDesc(Long roadId);
}
