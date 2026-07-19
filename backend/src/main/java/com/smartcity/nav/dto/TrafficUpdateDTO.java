package com.smartcity.nav.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TrafficUpdateDTO {
    private Long roadId;
    private String trafficLevel;
    private Double congestionFactor;
    private Boolean isRoadClosed;
}
