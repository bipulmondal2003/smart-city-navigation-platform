package com.smartcity.nav.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maps to the `roads` table. Represents a directed/bidirectional
 * edge between two Locations in the city graph.
 */
@Entity
@Table(name = "roads")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Road {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id", nullable = false)
    private Location fromLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id", nullable = false)
    private Location toLocation;

    @Column(name = "base_distance_km", nullable = false)
    private BigDecimal baseDistanceKm;

    @Column(name = "base_speed_kmph")
    private BigDecimal baseSpeedKmph;

    @Column(name = "current_weight", nullable = false)
    private BigDecimal currentWeight;

    @Column(name = "is_bidirectional")
    private Boolean isBidirectional;

    @Column(name = "is_closed")
    private Boolean isClosed;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
