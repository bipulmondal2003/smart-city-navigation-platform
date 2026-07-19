package com.smartcity.nav.algorithm;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord = false;
    // When isEndOfWord is true, this holds the actual location ID(s) that map to this exact name.
    Long locationId;
}
