package com.smartcity.nav.service;

import com.smartcity.nav.algorithm.CityGraph;
import com.smartcity.nav.algorithm.Edge;
import com.smartcity.nav.algorithm.Node;
import com.smartcity.nav.entity.Location;
import com.smartcity.nav.entity.Road;
import com.smartcity.nav.repository.LocationRepository;
import com.smartcity.nav.repository.RoadRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Owns the single in-memory CityGraph instance for the whole application.
 *
 * The graph is built once at startup (@PostConstruct) instead of being
 * reconstructed from MySQL on every route request — this is what makes
 * Dijkstra/BFS/A* fast, and it's the detail worth calling out in a viva:
 * the DB is the source of truth, but routing runs against an in-memory
 * adjacency list, not live SQL joins.
 *
 * Whenever an admin adds/edits/removes a road or location, call
 * rebuildGraph() to keep the in-memory structure in sync.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphService {

    private final LocationRepository locationRepository;
    private final RoadRepository roadRepository;

    private final CityGraph graph = new CityGraph();

    @PostConstruct
    public void rebuildGraph() {
        graph.clear();

        List<Location> locations = locationRepository.findAll();
        for (Location loc : locations) {
            graph.addNode(new Node(loc.getId(), loc.getLatitude().doubleValue(), loc.getLongitude().doubleValue()));
        }

        List<Road> roads = roadRepository.findAll();
        for (Road road : roads) {
            double weight = road.getCurrentWeight() != null
                    ? road.getCurrentWeight().doubleValue()
                    : road.getBaseDistanceKm().doubleValue();
            boolean closed = Boolean.TRUE.equals(road.getIsClosed());

            graph.addEdge(road.getFromLocation().getId(),
                    new Edge(road.getId(), road.getToLocation().getId(), weight, closed));

            if (Boolean.TRUE.equals(road.getIsBidirectional())) {
                graph.addEdge(road.getToLocation().getId(),
                        new Edge(road.getId(), road.getFromLocation().getId(), weight, closed));
            }
        }

        log.info("City graph rebuilt: {} nodes, {} roads loaded", graph.nodeCount(), roads.size());
    }

    public CityGraph getGraph() {
        return graph;
    }

    /**
     * Mutates the in-memory edge(s) for a given road in place — used by
     * TrafficService so a congestion update takes effect on the very next
     * route request without paying the cost of rebuilding the whole graph
     * from MySQL. Bidirectional roads have two Edge objects (one per
     * direction) sharing the same roadId, both get updated.
     */
    public void updateEdgeState(Long roadId, double newWeight, boolean closed) {
        for (Node node : graph.getAllNodes()) {
            for (var edge : graph.getNeighbors(node.getId())) {
                if (edge.getRoadId().equals(roadId)) {
                    edge.setWeight(newWeight);
                    edge.setClosed(closed);
                }
            }
        }
    }
}
