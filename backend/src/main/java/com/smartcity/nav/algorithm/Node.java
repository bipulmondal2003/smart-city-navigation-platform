package com.smartcity.nav.algorithm;

import lombok.Getter;

/**
 * A vertex in the city road-network graph.
 * Wraps just what the algorithms need (id + coordinates for the A* heuristic)
 * so the DSA engine doesn't depend on JPA entities directly.
 */
@Getter
public class Node {

    private final Long id;
    private final double latitude;
    private final double longitude;

    public Node(Long id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        return id.equals(((Node) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
