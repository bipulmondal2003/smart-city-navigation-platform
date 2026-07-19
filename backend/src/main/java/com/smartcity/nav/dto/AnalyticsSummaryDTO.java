package com.smartcity.nav.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnalyticsSummaryDTO {
    private int totalRoutesPlanned;
    private List<CountEntry> topRoutes;
    private List<CountEntry> topVisitedPlaces;
    private BigDecimal averageDistanceKm;
    private BigDecimal averageTimeMin;
    private List<CountEntry> topCongestedRoads; // "count" here is congestion % (180 = 1.8x)

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CountEntry {
        private String label;
        private int count;
    }
}
