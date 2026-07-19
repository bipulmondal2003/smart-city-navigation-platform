package com.smartcity.nav.service;

import com.smartcity.nav.algorithm.Trie;
import com.smartcity.nav.dto.LocationDTO;
import com.smartcity.nav.entity.Location;
import com.smartcity.nav.repository.LocationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Owns the in-memory Trie used for location-name autocomplete/prefix search.
 * Rebuilt at startup, and whenever the admin adds a new location.
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final LocationRepository locationRepository;

    private final Trie trie = new Trie();
    // Maps lowercase name -> the Location it belongs to, since the Trie itself
    // only tracks characters/prefixes, not full entity data.
    private final Map<String, Location> nameIndex = new HashMap<>();

    @PostConstruct
    public void init() {
        rebuild();
    }

    public void rebuild() {
        nameIndex.clear();
        List<Location> locations = locationRepository.findAll();
        for (Location loc : locations) {
            trie.insert(loc.getName(), loc.getId());
            nameIndex.put(loc.getName().toLowerCase(), loc);
        }
    }

    public List<LocationDTO> autocomplete(String prefix, int limit) {
        if (prefix == null || prefix.isBlank()) return List.of();

        List<String> matchedNames = trie.autocomplete(prefix.trim(), limit);

        return matchedNames.stream()
                .map(nameIndex::get)
                .filter(Objects::nonNull)
                .map(this::toDto)
                .toList();
    }

    private LocationDTO toDto(Location loc) {
        return LocationDTO.builder()
                .id(loc.getId())
                .name(loc.getName())
                .type(loc.getType().name())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .address(loc.getAddress())
                .build();
    }
}
