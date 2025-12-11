import { HIVE_API } from "$lib/serverConfig";
import { type Cookies } from "@sveltejs/kit";
import { API, type EventFilterRequest, type EventResponse } from "$lib/api";

export const load = async ({
  url,
  cookies
}: {
  url: URL;
  cookies: Cookies;
}) => {
  const params = url.searchParams;

  const q = params.get("q") || "";
  const tagsParam = params.get("tags");
  const tags = tagsParam ? tagsParam.split(",").filter(Boolean) : [];
  const start = params.get("start") || "";
  const end = params.get("end") || "";
  const proximityParam = params.get("proximity");
  const proximity = proximityParam ? parseInt(proximityParam) || 0 : 0;
  const typeParam = params.get("type");
  const type =
    typeParam === "OFFER" || typeParam === "NEED"
      ? (typeParam as "OFFER" | "NEED")
      : "";
  const includeCancelled = params.get("includeCancelled") === "true";
  const userLatParam = params.get("user_lat");
  const userLngParam = params.get("user_lng");
  const userLat = userLatParam ? parseFloat(userLatParam) : null;
  const userLng = userLngParam ? parseFloat(userLngParam) : null;

  let events: EventResponse[] = [];
  let hasSearched = false;

  // Only search if there are any filter parameters
  if (
    q ||
    tags.length ||
    start ||
    end ||
    proximity > 0 ||
    type ||
    includeCancelled
  ) {
    try {
      const requestBody: EventFilterRequest = {};
      if (q.trim()) requestBody.textSearch = q.trim();
      if (tags.length) requestBody.tags = tags;
      if (start) requestBody.startDate = start;
      if (end) requestBody.endDate = end;
      if (includeCancelled) requestBody.includeCancelled = includeCancelled;
      if (proximity > 0 && userLat && userLng) {
        requestBody.userLocation = { lat: userLat, lon: userLng };
        requestBody.proximity = proximity;
      }
      if (type) requestBody.type = type;

      events = await API.events.filter(HIVE_API!, cookies, requestBody);
      hasSearched = true;
    } catch (error) {
      console.error("Error fetching events:", error);
      events = [];
      hasSearched = true;
    }
  }

  return {
    events,
    hasSearched,
    q,
    tags,
    start,
    end,
    proximity,
    type,
    includeCancelled,
    userLat,
    userLng
  };
};
