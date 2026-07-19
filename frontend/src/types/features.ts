import type { LocationType } from "./location";

export interface NearbyResult {
  id: number;
  name: string;
  type: LocationType;
  latitude: number;
  longitude: number;
  address?: string;
  distanceKm: number;
}

export interface RoadInfo {
  id: number;
  fromLocationId: number;
  toLocationId: number;
  baseDistanceKm: number;
  currentWeight: number;
  isBidirectional: boolean;
  isClosed: boolean;
}

export interface CountEntry {
  label: string;
  count: number;
}

export interface AnalyticsSummary {
  totalRoutesPlanned: number;
  topRoutes: CountEntry[];
  topVisitedPlaces: CountEntry[];
  averageDistanceKm: number;
  averageTimeMin: number;
  topCongestedRoads: CountEntry[];
}

export interface RouteHistoryEntry {
  id: number;
  fromLocationName: string;
  toLocationName: string;
  algorithmUsed: string;
  totalDistanceKm: number;
  estimatedTimeMin: number;
  createdAt: string;
}
