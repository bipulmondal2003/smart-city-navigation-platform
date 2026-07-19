package com.smartcity.nav.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "traffic")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Traffic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "road_id", nullable = false)
    private Road road;

    @Enumerated(EnumType.STRING)
    @Column(name = "traffic_level", nullable = false)
    private TrafficLevel trafficLevel;

    @Column(name = "congestion_factor", nullable = false)
    private BigDecimal congestionFactor;

    @Column(name = "current_speed_kmph")
    private BigDecimal currentSpeedKmph;

    @Column(name = "is_road_closed")
    private Boolean isRoadClosed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by")
    private User triggeredBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public enum TrafficLevel {
        LOW, MODERATE, HEAVY, SEVERE
    }
}
