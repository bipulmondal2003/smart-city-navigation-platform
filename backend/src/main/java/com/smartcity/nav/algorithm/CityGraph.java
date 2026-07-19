package com.smartcity.nav.algorithm;

import java.util.*;

/**
 * The city road network as an in-memory graph, stored as an adjacency list:
 * nodeId -> list of outgoing edges. This is the core data structure the
 * whole DSA engine operates on.
 *
 * Space complexity: O(V + E)
 * addNode / addEdge: O(1) amortized
 * getNeighbors: O(1) lookup + O(degree) to iterate
 */
public class CityGraph {

    private final Map<Long, Node> nodes = new HashMap<>();
    private final Map<Long, List<Edge>> adjacencyList = new HashMap<>();

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        adjacencyList.putIfAbsent(node.getId(), new ArrayList<>());
    }

    public void addEdge(Long fromId, Edge edge) {
        adjacencyList.computeIfAbsent(fromId, k -> new ArrayList<>()).add(edge);
    }

    public Node getNode(Long id) {
        return nodes.get(id);
    }

    public boolean hasNode(Long id) {
        return nodes.containsKey(id);
    }

    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    public List<Edge> getNeighbors(Long nodeId) {
        return adjacencyList.getOrDefault(nodeId, Collections.emptyList());
    }

    public int nodeCount() {
        return nodes.size();
    }

    /** Clears the graph — used before a full rebuild from the database. */
    public void clear() {
        nodes.clear();
        adjacencyList.clear();
    }

    /**
     * Finds the specific Edge object for a road, so TrafficService can mutate
     * its weight/closed state in place without rebuilding the whole graph.
     */
    public Optional<Edge> findEdgeByRoadId(Long roadId) {
        return adjacencyList.values().stream()
                .flatMap(List::stream)
                .filter(e -> e.getRoadId().equals(roadId))
                .findFirst();
    }
}
