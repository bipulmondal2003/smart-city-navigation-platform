package com.smartcity.nav.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RouteHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id", nullable = false)
    private Location fromLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id", nullable = false)
    private Location toLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm_used", nullable = false)
    private AlgorithmUsed algorithmUsed;

    @Column(name = "total_distance_km")
    private BigDecimal totalDistanceKm;

    @Column(name = "estimated_time_min")
    private BigDecimal estimatedTimeMin;

    @Column(name = "path_json", columnDefinition = "json")
    private String pathJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum AlgorithmUsed {
        DIJKSTRA, ASTAR, BFS
    }
}
