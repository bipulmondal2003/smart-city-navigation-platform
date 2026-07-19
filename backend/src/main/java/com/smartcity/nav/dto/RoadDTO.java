package com.smartcity.nav.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoadDTO {
    private Long id;
    private Long fromLocationId;
    private Long toLocationId;
    private BigDecimal baseDistanceKm;
    private BigDecimal currentWeight;
    private Boolean isBidirectional;
    private Boolean isClosed;
}
