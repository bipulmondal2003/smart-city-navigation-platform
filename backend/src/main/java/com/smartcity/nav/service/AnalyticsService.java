package com.smartcity.nav.service;

import com.smartcity.nav.dto.AnalyticsSummaryDTO;
import com.smartcity.nav.entity.RouteHistory;
import com.smartcity.nav.entity.Traffic;
import com.smartcity.nav.repository.RouteHistoryRepository;
import com.smartcity.nav.repository.TrafficRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Aggregates analytics purely from data already in MySQL — no separate
 * analytics store. Two DSA techniques are the point of this module:
 *
 *  - HashMap frequency counting: tally how many times each (from,to) pair
 *    and each location appears across route_history, in a single O(N) pass.
 *  - Min-heap top-K: rather than sorting the full frequency map (O(N log N))
 *    just to keep the top 5, a bounded min-heap keeps the running top-K in
 *    O(N log K), popping the smallest whenever the heap exceeds size K.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final RouteHistoryRepository routeHistoryRepository;
    private final TrafficRepository trafficRepository;

    private static final int TOP_N = 5;

    public AnalyticsSummaryDTO getSummary() {
        List<RouteHistory> history = routeHistoryRepository.findAll();

        return AnalyticsSummaryDTO.builder()
                .totalRoutesPlanned(history.size())
                .topRoutes(topRoutes(history))
                .topVisitedPlaces(topVisitedPlaces(history))
                .averageDistanceKm(averageDistance(history))
                .averageTimeMin(averageTime(history))
                .topCongestedRoads(topCongestedRoads())
                .build();
    }

    /** HashMap frequency count over "fromId->toId" pairs, then heap-based top-N. */
    private List<AnalyticsSummaryDTO.CountEntry> topRoutes(List<RouteHistory> history) {
        Map<String, Integer> frequency = new HashMap<>();
        for (RouteHistory h : history) {
            String key = h.getFromLocation().getName() + " -> " + h.getToLocation().getName();
            frequency.merge(key, 1, Integer::sum);
        }
        return topNByFrequency(frequency, TOP_N);
    }

    /** HashMap frequency count over every location appearing as either endpoint. */
    private List<AnalyticsSummaryDTO.CountEntry> topVisitedPlaces(List<RouteHistory> history) {
        Map<String, Integer> frequency = new HashMap<>();
        for (RouteHistory h : history) {
            frequency.merge(h.getFromLocation().getName(), 1, Integer::sum);
            frequency.merge(h.getToLocation().getName(), 1, Integer::sum);
        }
        return topNByFrequency(frequency, TOP_N);
    }

    /**
     * Bounded min-heap top-N: push every entry, and whenever the heap grows
     * past N, pop the smallest. What survives at the end is the N largest —
     * O(N log K) instead of sorting everything.
     */
    private List<AnalyticsSummaryDTO.CountEntry> topNByFrequency(Map<String, Integer> frequency, int n) {
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : frequency.entrySet()) {
            minHeap.add(entry);
            if (minHeap.size() > n) {
                minHeap.poll();
            }
        }

        List<AnalyticsSummaryDTO.CountEntry> result = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            Map.Entry<String, Integer> e = minHeap.poll();
            result.add(new AnalyticsSummaryDTO.CountEntry(e.getKey(), e.getValue()));
        }
        Collections.reverse(result); // largest first
        return result;
    }

    /** Same bounded min-heap approach, applied to live congestion_factor instead of a frequency count. */
    private List<AnalyticsSummaryDTO.CountEntry> topCongestedRoads() {
        List<Traffic> allTraffic = trafficRepository.findAll();

        // Keep only the latest reading per road (a road may have many historical Traffic rows).
        Map<Long, Traffic> latestByRoad = new HashMap<>();
        for (Traffic t : allTraffic) {
            Traffic existing = latestByRoad.get(t.getRoad().getId());
            if (existing == null || t.getCreatedAt().isAfter(existing.getCreatedAt())) {
                latestByRoad.put(t.getRoad().getId(), t);
            }
        }

        PriorityQueue<Traffic> minHeap = new PriorityQueue<>(
                Comparator.comparingDouble(t -> t.getCongestionFactor().doubleValue()));

        for (Traffic t : latestByRoad.values()) {
            minHeap.add(t);
            if (minHeap.size() > TOP_N) minHeap.poll();
        }

        List<AnalyticsSummaryDTO.CountEntry> result = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            Traffic t = minHeap.poll();
            String label = t.getRoad().getFromLocation().getName() + " -> " + t.getRoad().getToLocation().getName();
            int congestionPercent = (int) Math.round(t.getCongestionFactor().doubleValue() * 100);
            result.add(new AnalyticsSummaryDTO.CountEntry(label, congestionPercent));
        }
        Collections.reverse(result);
        return result;
    }

    private BigDecimal averageDistance(List<RouteHistory> history) {
        if (history.isEmpty()) return BigDecimal.ZERO;
        double avg = history.stream().mapToDouble(h -> h.getTotalDistanceKm().doubleValue()).average().orElse(0);
        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal averageTime(List<RouteHistory> history) {
        if (history.isEmpty()) return BigDecimal.ZERO;
        double avg = history.stream().mapToDouble(h -> h.getEstimatedTimeMin().doubleValue()).average().orElse(0);
        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }
}
