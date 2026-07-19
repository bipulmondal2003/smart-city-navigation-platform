import { apiClient } from "./apiClient";
import type { RouteResult } from "@/types/route";

export async function fetchRoute(
  fromLocationId: number,
  toLocationId: number,
  algorithm: "dijkstra" | "astar" = "dijkstra"
): Promise<RouteResult> {
  const res = await apiClient.get<RouteResult>("/route", {
    params: { from: fromLocationId, to: toLocationId, algorithm },
  });
  return res.data;
}
