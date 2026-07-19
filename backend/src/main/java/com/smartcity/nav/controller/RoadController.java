package com.smartcity.nav.controller;

import com.smartcity.nav.dto.RoadDTO;
import com.smartcity.nav.entity.Location;
import com.smartcity.nav.entity.Road;
import com.smartcity.nav.exception.ResourceNotFoundException;
import com.smartcity.nav.repository.LocationRepository;
import com.smartcity.nav.repository.RoadRepository;
import com.smartcity.nav.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roads")
@RequiredArgsConstructor
public class RoadController {

    private final RoadRepository roadRepository;
    private final LocationRepository locationRepository;
    private final GraphService graphService;

    /** GET /api/roads - public. */
    @GetMapping
    public List<RoadDTO> getAllRoads() {
        return roadRepository.findAll().stream().map(this::toDto).toList();
    }

    /** POST /api/roads - admin only (see SecurityConfig). Adds a road and rebuilds the live graph. */
    @PostMapping
    public RoadDTO addRoad(@RequestBody RoadDTO dto) {
        Location from = locationRepository.findById(dto.getFromLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + dto.getFromLocationId()));
        Location to = locationRepository.findById(dto.getToLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + dto.getToLocationId()));

        Road road = Road.builder()
                .fromLocation(from)
                .toLocation(to)
                .baseDistanceKm(dto.getBaseDistanceKm())
                .baseSpeedKmph(java.math.BigDecimal.valueOf(40))
                .currentWeight(dto.getBaseDistanceKm())
                .isBidirectional(dto.getIsBidirectional() != null ? dto.getIsBidirectional() : true)
                .isClosed(false)
                .build();

        Road saved = roadRepository.save(road);
        graphService.rebuildGraph(); // simplest correct way to add a brand-new edge to the live graph
        return toDto(saved);
    }

    /** PATCH /api/roads/{id}/close - admin only. Marks a road closed without deleting it. */
    @PatchMapping("/{id}/close")
    public Map<String, String> closeRoad(@PathVariable Long id) {
        Road road = roadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Road not found: " + id));
        road.setIsClosed(true);
        roadRepository.save(road);
        graphService.updateEdgeState(id, road.getCurrentWeight().doubleValue(), true);
        return Map.of("status", "Road " + id + " closed");
    }

    /** PATCH /api/roads/{id}/reopen - admin only. */
    @PatchMapping("/{id}/reopen")
    public Map<String, String> reopenRoad(@PathVariable Long id) {
        Road road = roadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Road not found: " + id));
        road.setIsClosed(false);
        roadRepository.save(road);
        graphService.updateEdgeState(id, road.getCurrentWeight().doubleValue(), false);
        return Map.of("status", "Road " + id + " reopened");
    }

    /** DELETE /api/roads/{id} - admin only. */
    @DeleteMapping("/{id}")
    public Map<String, String> deleteRoad(@PathVariable Long id) {
        if (!roadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Road not found: " + id);
        }
        roadRepository.deleteById(id);
        graphService.rebuildGraph();
        return Map.of("status", "Road " + id + " deleted");
    }

    private RoadDTO toDto(Road road) {
        return RoadDTO.builder()
                .id(road.getId())
                .fromLocationId(road.getFromLocation().getId())
                .toLocationId(road.getToLocation().getId())
                .baseDistanceKm(road.getBaseDistanceKm())
                .currentWeight(road.getCurrentWeight() != null
                        ? road.getCurrentWeight()
                        : road.getBaseDistanceKm().setScale(3, RoundingMode.HALF_UP))
                .isBidirectional(road.getIsBidirectional())
                .isClosed(road.getIsClosed())
                .build();
    }
}
