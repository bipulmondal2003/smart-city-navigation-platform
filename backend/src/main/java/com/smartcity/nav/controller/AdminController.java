package com.smartcity.nav.controller;

import com.smartcity.nav.entity.Location;
import com.smartcity.nav.entity.Road;
import com.smartcity.nav.entity.User;
import com.smartcity.nav.repository.LocationRepository;
import com.smartcity.nav.repository.RoadRepository;
import com.smartcity.nav.repository.UserRepository;
import com.smartcity.nav.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * All endpoints here require ROLE_ADMIN (enforced in SecurityConfig via
 * the /api/admin/** path pattern). The actual CRUD for roads/locations/traffic
 * lives in their own controllers (RoadController, LocationController,
 * TrafficController) which are ALSO admin-gated for their write methods -
 * this controller covers the remaining admin-only views: user management
 * and an at-a-glance dashboard summary.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final RoadRepository roadRepository;
    private final GraphService graphService;

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::userSummary)
                .collect(Collectors.toList());
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardSummary() {
        List<Location> locations = locationRepository.findAll();
        List<Road> roads = roadRepository.findAll();
        long closedRoads = roads.stream().filter(r -> Boolean.TRUE.equals(r.getIsClosed())).count();

        return Map.of(
                "totalUsers", userRepository.count(),
                "totalLocations", locations.size(),
                "totalRoads", roads.size(),
                "closedRoads", closedRoads,
                "graphNodeCount", graphService.getGraph().nodeCount()
        );
    }

    /** POST /api/admin/graph/rebuild - manually force a full graph rebuild from the DB. */
    @PostMapping("/graph/rebuild")
    public Map<String, String> rebuildGraph() {
        graphService.rebuildGraph();
        return Map.of("status", "Graph rebuilt: " + graphService.getGraph().nodeCount() + " nodes");
    }

    private Map<String, Object> userSummary(User user) {
        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().getName(),
                "isActive", Boolean.TRUE.equals(user.getIsActive())
        );
    }
}
