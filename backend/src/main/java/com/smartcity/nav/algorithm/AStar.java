package com.smartcity.nav.algorithm;

import java.util.*;

/**
 * A* search over the CityGraph.
 *
 * Like Dijkstra, but instead of always expanding the globally-closest node,
 * it expands the node with the lowest f(n) = g(n) + h(n), where:
 *   g(n) = actual distance traveled so far from the source
 *   h(n) = straight-line (haversine) distance from n to the target — an
 *          admissible heuristic since it never overestimates real road distance
 *
 * This lets A* skip exploring nodes that are "in the wrong direction",
 * which is why it's typically faster than Dijkstra in practice on road
 * networks, while still guaranteeing the shortest path (because the
 * heuristic never overestimates).
 *
 * Time complexity: O((V + E) log V) worst case — same bound as Dijkstra,
 *                   but visits far fewer nodes in practice with a good heuristic.
 * Space complexity: O(V + E)
 */
public class AStar {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public static Dijkstra.Result findShortestPath(CityGraph graph, Long sourceId, Long targetId) {
        if (!graph.hasNode(sourceId) || !graph.hasNode(targetId)) {
            return new Dijkstra.Result(Collections.emptyList(), Double.POSITIVE_INFINITY, false);
        }

        Node target = graph.getNode(targetId);

        Map<Long, Double> gScore = new HashMap<>();   // actual distance from source
        Map<Long, Long> predecessor = new HashMap<>();
        Set<Long> visited = new HashSet<>();

        record HeapEntry(Long nodeId, double fScore) {}
        PriorityQueue<HeapEntry> openSet = new PriorityQueue<>(Comparator.comparingDouble(HeapEntry::fScore));

        for (Node node : graph.getAllNodes()) {
            gScore.put(node.getId(), Double.POSITIVE_INFINITY);
        }
        gScore.put(sourceId, 0.0);
        openSet.add(new HeapEntry(sourceId, haversine(graph.getNode(sourceId), target)));

        while (!openSet.isEmpty()) {
            HeapEntry current = openSet.poll();
            Long currentId = current.nodeId();

            if (visited.contains(currentId)) continue;
            visited.add(currentId);

            if (currentId.equals(targetId)) break;

            for (Edge edge : graph.getNeighbors(currentId)) {
                if (edge.isClosed()) continue;

                Long neighborId = edge.getToNodeId();
                if (visited.contains(neighborId)) continue;

                double tentativeG = gScore.get(currentId) + edge.getWeight();

                if (tentativeG < gScore.get(neighborId)) {
                    gScore.put(neighborId, tentativeG);
                    predecessor.put(neighborId, currentId);
                    double f = tentativeG + haversine(graph.getNode(neighborId), target);
                    openSet.add(new HeapEntry(neighborId, f));
                }
            }
        }

        double finalDistance = gScore.getOrDefault(targetId, Double.POSITIVE_INFINITY);
        if (Double.isInfinite(finalDistance)) {
            return new Dijkstra.Result(Collections.emptyList(), Double.POSITIVE_INFINITY, false);
        }

        LinkedList<Long> path = new LinkedList<>();
        Long step = targetId;
        while (step != null) {
            path.addFirst(step);
            step = predecessor.get(step);
        }

        return new Dijkstra.Result(path, finalDistance, true);
    }

    /** Great-circle distance between two nodes in km — the A* heuristic function h(n). */
    public static double haversine(Node a, Node b) {
        double lat1 = Math.toRadians(a.getLatitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double dLat = Math.toRadians(b.getLatitude() - a.getLatitude());
        double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());

        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));

        return EARTH_RADIUS_KM * c;
    }
}
