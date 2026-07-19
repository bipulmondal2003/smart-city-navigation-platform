package com.smartcity.nav.service;

import com.smartcity.nav.algorithm.AStar;
import com.smartcity.nav.algorithm.Node;
import com.smartcity.nav.dto.LocationDTO;
import com.smartcity.nav.entity.Location;
import com.smartcity.nav.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Finds the nearest N locations (optionally filtered by type) to a given
 * lat/lng point.
 *
 * Distance is computed with the same haversine formula used as A*'s
 * heuristic (algorithm.AStar#haversine), then results are sorted with a
 * simple comparator sort — for a city-sized dataset (dozens to low
 * hundreds of locations) this beats the overhead of a heap for a one-shot
 * top-N query; the heap-based top-N approach is used instead in the
 * Analytics module, where it's a better fit (see AnalyticsService).
 */
@Service
@RequiredArgsConstructor
public class NearbyService {

    private final LocationRepository locationRepository;

    public List<LocationDTO> findNearby(double latitude, double longitude, String type, int limit) {
        Node origin = new Node(-1L, latitude, longitude);

        List<Location> candidates = (type == null || type.isBlank())
                ? locationRepository.findAll()
                : locationRepository.findByType(Location.LocationType.valueOf(type.toUpperCase()));

        return candidates.stream()
                .map(loc -> new Object() {
                    final Location location = loc;
                    final double distanceKm = AStar.haversine(
                            origin, new Node(loc.getId(), loc.getLatitude().doubleValue(), loc.getLongitude().doubleValue()));
                })
                .sorted(Comparator.comparingDouble(o -> o.distanceKm))
                .limit(limit)
                .map(o -> toDto(o.location, o.distanceKm))
                .toList();
    }

    private LocationDTO toDto(Location loc, double distanceKm) {
        return LocationDTO.builder()
                .id(loc.getId())
                .name(loc.getName())
                .type(loc.getType().name())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .address(loc.getAddress())
                .distanceKm(Math.round(distanceKm * 1000.0) / 1000.0)
                .build();
    }
}
