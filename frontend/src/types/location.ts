export type LocationType =
  | "HOSPITAL"
  | "RESTAURANT"
  | "AIRPORT"
  | "HOTEL"
  | "SCHOOL"
  | "BUS_STOP"
  | "ATM"
  | "GENERIC";

export interface LocationPoint {
  id?: number;
  name: string;
  type: LocationType;
  latitude: number;
  longitude: number;
  address?: string;
}

export interface SelectedPoint {
  latitude: number;
  longitude: number;
}
