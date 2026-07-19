package com.smartcity.nav.algorithm;

import java.util.*;

/**
 * Breadth-First and Depth-First traversal over the CityGraph.
 *
 * Used for:
 *  - BFS: "locations reachable within N hops" (nearby-search fallback when
 *    no distance-based results are needed, and validating that a spatial
 *    nearest-neighbor result is actually road-reachable from the user)
 *  - DFS: general connectivity checks (e.g. admin panel "is the graph still
 *    connected after I close this road?")
 *
 * Time complexity (both): O(V + E)
 * Space complexity (both): O(V) for the visited set + queue/stack
 */
public class GraphTraversal {

    /**
     * Returns all node IDs reachable from `startId` within `maxHops` edges,
     * ignoring closed roads. A hop-limited BFS is the right tool here because
     * BFS naturally explores level-by-level (nearest hop-count first).
     */
    public static List<Long> bfsWithinHops(CityGraph graph, Long startId, int maxHops) {
        List<Long> result = new ArrayList<>();
        if (!graph.hasNode(startId)) return result;

        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        Map<Long, Integer> hopCount = new HashMap<>();

        queue.add(startId);
        visited.add(startId);
        hopCount.put(startId, 0);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            int hops = hopCount.get(current);

            if (hops >= maxHops) continue;

            for (Edge edge : graph.getNeighbors(current)) {
                if (edge.isClosed()) continue;
                Long neighborId = edge.getToNodeId();

                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    hopCount.put(neighborId, hops + 1);
                    result.add(neighborId);
                    queue.add(neighborId);
                }
            }
        }
        return result;
    }

    /**
     * Full BFS traversal order from a start node (no hop limit) — used when
     * ranking "nearest" results purely by hop-distance rather than physical distance.
     */
    public static List<Long> bfsFullTraversal(CityGraph graph, Long startId) {
        return bfsWithinHops(graph, startId, Integer.MAX_VALUE);
    }

    /**
     * Recursive DFS traversal from a start node — returns visited nodes
     * in depth-first order. Used for connectivity checks in the admin panel.
     */
    public static List<Long> dfs(CityGraph graph, Long startId) {
        List<Long> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        dfsHelper(graph, startId, visited, result);
        return result;
    }

    private static void dfsHelper(CityGraph graph, Long nodeId, Set<Long> visited, List<Long> result) {
        if (visited.contains(nodeId)) return;
        visited.add(nodeId);
        result.add(nodeId);

        for (Edge edge : graph.getNeighbors(nodeId)) {
            if (!edge.isClosed() && !visited.contains(edge.getToNodeId())) {
                dfsHelper(graph, edge.getToNodeId(), visited, result);
            }
        }
    }

    /** True if every node in the graph is reachable from `startId` (ignoring closed roads). */
    public static boolean isFullyConnectedFrom(CityGraph graph, Long startId) {
        return dfs(graph, startId).size() == graph.nodeCount();
    }
}
