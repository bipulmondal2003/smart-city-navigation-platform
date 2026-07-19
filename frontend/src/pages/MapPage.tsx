import MapView from "@/components/map/MapView";
import type { SelectedPoint } from "@/types/location";

export default function MapPage() {
  function handleLocationSelect(point: SelectedPoint) {
    // Routing/route-planning is implemented in a later phase.
    // For now this just proves click-to-select works end-to-end.
    console.log("Selected point:", point);
  }

  return (
    <div className="flex h-full flex-col">
      <header className="border-b bg-white px-6 py-3 shadow-sm">
        <h1 className="text-lg font-semibold text-gray-800">
          Smart City Navigation Platform
        </h1>
        <p className="text-sm text-gray-500">
          Click anywhere on the map to select a location.
        </p>
      </header>

      <main className="flex-1">
        <MapView onLocationSelect={handleLocationSelect} />
      </main>
    </div>
  );
}
