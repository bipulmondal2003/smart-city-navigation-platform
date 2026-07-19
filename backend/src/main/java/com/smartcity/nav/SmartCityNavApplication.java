package com.smartcity.nav;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Smart City Navigation Platform backend.
 *
 * On startup, GraphService (in the `algorithm` package) will read
 * all locations + roads from MySQL and build the in-memory adjacency
 * list used by the DSA engine (Dijkstra / A* / BFS).
 */
@SpringBootApplication
public class SmartCityNavApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCityNavApplication.class, args);
    }
}
