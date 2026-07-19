package com.smartcity.nav.service;

import com.smartcity.nav.algorithm.AStar;
import com.smartcity.nav.algorithm.Dijkstra;
import com.smartcity.nav.algorithm.GraphTraversal;
import com.smartcity.nav.dto.LocationDTO;
import com.smartcity.nav.dto.RouteRequestDTO;
import com.smartcity.nav.dto.RouteResponseDTO;
import com.smartcity.nav.entity.Location;
import com.smartcity.nav.exception.BadRequestException;
import com.smartcity.nav.exception.ResourceNotFoundException;
import com.smartcity.nav.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private static final double ASSUMED_AVG_SPEED_KMPH = 40.0;

    private final GraphService graphService;
    private final LocationRepository locationRepository;

    public RouteResponseDTO findRoute(RouteRequestDTO request) {
        if (request.getFromLocationId() == null || request.getToLocationId() == null) {
            throw new BadRequestException("fromLocationId and toLocationId are required");
        }
        if (request.getFromLocationId().equals(request.getToLocationId())) {
            throw new BadRequestException("fromLocationId and toLocationId must be different");
        }

        var graph = graphService.getGraph();
        boolean useAStar = "astar".equalsIgnoreCase(request.getAlgorithm());

        Dijkstra.Result result = useAStar
                ? AStar.findShortestPath(graph, request.getFromLocationId(), request.getToLocationId())
                : Dijkstra.findShortestPath(graph, request.getFromLocationId(), request.getToLocationId());

        if (!result.reachable) {
            throw new ResourceNotFoundException(
                    "No route found between location " + request.getFromLocationId()
                            + " and " + request.getToLocationId());
        }

        BigDecimal distanceKm = BigDecimal.valueOf(result.totalDistance).setScale(3, RoundingMode.HALF_UP);
        BigDecimal timeMin = BigDecimal.valueOf(result.totalDistance / ASSUMED_AVG_SPEED_KMPH * 60)
                .setScale(2, RoundingMode.HALF_UP);

        return RouteResponseDTO.builder()
                .pathLocationIds(result.path)
                .totalDistanceKm(distanceKm)
                .estimatedTimeMin(timeMin)
                .algorithmUsed(useAStar ? "ASTAR" : "DIJKSTRA")
                .build();
    }

    /**
     * BFS-powered "what's reachable within N hops" - a separate, unweighted
     * traversal from the distance-based Dijkstra/A* routes above. Demonstrates
     * GraphTraversal.bfsWithinHops end-to-end via a real endpoint.
     */
    public List<LocationDTO> findReachableWithinHops(Long fromLocationId, int maxHops) {
        if (!graphService.getGraph().hasNode(fromLocationId)) {
            throw new ResourceNotFoundException("Location not found: " + fromLocationId);
        }

        List<Long> reachableIds = GraphTraversal.bfsWithinHops(graphService.getGraph(), fromLocationId, maxHops);

        return reachableIds.stream()
                .map(locationRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(this::toDto)
                .toList();
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
