package com.smartcity.nav.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Maps to the `roles` table. Values: CITIZEN, ADMIN.
 */
@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;
}
