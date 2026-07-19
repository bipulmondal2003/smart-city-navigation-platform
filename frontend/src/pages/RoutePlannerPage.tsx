import { useState } from "react";
import { MapContainer, TileLayer, ZoomControl } from "react-leaflet";
import { useQuery, useMutation } from "@tanstack/react-query";
import { fetchLocations } from "@/services/locationApi";
import { fetchRoute } from "@/services/routeApi";
import LocationMarkers from "@/components/map/LocationMarkers";
import RoutePolyline from "@/components/map/RoutePolyline";
import SearchAutocomplete from "@/components/common/SearchAutocomplete";
import type { RouteResult } from "@/types/route";

const CITY_CENTER: [number, number] = [26.7271, 88.3953];
const DEFAULT_ZOOM = 14;

export default function RoutePlannerPage() {
  const [fromId, setFromId] = useState<string>("");
  const [toId, setToId] = useState<string>("");
  const [route, setRoute] = useState<RouteResult | null>(null);

  const { data: locations = [], isLoading: locationsLoading, isError: locationsError } = useQuery({
    queryKey: ["locations"],
    queryFn: fetchLocations,
  });

  const routeMutation = useMutation({
    mutationFn: () => fetchRoute(Number(fromId), Number(toId)),
    onSuccess: (data) => setRoute(data),
  });

  function handleFindRoute(e: React.FormEvent) {
    e.preventDefault();
    if (!fromId || !toId || fromId === toId) return;
    setRoute(null);
    routeMutation.mutate();
  }

  return (
    <div className="flex h-full">
      {/* Sidebar */}
      <aside className="w-80 shrink-0 overflow-y-auto border-r bg-white p-5">
        <h1 className="text-lg font-semibold text-gray-800">Route Planner</h1>
        <p className="mb-4 text-sm text-gray-500">Shortest path via Dijkstra's algorithm.</p>

        {locationsLoading && <p className="text-sm text-gray-400">Loading locations…</p>}
        {locationsError && locations.length === 0 && (
          <p className="text-sm text-red-600">
            Couldn't load locations. Is the backend running on :8080?
          </p>
        )}

        {!locationsLoading && locations.length > 0 && (
          <>
            <div className="mb-4">
              <label className="mb-1 block text-sm font-medium text-gray-700">Quick search</label>
              <SearchAutocomplete
                placeholder="Type a location name…"
                onSelect={(loc) => loc.id && setFromId(String(loc.id))}
              />
            </div>

            <form onSubmit={handleFindRoute} className="space-y-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">From</label>
              <select
                value={fromId}
                onChange={(e) => setFromId(e.target.value)}
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
              >
                <option value="">Select starting point</option>
                {locations.map((loc) => (
                  <option key={loc.id} value={loc.id}>
                    {loc.name} ({loc.type})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">To</label>
              <select
                value={toId}
                onChange={(e) => setToId(e.target.value)}
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
              >
                <option value="">Select destination</option>
                {locations.map((loc) => (
                  <option key={loc.id} value={loc.id}>
                    {loc.name} ({loc.type})
                  </option>
                ))}
              </select>
            </div>

            <button
              type="submit"
              disabled={!fromId || !toId || fromId === toId || routeMutation.isPending}
              className="w-full rounded-md bg-primary-600 px-3 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-50"
            >
              {routeMutation.isPending ? "Finding route…" : "Find Route"}
            </button>
            </form>
          </>
        )}

        {routeMutation.isError && (
          <div className="mt-4 rounded-md bg-red-50 p-3 text-sm text-red-700">
            No route found between those locations.
          </div>
        )}

        {route && (
          <div className="mt-5 space-y-2 rounded-md bg-primary-50 p-4 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">Distance</span>
              <span className="font-medium">{route.totalDistanceKm} km</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Est. time</span>
              <span className="font-medium">{route.estimatedTimeMin} min</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Algorithm</span>
              <span className="font-medium">{route.algorithmUsed}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Stops</span>
              <span className="font-medium">{route.pathLocationIds.length}</span>
            </div>
          </div>
        )}
      </aside>

      {/* Map */}
      <main className="flex-1">
        <MapContainer center={CITY_CENTER} zoom={DEFAULT_ZOOM} zoomControl={false} className="h-full w-full">
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <ZoomControl position="bottomright" />
          <LocationMarkers locations={locations} />
          {route && <RoutePolyline pathLocationIds={route.pathLocationIds} locations={locations} />}
        </MapContainer>
      </main>
    </div>
  );
}
