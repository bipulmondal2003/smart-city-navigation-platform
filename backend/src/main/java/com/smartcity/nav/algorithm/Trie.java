package com.smartcity.nav.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * A Trie (prefix tree) indexing location names for fast autocomplete.
 *
 * Why a Trie over a SQL LIKE '%prefix%' query: prefix lookup here is
 * O(L) where L is the length of the typed prefix, completely independent
 * of how many locations exist (V) — a LIKE scan is O(V) in the worst case
 * without a prefix-friendly index. For an autocomplete box firing on every
 * keystroke, that difference matters.
 *
 * insert:  O(L), L = length of the name
 * search (exact): O(L)
 * autocomplete(prefix): O(L) to reach the prefix node + O(K) to collect
 *                        the K matching results beneath it
 */
public class Trie {

    private final TrieNode root = new TrieNode();

    public void insert(String name, Long locationId) {
        TrieNode current = root;
        for (char c : name.toLowerCase().toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
        }
        current.isEndOfWord = true;
        current.locationId = locationId;
    }

    /** Exact-match search — true if this exact name was inserted. */
    public boolean search(String name) {
        TrieNode node = traverse(name);
        return node != null && node.isEndOfWord;
    }

    /** Returns up to `limit` location names that start with the given prefix. */
    public List<String> autocomplete(String prefix, int limit) {
        List<String> results = new ArrayList<>();
        TrieNode prefixNode = traverse(prefix);
        if (prefixNode == null) return results;

        collectWords(prefixNode, prefix.toLowerCase(), results, limit);
        return results;
    }

    private TrieNode traverse(String text) {
        TrieNode current = root;
        for (char c : text.toLowerCase().toCharArray()) {
            current = current.children.get(c);
            if (current == null) return null;
        }
        return current;
    }

    private void collectWords(TrieNode node, String prefix, List<String> results, int limit) {
        if (results.size() >= limit) return;
        if (node.isEndOfWord) results.add(prefix);

        for (var entry : node.children.entrySet()) {
            if (results.size() >= limit) return;
            collectWords(entry.getValue(), prefix + entry.getKey(), results, limit);
        }
    }
}
