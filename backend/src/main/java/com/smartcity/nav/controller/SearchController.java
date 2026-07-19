package com.smartcity.nav.controller;

import com.smartcity.nav.dto.LocationDTO;
import com.smartcity.nav.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /** GET /api/search?q={prefix}&limit={default 8} - Trie-based prefix autocomplete. Public. */
    @GetMapping
    public List<LocationDTO> search(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", required = false, defaultValue = "8") Integer limit
    ) {
        return searchService.autocomplete(query, limit);
    }
}
