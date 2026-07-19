package com.smartcity.nav.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationDTO {
    private Long id;
    private String name;
    private String type;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private Double distanceKm; // populated only by /api/nearby; null elsewhere
}
