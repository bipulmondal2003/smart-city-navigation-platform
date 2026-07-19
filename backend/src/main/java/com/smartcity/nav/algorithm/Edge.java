package com.smartcity.nav.algorithm;

import lombok.Getter;

/**
 * A directed edge (road) from one Node to another.
 * `weight` is the value the algorithms actually traverse with — it starts
 * equal to the road's base distance but TrafficService can mutate it live,
 * which is what makes the traffic-simulation feature reroute in real time.
 */
@Getter
public class Edge {

    private final Long roadId;
    private final Long toNodeId;
    private double weight;       // km-equivalent cost, adjusted by traffic
    private boolean closed;

    public Edge(Long roadId, Long toNodeId, double weight, boolean closed) {
        this.roadId = roadId;
        this.toNodeId = toNodeId;
        this.weight = weight;
        this.closed = closed;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
