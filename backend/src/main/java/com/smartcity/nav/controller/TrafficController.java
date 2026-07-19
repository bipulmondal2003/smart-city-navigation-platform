package com.smartcity.nav.controller;

import com.smartcity.nav.dto.RoadDTO;
import com.smartcity.nav.dto.TrafficUpdateDTO;
import com.smartcity.nav.entity.Road;
import com.smartcity.nav.entity.User;
import com.smartcity.nav.exception.ResourceNotFoundException;
import com.smartcity.nav.repository.RoadRepository;
import com.smartcity.nav.repository.UserRepository;
import com.smartcity.nav.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficService trafficService;
    private final RoadRepository roadRepository;
    private final UserRepository userRepository;

    /** GET /api/traffic/current - public. Current live weight/closed state of every road. */
    @GetMapping("/current")
    public List<RoadDTO> getCurrentTraffic() {
        return roadRepository.findAll().stream().map(this::toDto).toList();
    }

    /** POST /api/traffic - admin only. Apply a specific congestion level to one road. */
    @PostMapping
    public Map<String, String> updateTraffic(
            @RequestBody TrafficUpdateDTO dto,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User admin = currentAdmin(principal);
        trafficService.applyTrafficUpdate(dto, admin);
        return Map.of("status", "Traffic updated for road " + dto.getRoadId());
    }

    /** POST /api/traffic/simulate?count=5 - admin only. Randomly congest N roads at once. */
    @PostMapping("/simulate")
    public Map<String, Object> simulate(@RequestParam(value = "count", defaultValue = "5") int count) {
        int affected = trafficService.simulateRandomTraffic(count);
        return Map.of("status", "Traffic simulated", "roadsAffected", affected);
    }

    /** POST /api/traffic/reset - admin only. Clears all congestion back to base weights. */
    @PostMapping("/reset")
    public Map<String, String> reset() {
        trafficService.resetTraffic();
        return Map.of("status", "Traffic reset to baseline");
    }

    private User currentAdmin(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private RoadDTO toDto(Road road) {
        return RoadDTO.builder()
                .id(road.getId())
                .fromLocationId(road.getFromLocation().getId())
                .toLocationId(road.getToLocation().getId())
                .baseDistanceKm(road.getBaseDistanceKm())
                .currentWeight(road.getCurrentWeight())
                .isBidirectional(road.getIsBidirectional())
                .isClosed(road.getIsClosed())
                .build();
    }
}
