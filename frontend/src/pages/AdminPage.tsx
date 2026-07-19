import { useState, type FormEvent } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  fetchAdminDashboard,
  fetchAdminUsers,
  addLocation,
  addRoad,
  closeRoad,
  reopenRoad,
  deleteRoad,
  fetchCurrentTraffic,
} from "@/services/featureApi";
import { fetchLocations } from "@/services/locationApi";
import type { LocationType } from "@/types/location";

const LOCATION_TYPES: LocationType[] = [
  "HOSPITAL", "RESTAURANT", "AIRPORT", "HOTEL", "SCHOOL", "BUS_STOP", "ATM", "GENERIC",
];

export default function AdminPage() {
  const queryClient = useQueryClient();

  const { data: dashboard } = useQuery({ queryKey: ["admin-dashboard"], queryFn: fetchAdminDashboard });
  const { data: users = [] } = useQuery({ queryKey: ["admin-users"], queryFn: fetchAdminUsers });
  const { data: locations = [] } = useQuery({ queryKey: ["locations"], queryFn: fetchLocations });
  const { data: roads = [] } = useQuery({ queryKey: ["traffic"], queryFn: fetchCurrentTraffic });

  const locationName = (id: number) => locations.find((l) => l.id === id)?.name ?? `#${id}`;

  function invalidateMapData() {
    queryClient.invalidateQueries({ queryKey: ["locations"] });
    queryClient.invalidateQueries({ queryKey: ["traffic"] });
    queryClient.invalidateQueries({ queryKey: ["admin-dashboard"] });
  }

  // --- Add Location form ---
  const [locName, setLocName] = useState("");
  const [locType, setLocType] = useState<LocationType>("GENERIC");
  const [locLat, setLocLat] = useState("");
  const [locLng, setLocLng] = useState("");

  const addLocationMutation = useMutation({
    mutationFn: () =>
      addLocation({ name: locName, type: locType, latitude: Number(locLat), longitude: Number(locLng) }),
    onSuccess: () => {
      setLocName(""); setLocLat(""); setLocLng("");
      invalidateMapData();
    },
  });

  function handleAddLocation(e: FormEvent) {
    e.preventDefault();
    addLocationMutation.mutate();
  }

  // --- Add Road form ---
  const [roadFrom, setRoadFrom] = useState("");
  const [roadTo, setRoadTo] = useState("");
  const [roadDistance, setRoadDistance] = useState("");

  const addRoadMutation = useMutation({
    mutationFn: () =>
      addRoad({
        fromLocationId: Number(roadFrom),
        toLocationId: Number(roadTo),
        baseDistanceKm: Number(roadDistance),
        isBidirectional: true,
      }),
    onSuccess: () => {
      setRoadFrom(""); setRoadTo(""); setRoadDistance("");
      invalidateMapData();
    },
  });

  function handleAddRoad(e: FormEvent) {
    e.preventDefault();
    if (roadFrom === roadTo) return;
    addRoadMutation.mutate();
  }

  const closeMutation = useMutation({ mutationFn: closeRoad, onSuccess: invalidateMapData });
  const reopenMutation = useMutation({ mutationFn: reopenRoad, onSuccess: invalidateMapData });
  const deleteMutation = useMutation({ mutationFn: deleteRoad, onSuccess: invalidateMapData });

  return (
    <div className="mx-auto h-full max-w-5xl overflow-y-auto p-6">
      <h1 className="text-lg font-semibold text-gray-800">Admin Panel</h1>
      <p className="mb-5 text-sm text-gray-500">Manage the city graph: locations, roads, and users.</p>

      {dashboard && (
        <div className="mb-6 grid grid-cols-2 gap-3 md:grid-cols-5">
          {Object.entries(dashboard).map(([key, value]) => (
            <div key={key} className="rounded-md bg-white p-3 text-center shadow-sm">
              <div className="text-xl font-semibold text-gray-800">{value}</div>
              <div className="text-xs capitalize text-gray-500">{key.replace(/([A-Z])/g, " $1")}</div>
            </div>
          ))}
        </div>
      )}

      <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
        {/* Add Location */}
        <form onSubmit={handleAddLocation} className="rounded-md bg-white p-4 shadow-sm">
          <h3 className="mb-3 text-sm font-semibold text-gray-700">Add Location</h3>
          <input
            required placeholder="Name" value={locName} onChange={(e) => setLocName(e.target.value)}
            className="mb-2 w-full rounded-md border border-gray-300 px-2 py-1.5 text-sm"
          />
          <select
            value={locType} onChange={(e) => setLocType(e.target.value as LocationType)}
            className="mb-2 w-full rounded-md border border-gray-300 px-2 py-1.5 text-sm"
          >
            {LOCATION_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>
          <div className="mb-2 flex gap-2">
            <input
              required type="number" step="any" placeholder="Latitude" value={locLat}
              onChange={(e) => setLocLat(e.target.value)}
              className="w-1/2 rounded-md border border-gray-300 px-2 py-1.5 text-sm"
            />
            <input
              required type="number" step="any" placeholder="Longitude" value={locLng}
              onChange={(e) => setLocLng(e.target.value)}
              className="w-1/2 rounded-md border border-gray-300 px-2 py-1.5 text-sm"
            />
          </div>
          <button
            type="submit" disabled={addLocationMutation.isPending}
            className="w-full rounded-md bg-primary-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-50"
          >
            {addLocationMutation.isPending ? "Adding…" : "Add Location"}
          </button>
        </form>

        {/* Add Road */}
        <form onSubmit={handleAddRoad} className="rounded-md bg-white p-4 shadow-sm">
          <h3 className="mb-3 text-sm font-semibold text-gray-700">Add Road</h3>
          <select
            required value={roadFrom} onChange={(e) => setRoadFrom(e.target.value)}
            className="mb-2 w-full rounded-md border border-gray-300 px-2 py-1.5 text-sm"
          >
            <option value="">From…</option>
            {locations.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
          </select>
          <select
            required value={roadTo} onChange={(e) => setRoadTo(e.target.value)}
            className="mb-2 w-full rounded-md border border-gray-300 px-2 py-1.5 text-sm"
          >
            <option value="">To…</option>
            {locations.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
          </select>
          <input
            required type="number" step="any" min="0.01" placeholder="Distance (km)" value={roadDistance}
            onChange={(e) => setRoadDistance(e.target.value)}
            className="mb-2 w-full rounded-md border border-gray-300 px-2 py-1.5 text-sm"
          />
          <button
            type="submit" disabled={addRoadMutation.isPending}
            className="w-full rounded-md bg-primary-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-50"
          >
            {addRoadMutation.isPending ? "Adding…" : "Add Road"}
          </button>
        </form>
      </div>

      {/* Roads management */}
      <div className="mt-5 rounded-md bg-white p-4 shadow-sm">
        <h3 className="mb-3 text-sm font-semibold text-gray-700">Manage Roads</h3>
        <div className="space-y-1.5">
          {roads.map((road) => (
            <div key={road.id} className="flex items-center justify-between border-b border-gray-100 py-1.5 text-sm">
              <span className="text-gray-700">
                {locationName(road.fromLocationId)} → {locationName(road.toLocationId)}
                {road.isClosed && <span className="ml-2 text-xs font-medium text-red-600">CLOSED</span>}
              </span>
              <span className="flex gap-2">
                {road.isClosed ? (
                  <button onClick={() => reopenMutation.mutate(road.id)} className="text-xs text-primary-600 hover:underline">
                    Reopen
                  </button>
                ) : (
                  <button onClick={() => closeMutation.mutate(road.id)} className="text-xs text-orange-600 hover:underline">
                    Close
                  </button>
                )}
                <button onClick={() => deleteMutation.mutate(road.id)} className="text-xs text-red-600 hover:underline">
                  Delete
                </button>
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Users */}
      <div className="mt-5 rounded-md bg-white p-4 shadow-sm">
        <h3 className="mb-3 text-sm font-semibold text-gray-700">Users</h3>
        <div className="space-y-1.5">
          {users.map((u) => (
            <div key={u.id} className="flex items-center justify-between border-b border-gray-100 py-1.5 text-sm">
              <span className="text-gray-700">{u.name} <span className="text-gray-400">({u.email})</span></span>
              <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-600">{u.role}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
