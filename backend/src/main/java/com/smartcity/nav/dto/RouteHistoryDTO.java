package com.smartcity.nav.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RouteHistoryDTO {
    private Long id;
    private String fromLocationName;
    private String toLocationName;
    private String algorithmUsed;
    private BigDecimal totalDistanceKm;
    private BigDecimal estimatedTimeMin;
    private LocalDateTime createdAt;
}
