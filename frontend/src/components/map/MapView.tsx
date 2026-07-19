import { useState } from "react";
import { MapContainer, TileLayer, Marker, Popup, ZoomControl } from "react-leaflet";
import L from "leaflet";
import ClickToSelectHandler from "./ClickToSelectHandler";
import RecenterMap from "./RecenterMap";
import { useGeolocation } from "@/hooks/useGeolocation";
import type { SelectedPoint } from "@/types/location";

// Default Leaflet marker icons are broken by bundlers unless re-pointed at CDN assets.
const defaultIcon = L.icon({
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
});

// Fallback fictional-city center used until the graph/location dataset is seeded.
const DEFAULT_CENTER: [number, number] = [26.7271, 88.3953]; // Siliguri, as a placeholder city center
const DEFAULT_ZOOM = 13;

interface MapViewProps {
  /** Called whenever the user clicks the map to drop/move the selection marker. */
  onLocationSelect?: (point: SelectedPoint) => void;
  /** Optional externally-controlled marker (e.g. pre-selected "from" location). */
  selected?: SelectedPoint | null;
}

export default function MapView({ onLocationSelect, selected }: MapViewProps) {
  const [internalSelected, setInternalSelected] = useState<SelectedPoint | null>(null);
  const { latitude, longitude, error, loading, locate } = useGeolocation();

  const point = selected ?? internalSelected;

  function handleSelect(p: SelectedPoint) {
    setInternalSelected(p);
    onLocationSelect?.(p);
  }

  return (
    <div className="relative h-full w-full">
      <MapContainer
        center={DEFAULT_CENTER}
        zoom={DEFAULT_ZOOM}
        zoomControl={false}
        className="h-full w-full rounded-lg"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {/* Zoom control repositioned so it doesn't collide with our overlay UI */}
        <ZoomControl position="bottomright" />

        <ClickToSelectHandler onSelect={handleSelect} />

        {point && (
          <Marker position={[point.latitude, point.longitude]} icon={defaultIcon}>
            <Popup>
              Lat: {point.latitude} <br />
              Lng: {point.longitude}
            </Popup>
          </Marker>
        )}

        {latitude !== null && longitude !== null && (
          <RecenterMap latitude={latitude} longitude={longitude} />
        )}
      </MapContainer>

      {/* Overlay: current-location control + lat/lng readout */}
      <div className="absolute top-3 left-3 z-[1000] flex flex-col gap-2">
        <button
          onClick={locate}
          disabled={loading}
          className="rounded-md bg-white px-3 py-2 text-sm font-medium text-primary-700 shadow-md hover:bg-primary-50 disabled:opacity-50"
        >
          {loading ? "Locating..." : "📍 Use My Location"}
        </button>

        {error && (
          <div className="max-w-[220px] rounded-md bg-red-50 px-3 py-2 text-xs text-red-700 shadow">
            {error}
          </div>
        )}
      </div>

      {point && (
        <div className="absolute bottom-3 left-3 z-[1000] rounded-md bg-white px-3 py-2 text-sm shadow-md">
          <span className="font-medium text-gray-700">Selected:</span>{" "}
          <span className="text-gray-500">
            {point.latitude}, {point.longitude}
          </span>
        </div>
      )}
    </div>
  );
}
