import { apiClient } from "./apiClient";
import type { LocationPoint } from "@/types/location";

export async function fetchLocations(): Promise<LocationPoint[]> {
  const res = await apiClient.get<LocationPoint[]>("/locations");
  return res.data;
}
