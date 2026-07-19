package com.smartcity.nav.controller;

import com.smartcity.nav.dto.RouteHistoryDTO;
import com.smartcity.nav.entity.User;
import com.smartcity.nav.exception.ResourceNotFoundException;
import com.smartcity.nav.repository.UserRepository;
import com.smartcity.nav.service.RouteHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class RouteHistoryController {

    private final RouteHistoryService routeHistoryService;
    private final UserRepository userRepository;

    /** GET /api/history - requires authentication (see SecurityConfig: anyRequest().authenticated()). */
    @GetMapping
    public List<RouteHistoryDTO> getMyHistory(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return routeHistoryService.getHistoryForUser(user.getId());
    }
}
