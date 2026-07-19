package com.smartcity.nav.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcity.nav.dto.RouteHistoryDTO;
import com.smartcity.nav.dto.RouteResponseDTO;
import com.smartcity.nav.entity.Location;
import com.smartcity.nav.entity.RouteHistory;
import com.smartcity.nav.entity.User;
import com.smartcity.nav.exception.ResourceNotFoundException;
import com.smartcity.nav.repository.LocationRepository;
import com.smartcity.nav.repository.RouteHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteHistoryService {

    private final RouteHistoryRepository routeHistoryRepository;
    private final LocationRepository locationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void record(User user, Long fromLocationId, Long toLocationId, RouteResponseDTO route) {
        try {
            Location from = locationRepository.findById(fromLocationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + fromLocationId));
            Location to = locationRepository.findById(toLocationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + toLocationId));

            RouteHistory entry = RouteHistory.builder()
                    .user(user)
                    .fromLocation(from)
                    .toLocation(to)
                    .algorithmUsed(RouteHistory.AlgorithmUsed.valueOf(route.getAlgorithmUsed()))
                    .totalDistanceKm(route.getTotalDistanceKm())
                    .estimatedTimeMin(route.getEstimatedTimeMin())
                    .pathJson(objectMapper.writeValueAsString(route.getPathLocationIds()))
                    .createdAt(LocalDateTime.now())
                    .build();

            routeHistoryRepository.save(entry);
        } catch (Exception e) {
            // History logging must never break the actual route response for the user.
            log.warn("Failed to record route history: {}", e.getMessage());
        }
    }

    public List<RouteHistoryDTO> getHistoryForUser(Long userId) {
        return routeHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    private RouteHistoryDTO toDto(RouteHistory h) {
        return RouteHistoryDTO.builder()
                .id(h.getId())
                .fromLocationName(h.getFromLocation().getName())
                .toLocationName(h.getToLocation().getName())
                .algorithmUsed(h.getAlgorithmUsed().name())
                .totalDistanceKm(h.getTotalDistanceKm())
                .estimatedTimeMin(h.getEstimatedTimeMin())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
