package com.smartcity.nav.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NearbyRequestDTO {
    private Double latitude;
    private Double longitude;
    private String type;
    private Integer limit;
}
