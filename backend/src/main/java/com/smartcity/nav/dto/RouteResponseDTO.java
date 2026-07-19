package com.smartcity.nav.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RouteResponseDTO {
    private List<Long> pathLocationIds;
    private BigDecimal totalDistanceKm;
    private BigDecimal estimatedTimeMin;
    private String algorithmUsed;
}
