package com.smartcity.nav.service;

import com.smartcity.nav.dto.TrafficUpdateDTO;
import com.smartcity.nav.entity.Road;
import com.smartcity.nav.entity.Traffic;
import com.smartcity.nav.entity.User;
import com.smartcity.nav.exception.ResourceNotFoundException;
import com.smartcity.nav.repository.RoadRepository;
import com.smartcity.nav.repository.TrafficRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Applies traffic conditions to roads. This is what makes the platform
 * "smart": a congestion update here mutates the live in-memory graph edge
 * weight, so the very next /api/route call for that road returns a
 * different (rerouted) path — no server restart or manual graph rebuild needed.
 */
@Service
@RequiredArgsConstructor
public class TrafficService {

    private final RoadRepository roadRepository;
    private final TrafficRepository trafficRepository;
    private final GraphService graphService;

    private final Random random = new Random();

    @Transactional
    public void applyTrafficUpdate(TrafficUpdateDTO dto, User admin) {
        Road road = roadRepository.findById(dto.getRoadId())
                .orElseThrow(() -> new ResourceNotFoundException("Road not found: " + dto.getRoadId()));

        Traffic.TrafficLevel level = Traffic.TrafficLevel.valueOf(dto.getTrafficLevel().toUpperCase());
        double congestionFactor = dto.getCongestionFactor() != null ? dto.getCongestionFactor() : congestionFactorFor(level);
        boolean closed = Boolean.TRUE.equals(dto.getIsRoadClosed());

        double newWeight = road.getBaseDistanceKm().doubleValue() * congestionFactor;

        // Persist the road's current live weight/closed flag.
        road.setCurrentWeight(BigDecimal.valueOf(newWeight).setScale(3, RoundingMode.HALF_UP));
        road.setIsClosed(closed);
        roadRepository.save(road);

        // Log the traffic event for history/analytics.
        Traffic traffic = Traffic.builder()
                .road(road)
                .trafficLevel(level)
                .congestionFactor(BigDecimal.valueOf(congestionFactor))
                .currentSpeedKmph(road.getBaseSpeedKmph())
                .isRoadClosed(closed)
                .triggeredBy(admin)
                .createdAt(LocalDateTime.now())
                .build();
        trafficRepository.save(traffic);

        // Push the change straight into the live graph used by Dijkstra/A*.
        graphService.updateEdgeState(road.getId(), newWeight, closed);
    }

    /**
     * Picks a random subset of roads and applies random congestion levels —
     * the "Simulate Traffic" button. Demonstrates the graph reacting to
     * bulk dynamic edge-weight changes.
     */
    @Transactional
    public int simulateRandomTraffic(int roadsToAffect) {
        List<Road> allRoads = roadRepository.findAll();
        if (allRoads.isEmpty()) return 0;

        int affected = Math.min(roadsToAffect, allRoads.size());
        Traffic.TrafficLevel[] levels = Traffic.TrafficLevel.values();

        for (int i = 0; i < affected; i++) {
            Road road = allRoads.get(random.nextInt(allRoads.size()));
            Traffic.TrafficLevel level = levels[random.nextInt(levels.length)];
            double factor = congestionFactorFor(level);

            double newWeight = road.getBaseDistanceKm().doubleValue() * factor;
            road.setCurrentWeight(BigDecimal.valueOf(newWeight).setScale(3, RoundingMode.HALF_UP));
            roadRepository.save(road);

            graphService.updateEdgeState(road.getId(), newWeight, Boolean.TRUE.equals(road.getIsClosed()));

            Traffic traffic = Traffic.builder()
                    .road(road)
                    .trafficLevel(level)
                    .congestionFactor(BigDecimal.valueOf(factor))
                    .currentSpeedKmph(road.getBaseSpeedKmph())
                    .isRoadClosed(road.getIsClosed())
                    .createdAt(LocalDateTime.now())
                    .build();
            trafficRepository.save(traffic);
        }

        return affected;
    }

    /** Reverts every road to its original, uncongested weight. */
    @Transactional
    public void resetTraffic() {
        List<Road> allRoads = roadRepository.findAll();
        for (Road road : allRoads) {
            road.setCurrentWeight(road.getBaseDistanceKm());
            road.setIsClosed(false);
            roadRepository.save(road);
            graphService.updateEdgeState(road.getId(), road.getBaseDistanceKm().doubleValue(), false);
        }
    }

    private double congestionFactorFor(Traffic.TrafficLevel level) {
        return switch (level) {
            case LOW -> 1.0;
            case MODERATE -> 1.3;
            case HEAVY -> 1.8;
            case SEVERE -> 2.5;
        };
    }
}
