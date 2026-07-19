import { useState, useEffect, useRef } from "react";
import { fetchSearchSuggestions } from "@/services/featureApi";
import type { LocationPoint } from "@/types/location";

interface SearchAutocompleteProps {
  placeholder?: string;
  onSelect: (location: LocationPoint) => void;
}

/**
 * Debounced autocomplete backed by the backend Trie (GET /api/search).
 * Each keystroke fires a prefix lookup; the Trie makes this O(L) server-side
 * regardless of how many locations exist, which is the whole point of using
 * one instead of a SQL LIKE '%...%' scan.
 */
export default function SearchAutocomplete({ placeholder, onSelect }: SearchAutocompleteProps) {
  const [query, setQuery] = useState("");
  const [suggestions, setSuggestions] = useState<LocationPoint[]>([]);
  const [open, setOpen] = useState(false);
  const debounceRef = useRef<ReturnType<typeof setTimeout>>();

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);

    if (!query.trim()) {
      setSuggestions([]);
      return;
    }

    debounceRef.current = setTimeout(async () => {
      const results = await fetchSearchSuggestions(query, 6);
      setSuggestions(results);
      setOpen(true);
    }, 200);

    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [query]);

  function handlePick(loc: LocationPoint) {
    onSelect(loc);
    setQuery(loc.name);
    setOpen(false);
  }

  return (
    <div className="relative">
      <input
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        onFocus={() => suggestions.length > 0 && setOpen(true)}
        onBlur={() => setTimeout(() => setOpen(false), 150)}
        placeholder={placeholder ?? "Search a location…"}
        className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
      />
      {open && suggestions.length > 0 && (
        <ul className="absolute z-10 mt-1 w-full rounded-md border border-gray-200 bg-white shadow-lg">
          {suggestions.map((loc) => (
            <li
              key={loc.id}
              onMouseDown={() => handlePick(loc)}
              className="cursor-pointer px-3 py-2 text-sm hover:bg-primary-50"
            >
              <span className="font-medium text-gray-800">{loc.name}</span>{" "}
              <span className="text-xs text-gray-400">{loc.type}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
