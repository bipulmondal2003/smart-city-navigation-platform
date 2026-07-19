import { useMapEvents } from "react-leaflet";
import type { SelectedPoint } from "@/types/location";

interface ClickToSelectHandlerProps {
  onSelect: (point: SelectedPoint) => void;
}

/**
 * Invisible helper component (must be rendered inside <MapContainer>).
 * react-leaflet's useMapEvents hook attaches native Leaflet event listeners
 * to the map instance and cleans them up automatically on unmount.
 */
export default function ClickToSelectHandler({ onSelect }: ClickToSelectHandlerProps) {
  useMapEvents({
    click(e) {
      onSelect({
        latitude: Number(e.latlng.lat.toFixed(6)),
        longitude: Number(e.latlng.lng.toFixed(6)),
      });
    },
  });

  return null;
}
