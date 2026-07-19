import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { fetchCurrentTraffic, simulateTraffic, resetTraffic } from "@/services/featureApi";
import { fetchLocations } from "@/services/locationApi";
import { useAuth } from "@/contexts/AuthContext";

function congestionLabel(currentWeight: number, baseDistanceKm: number): { label: string; color: string } {
  const ratio = currentWeight / baseDistanceKm;
  if (ratio <= 1.05) return { label: "Low", color: "bg-green-100 text-green-700" };
  if (ratio <= 1.5) return { label: "Moderate", color: "bg-yellow-100 text-yellow-700" };
  if (ratio <= 2.0) return { label: "Heavy", color: "bg-orange-100 text-orange-700" };
  return { label: "Severe", color: "bg-red-100 text-red-700" };
}

export default function TrafficPage() {
  const { isAdmin } = useAuth();
  const queryClient = useQueryClient();

  const { data: roads = [], isLoading } = useQuery({ queryKey: ["traffic"], queryFn: fetchCurrentTraffic });
  const { data: locations = [] } = useQuery({ queryKey: ["locations"], queryFn: fetchLocations });

  const locationName = (id: number) => locations.find((l) => l.id === id)?.name ?? `#${id}`;

  const simulateMutation = useMutation({
    mutationFn: () => simulateTraffic(6),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["traffic"] }),
  });

  const resetMutation = useMutation({
    mutationFn: resetTraffic,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["traffic"] }),
  });

  return (
    <div className="mx-auto h-full max-w-3xl overflow-y-auto p-6">
      <h1 className="text-lg font-semibold text-gray-800">Traffic Simulation</h1>
      <p className="mb-5 text-sm text-gray-500">
        Congestion here directly reweights the live route graph — try simulating, then re-run a route on the
        Route page to see it change.
      </p>

      {isAdmin ? (
        <div className="mb-5 flex gap-3">
          <button
            onClick={() => simulateMutation.mutate()}
            disabled={simulateMutation.isPending}
            className="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-50"
          >
            {simulateMutation.isPending ? "Simulating…" : "🚦 Simulate Traffic"}
          </button>
          <button
            onClick={() => resetMutation.mutate()}
            disabled={resetMutation.isPending}
            className="rounded-md border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          >
            {resetMutation.isPending ? "Resetting…" : "Reset to Baseline"}
          </button>
        </div>
      ) : (
        <div className="mb-5 rounded-md bg-gray-50 p-3 text-sm text-gray-500">
          Log in as an admin to trigger traffic simulation. You can still view current conditions below.
        </div>
      )}

      {isLoading ? (
        <p className="text-sm text-gray-400">Loading road conditions…</p>
      ) : (
        <div className="space-y-2">
          {roads.map((road) => {
            const { label, color } = congestionLabel(road.currentWeight, road.baseDistanceKm);
            return (
              <div key={road.id} className="flex items-center justify-between rounded-md bg-white p-3 shadow-sm">
                <div className="text-sm text-gray-700">
                  {locationName(road.fromLocationId)} → {locationName(road.toLocationId)}
                  {road.isClosed && <span className="ml-2 text-xs font-medium text-red-600">CLOSED</span>}
                </div>
                <div className="flex items-center gap-3">
                  <span className="text-xs text-gray-400">
                    {road.currentWeight.toFixed(2)} / {road.baseDistanceKm.toFixed(2)} km
                  </span>
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${color}`}>{label}</span>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
