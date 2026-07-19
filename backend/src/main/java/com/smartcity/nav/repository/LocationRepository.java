package com.smartcity.nav.repository;

import com.smartcity.nav.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByType(Location.LocationType type);
    List<Location> findByNameContainingIgnoreCase(String namePrefix);

    // TODO: native @Query using ST_Distance_Sphere(geo_point, POINT(:lng,:lat))
    // for the Nearby Services module.
}
