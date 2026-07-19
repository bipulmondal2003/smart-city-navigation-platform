import { Fragment } from "react";
import { Marker, Popup } from "react-leaflet";
import L from "leaflet";
import type { LocationPoint } from "@/types/location";

const icon = L.icon({
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
  iconSize: [20, 33],
  iconAnchor: [10, 33],
  popupAnchor: [1, -28],
});

interface LocationMarkersProps {
  locations: LocationPoint[];
}

export default function LocationMarkers({ locations }: LocationMarkersProps) {
  return (
    <Fragment>
      {locations.map((loc) => (
        <Marker key={loc.id} position={[loc.latitude, loc.longitude]} icon={icon}>
          <Popup>
            <div className="text-sm">
              <div className="font-semibold">{loc.name}</div>
              <div className="text-gray-500">{loc.type}</div>
            </div>
          </Popup>
        </Marker>
      ))}
    </Fragment>
  );
}
