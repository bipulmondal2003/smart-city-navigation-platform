import { useQuery } from "@tanstack/react-query";
import { fetchMyHistory } from "@/services/featureApi";

export default function HistoryPage() {
  const { data: history = [], isLoading } = useQuery({ queryKey: ["history"], queryFn: fetchMyHistory });

  return (
    <div className="mx-auto h-full max-w-3xl overflow-y-auto p-6">
      <h1 className="text-lg font-semibold text-gray-800">Route History</h1>
      <p className="mb-5 text-sm text-gray-500">Every route you've planned while logged in.</p>

      {isLoading ? (
        <p className="text-sm text-gray-400">Loading…</p>
      ) : history.length === 0 ? (
        <p className="text-sm text-gray-400">No routes yet — plan one on the Route page.</p>
      ) : (
        <div className="space-y-2">
          {history.map((h) => (
            <div key={h.id} className="rounded-md bg-white p-3 shadow-sm">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-gray-800">
                  {h.fromLocationName} → {h.toLocationName}
                </span>
                <span className="text-xs text-gray-400">{new Date(h.createdAt).toLocaleString()}</span>
              </div>
              <div className="mt-1 text-xs text-gray-500">
                {h.totalDistanceKm} km · {h.estimatedTimeMin} min · {h.algorithmUsed}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
