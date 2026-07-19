import { apiClient } from "./apiClient";
import type { NearbyResult, RoadInfo, AnalyticsSummary, RouteHistoryEntry } from "@/types/features";
import type { LocationPoint } from "@/types/location";

export async function fetchNearby(lat: number, lng: number, type: string, limit = 5): Promise<NearbyResult[]> {
  const res = await apiClient.get<NearbyResult[]>("/nearby", {
    params: { lat, lng, type: type || undefined, limit },
  });
  return res.data;
}

export async function fetchSearchSuggestions(q: string, limit = 8): Promise<LocationPoint[]> {
  if (!q.trim()) return [];
  const res = await apiClient.get<LocationPoint[]>("/search", { params: { q, limit } });
  return res.data;
}

export async function fetchCurrentTraffic(): Promise<RoadInfo[]> {
  const res = await apiClient.get<RoadInfo[]>("/traffic/current");
  return res.data;
}

export async function simulateTraffic(count = 5): Promise<{ roadsAffected: number }> {
  const res = await apiClient.post("/traffic/simulate", null, { params: { count } });
  return res.data;
}

export async function resetTraffic(): Promise<void> {
  await apiClient.post("/traffic/reset");
}

export async function updateTraffic(
  roadId: number,
  trafficLevel: "LOW" | "MODERATE" | "HEAVY" | "SEVERE",
  isRoadClosed = false
): Promise<void> {
  await apiClient.post("/traffic", { roadId, trafficLevel, isRoadClosed });
}

export async function fetchAnalytics(): Promise<AnalyticsSummary> {
  const res = await apiClient.get<AnalyticsSummary>("/analytics");
  return res.data;
}

export async function fetchMyHistory(): Promise<RouteHistoryEntry[]> {
  const res = await apiClient.get<RouteHistoryEntry[]>("/history");
  return res.data;
}

// --- Admin ---
export async function addLocation(loc: LocationPoint): Promise<LocationPoint> {
  const res = await apiClient.post<LocationPoint>("/locations", loc);
  return res.data;
}

export async function deleteLocation(id: number): Promise<void> {
  await apiClient.delete(`/locations/${id}`);
}

export async function addRoad(road: {
  fromLocationId: number;
  toLocationId: number;
  baseDistanceKm: number;
  isBidirectional?: boolean;
}): Promise<RoadInfo> {
  const res = await apiClient.post<RoadInfo>("/roads", road);
  return res.data;
}

export async function closeRoad(id: number): Promise<void> {
  await apiClient.patch(`/roads/${id}/close`);
}

export async function reopenRoad(id: number): Promise<void> {
  await apiClient.patch(`/roads/${id}/reopen`);
}

export async function deleteRoad(id: number): Promise<void> {
  await apiClient.delete(`/roads/${id}`);
}

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  role: string;
  isActive: boolean;
}

export async function fetchAdminUsers(): Promise<AdminUser[]> {
  const res = await apiClient.get<AdminUser[]>("/admin/users");
  return res.data;
}

export async function fetchAdminDashboard(): Promise<Record<string, number>> {
  const res = await apiClient.get<Record<string, number>>("/admin/dashboard");
  return res.data;
}
