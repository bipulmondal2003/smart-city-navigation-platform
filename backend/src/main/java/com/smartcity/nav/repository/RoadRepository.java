package com.smartcity.nav.repository;

import com.smartcity.nav.entity.Road;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoadRepository extends JpaRepository<Road, Long> {
    List<Road> findByFromLocationId(Long fromLocationId);
    List<Road> findByToLocationId(Long toLocationId);
    List<Road> findByIsClosedFalse();
}
