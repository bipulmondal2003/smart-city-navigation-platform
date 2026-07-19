package com.smartcity.nav.controller;

import com.smartcity.nav.dto.LocationDTO;
import com.smartcity.nav.entity.Location;
import com.smartcity.nav.exception.ResourceNotFoundException;
import com.smartcity.nav.repository.LocationRepository;
import com.smartcity.nav.service.GraphService;
import com.smartcity.nav.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationRepository locationRepository;
    private final GraphService graphService;
    private final SearchService searchService;

    /** GET /api/locations - public, used to populate the map + route-planner dropdowns. */
    @GetMapping
    public List<LocationDTO> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * POST /api/locations - admin only (see SecurityConfig).
     * geo_point is populated automatically by a DB trigger from lat/lng
     * (see database/schema.sql), so the entity/JPA layer never has to touch it.
     */
    @PostMapping
    public LocationDTO addLocation(@RequestBody LocationDTO dto) {
        Location location = Location.builder()
                .name(dto.getName())
                .type(Location.LocationType.valueOf(dto.getType().toUpperCase()))
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .address(dto.getAddress())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Location saved = locationRepository.save(location);

        // A new location changes both the graph's node set and the search index.
        graphService.rebuildGraph();
        searchService.rebuild();

        return toDto(saved);
    }

    /** DELETE /api/locations/{id} - admin only. */
    @DeleteMapping("/{id}")
    public Map<String, String> deleteLocation(@PathVariable Long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Location not found: " + id);
        }
        locationRepository.deleteById(id);
        graphService.rebuildGraph();
        searchService.rebuild();
        return Map.of("status", "Location " + id + " deleted");
    }

    private LocationDTO toDto(Location loc) {
        return LocationDTO.builder()
                .id(loc.getId())
                .name(loc.getName())
                .type(loc.getType().name())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .address(loc.getAddress())
                .build();
    }
}
