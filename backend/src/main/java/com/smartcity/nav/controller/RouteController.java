package com.smartcity.nav.controller;

import com.smartcity.nav.dto.RouteRequestDTO;
import com.smartcity.nav.dto.RouteResponseDTO;
import com.smartcity.nav.entity.User;
import com.smartcity.nav.repository.UserRepository;
import com.smartcity.nav.service.RouteHistoryService;
import com.smartcity.nav.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final RouteHistoryService routeHistoryService;
    private final UserRepository userRepository;

    /**
     * GET /api/route?from={locationId}&to={locationId}&algorithm=dijkstra|astar
     * Public endpoint (see SecurityConfig) - anyone can plan a route without logging in.
     * If the caller IS authenticated (JwtAuthFilter populated the SecurityContext),
     * the route is also saved to their history.
     */
    @GetMapping
    public RouteResponseDTO getRoute(
            @RequestParam("from") Long fromLocationId,
            @RequestParam("to") Long toLocationId,
            @RequestParam(value = "algorithm", required = false, defaultValue = "dijkstra") String algorithm
    ) {
        RouteRequestDTO request = new RouteRequestDTO(fromLocationId, toLocationId, algorithm);
        RouteResponseDTO response = routeService.findRoute(request);

        currentUser().ifPresent(user ->
                routeHistoryService.record(user, fromLocationId, toLocationId, response));

        return response;
    }

    private Optional<User> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        String email = auth.getName();
        return userRepository.findByEmail(email);
    }

    /**
     * GET /api/route/reachable?from={locationId}&maxHops={default 2}
     * BFS-based: returns every location reachable within N road-hops,
     * ignoring distance/weight entirely (unlike Dijkstra/A* above).
     */
    @GetMapping("/reachable")
    public java.util.List<com.smartcity.nav.dto.LocationDTO> getReachable(
            @RequestParam("from") Long fromLocationId,
            @RequestParam(value = "maxHops", required = false, defaultValue = "2") int maxHops
    ) {
        return routeService.findReachableWithinHops(fromLocationId, maxHops);
    }
}
