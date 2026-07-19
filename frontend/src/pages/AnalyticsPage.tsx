import { useQuery } from "@tanstack/react-query";
import { fetchAnalytics } from "@/services/featureApi";
import type { CountEntry } from "@/types/features";

function RankedList({ title, entries, unit }: { title: string; entries: CountEntry[]; unit?: string }) {
  return (
    <div className="rounded-md bg-white p-4 shadow-sm">
      <h3 className="mb-3 text-sm font-semibold text-gray-700">{title}</h3>
      {entries.length === 0 ? (
        <p className="text-xs text-gray-400">No data yet.</p>
      ) : (
        <ol className="space-y-1.5">
          {entries.map((e, i) => (
            <li key={e.label} className="flex items-center justify-between text-sm">
              <span className="text-gray-600">
                <span className="mr-2 text-gray-400">{i + 1}.</span>
                {e.label}
              </span>
              <span className="font-medium text-primary-700">
                {e.count}
                {unit}
              </span>
            </li>
          ))}
        </ol>
      )}
    </div>
  );
}

export default function AnalyticsPage() {
  const { data, isLoading } = useQuery({ queryKey: ["analytics"], queryFn: fetchAnalytics });

  if (isLoading || !data) {
    return <div className="p-6 text-sm text-gray-400">Loading analytics…</div>;
  }

  return (
    <div className="mx-auto h-full max-w-4xl overflow-y-auto p-6">
      <h1 className="text-lg font-semibold text-gray-800">Analytics Dashboard</h1>
      <p className="mb-5 text-sm text-gray-500">Aggregated from route history using HashMap frequency counting and a top-K min-heap.</p>

      <div className="mb-5 grid grid-cols-3 gap-3">
        <div className="rounded-md bg-white p-4 text-center shadow-sm">
          <div className="text-2xl font-semibold text-gray-800">{data.totalRoutesPlanned}</div>
          <div className="text-xs text-gray-500">Routes Planned</div>
        </div>
        <div className="rounded-md bg-white p-4 text-center shadow-sm">
          <div className="text-2xl font-semibold text-gray-800">{data.averageDistanceKm} km</div>
          <div className="text-xs text-gray-500">Avg. Distance</div>
        </div>
        <div className="rounded-md bg-white p-4 text-center shadow-sm">
          <div className="text-2xl font-semibold text-gray-800">{data.averageTimeMin} min</div>
          <div className="text-xs text-gray-500">Avg. Time</div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <RankedList title="Top Routes" entries={data.topRoutes} unit=" trips" />
        <RankedList title="Most Visited Places" entries={data.topVisitedPlaces} unit=" visits" />
        <RankedList title="Top Congested Roads" entries={data.topCongestedRoads} unit="%" />
      </div>

      {data.totalRoutesPlanned === 0 && (
        <p className="mt-5 text-sm text-gray-400">
          No routes recorded yet — history is only saved when a logged-in user plans a route. Log in and try
          the Route page.
        </p>
      )}
    </div>
  );
}
