import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { fetchNearby } from "@/services/featureApi";
import { useGeolocation } from "@/hooks/useGeolocation";
import type { NearbyResult } from "@/types/features";

const TYPES = ["", "HOSPITAL", "RESTAURANT", "AIRPORT", "HOTEL", "SCHOOL", "BUS_STOP", "ATM", "GENERIC"];

export default function NearbyPage() {
  const { latitude, longitude, locate, loading: locating, error: geoError } = useGeolocation();
  const [type, setType] = useState("");
  const [limit, setLimit] = useState(5);
  const [results, setResults] = useState<NearbyResult[]>([]);

  const nearbyMutation = useMutation({
    mutationFn: () => fetchNearby(latitude!, longitude!, type, limit),
    onSuccess: setResults,
  });

  return (
    <div className="mx-auto h-full max-w-2xl overflow-y-auto p-6">
      <h1 className="text-lg font-semibold text-gray-800">Nearby Services</h1>
      <p className="mb-5 text-sm text-gray-500">
        Find the closest hospitals, ATMs, bus stops, and more — sorted by straight-line distance.
      </p>

      <div className="mb-5 flex flex-wrap items-end gap-3 rounded-md bg-white p-4 shadow-sm">
        <div>
          <label className="mb-1 block text-xs font-medium text-gray-600">Type</label>
          <select
            value={type}
            onChange={(e) => setType(e.target.value)}
            className="rounded-md border border-gray-300 px-2 py-1.5 text-sm"
          >
            {TYPES.map((t) => (
              <option key={t} value={t}>{t || "All types"}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="mb-1 block text-xs font-medium text-gray-600">Limit</label>
          <input
            type="number"
            min={1}
            max={20}
            value={limit}
            onChange={(e) => setLimit(Number(e.target.value))}
            className="w-16 rounded-md border border-gray-300 px-2 py-1.5 text-sm"
          />
        </div>

        <button
          onClick={locate}
          disabled={locating}
          className="rounded-md border border-gray-300 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-50 disabled:opacity-50"
        >
          {locating ? "Locating…" : "📍 Use my location"}
        </button>

        <button
          onClick={() => nearbyMutation.mutate()}
          disabled={latitude === null || longitude === null || nearbyMutation.isPending}
          className="rounded-md bg-primary-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-50"
        >
          {nearbyMutation.isPending ? "Searching…" : "Search Nearby"}
        </button>
      </div>

      {geoError && <p className="mb-4 text-sm text-red-600">{geoError}</p>}
      {latitude !== null && longitude !== null && (
        <p className="mb-4 text-xs text-gray-400">
          Using location: {latitude.toFixed(4)}, {longitude.toFixed(4)}
        </p>
      )}

      <div className="space-y-2">
        {results.map((r) => (
          <div key={r.id} className="flex items-center justify-between rounded-md bg-white p-3 shadow-sm">
            <div>
              <div className="text-sm font-medium text-gray-800">{r.name}</div>
              <div className="text-xs text-gray-500">{r.type}{r.address ? ` · ${r.address}` : ""}</div>
            </div>
            <div className="text-sm font-medium text-primary-700">{r.distanceKm} km</div>
          </div>
        ))}
        {nearbyMutation.isSuccess && results.length === 0 && (
          <p className="text-sm text-gray-400">No locations found for that type.</p>
        )}
      </div>
    </div>
  );
}
