package com.smartcity.nav.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RouteRequestDTO {
    private Long fromLocationId;
    private Long toLocationId;
    private String algorithm; // "dijkstra" | "astar"
}
