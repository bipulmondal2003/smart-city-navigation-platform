package com.smartcity.nav.algorithm;

import java.util.*;

/**
 * Dijkstra's shortest-path algorithm over the CityGraph.
 *
 * Uses a binary min-heap (PriorityQueue) keyed on tentative distance —
 * this is the standard greedy + heap approach: always expand the
 * closest unvisited node next, relax its neighbors, repeat.
 *
 * Time complexity:  O((V + E) log V)   — each edge relaxation may push
 *                                        onto the heap, each push/pop is O(log V)
 * Space complexity: O(V + E)           — distance map, predecessor map, heap
 *
 * Guarantees the shortest path IF all edge weights are non-negative,
 * which holds here since road weights are distances/traffic-adjusted
 * distances and are never negative.
 */
public class Dijkstra {

    public static class Result {
        public final List<Long> path;
        public final double totalDistance;
        public final boolean reachable;

        public Result(List<Long> path, double totalDistance, boolean reachable) {
            this.path = path;
            this.totalDistance = totalDistance;
            this.reachable = reachable;
        }
    }

    private record HeapEntry(Long nodeId, double distance) {}

    public static Result findShortestPath(CityGraph graph, Long sourceId, Long targetId) {
        if (!graph.hasNode(sourceId) || !graph.hasNode(targetId)) {
            return new Result(Collections.emptyList(), Double.POSITIVE_INFINITY, false);
        }

        Map<Long, Double> distance = new HashMap<>();
        Map<Long, Long> predecessor = new HashMap<>();
        Set<Long> visited = new HashSet<>();

        // Min-heap ordered by current best-known distance to each node.
        PriorityQueue<HeapEntry> minHeap = new PriorityQueue<>(Comparator.comparingDouble(HeapEntry::distance));

        for (Node node : graph.getAllNodes()) {
            distance.put(node.getId(), Double.POSITIVE_INFINITY);
        }
        distance.put(sourceId, 0.0);
        minHeap.add(new HeapEntry(sourceId, 0.0));

        while (!minHeap.isEmpty()) {
            HeapEntry current = minHeap.poll();
            Long currentId = current.nodeId();

            if (visited.contains(currentId)) continue; // stale heap entry, skip
            visited.add(currentId);

            if (currentId.equals(targetId)) break; // shortest path to target finalized

            for (Edge edge : graph.getNeighbors(currentId)) {
                if (edge.isClosed()) continue;

                Long neighborId = edge.getToNodeId();
                if (visited.contains(neighborId)) continue;

                double candidateDistance = distance.get(currentId) + edge.getWeight();

                if (candidateDistance < distance.get(neighborId)) {
                    distance.put(neighborId, candidateDistance);
                    predecessor.put(neighborId, currentId);
                    minHeap.add(new HeapEntry(neighborId, candidateDistance));
                }
            }
        }

        double finalDistance = distance.getOrDefault(targetId, Double.POSITIVE_INFINITY);
        if (Double.isInfinite(finalDistance)) {
            return new Result(Collections.emptyList(), Double.POSITIVE_INFINITY, false);
        }

        // Reconstruct path by walking predecessors backward from target to source.
        LinkedList<Long> path = new LinkedList<>();
        Long step = targetId;
        while (step != null) {
            path.addFirst(step);
            step = predecessor.get(step);
        }

        return new Result(path, finalDistance, true);
    }
}
