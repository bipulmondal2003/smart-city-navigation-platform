package com.smartcity.nav.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Maps to the `locations` table.
 * `latitude`/`longitude` are duplicated from the spatial `geo_point`
 * column for convenience when the DSA engine builds graph nodes.
 */
@Entity
@Table(name = "locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType type;

    @Column(nullable = false, precision = 10, scale = 7)
    private java.math.BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private java.math.BigDecimal longitude;

    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum LocationType {
        HOSPITAL, RESTAURANT, AIRPORT, HOTEL, SCHOOL, BUS_STOP, ATM, GENERIC
    }
}
