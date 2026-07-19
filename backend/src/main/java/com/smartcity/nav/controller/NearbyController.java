package com.smartcity.nav.controller;

import com.smartcity.nav.dto.LocationDTO;
import com.smartcity.nav.service.NearbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nearby")
@RequiredArgsConstructor
public class NearbyController {

    private final NearbyService nearbyService;

    /**
     * GET /api/nearby?lat={}&lng={}&type={optional}&limit={default 5}
     * Public endpoint. `type` matches Location.LocationType (HOSPITAL, ATM, etc); omit for all types.
     */
    @GetMapping
    public List<LocationDTO> getNearby(
            @RequestParam("lat") Double latitude,
            @RequestParam("lng") Double longitude,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "limit", required = false, defaultValue = "5") Integer limit
    ) {
        return nearbyService.findNearby(latitude, longitude, type, limit);
    }
}
