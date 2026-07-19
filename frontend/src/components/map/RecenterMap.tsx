import { useEffect } from "react";
import { useMap } from "react-leaflet";

interface RecenterMapProps {
  latitude: number;
  longitude: number;
  zoom?: number;
}

/**
 * react-leaflet's <MapContainer> only sets the initial center/zoom.
 * To programmatically move the view later (e.g. after "Use my location"),
 * we grab the map instance via useMap() and call flyTo() in an effect.
 */
export default function RecenterMap({ latitude, longitude, zoom = 15 }: RecenterMapProps) {
  const map = useMap();

  useEffect(() => {
    map.flyTo([latitude, longitude], zoom, { duration: 0.8 });
  }, [latitude, longitude, zoom, map]);

  return null;
}
