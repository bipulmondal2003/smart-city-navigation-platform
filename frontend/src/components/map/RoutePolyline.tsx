import { Polyline } from "react-leaflet";
import type { LocationPoint } from "@/types/location";

interface RoutePolylineProps {
  pathLocationIds: number[];
  locations: LocationPoint[];
}

export default function RoutePolyline({ pathLocationIds, locations }: RoutePolylineProps) {
  const locationById = new Map(locations.map((l) => [l.id, l]));

  const positions: [number, number][] = pathLocationIds
    .map((id) => locationById.get(id))
    .filter((loc): loc is LocationPoint => Boolean(loc))
    .map((loc) => [loc.latitude, loc.longitude]);

  if (positions.length < 2) return null;

  return <Polyline positions={positions} pathOptions={{ color: "#2563eb", weight: 5, opacity: 0.8 }} />;
}
