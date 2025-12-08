import type { PageServerLoad } from "./$types";
import { HIVE_API } from "$lib/serverConfig";
import type { Cookies } from "@sveltejs/kit";
import { API, type EventFilterRequest } from "$lib/api";

export const load: PageServerLoad = async ({
  url,
  cookies
}: {
  url: URL;
  cookies: Cookies;
}) => {
  const searchParams = url.searchParams;
  const requestBody: EventFilterRequest = {};

  if (searchParams.has("q")) {
    requestBody.textSearch = searchParams.get("q")!.trim();
  }

  if (searchParams.has("tags")) {
    requestBody.tags = searchParams.get("tags")!.split(",").filter(Boolean);
  }

  if (searchParams.has("start")) {
    requestBody.startDate = searchParams.get("start")!;
  }

  if (searchParams.has("end")) {
    requestBody.endDate = searchParams.get("end")!;
  }

  if (searchParams.has("user_lat") && searchParams.has("user_lng")) {
    const userLat = parseFloat(searchParams.get("user_lat") || "");
    const userLng = parseFloat(searchParams.get("user_lng") || "");
    if (!isNaN(userLat) && !isNaN(userLng)) {
      requestBody.userLocation = { lat: userLat, lon: userLng };
      if (searchParams.has("proximity")) {
        const proximity = parseInt(searchParams.get("proximity") || "0");
        if (!isNaN(proximity) && proximity != 0) {
          requestBody.proximity = proximity;
        }
      }
    }
  }

  if (searchParams.has("includeCancelled")) {
    requestBody.includeCancelled =
      searchParams.get("includeCancelled") === "true";
  }

  try {
    console.log(
      "Fetching events from:",
      `${HIVE_API}/event/search`,
      "with body:",
      requestBody
    );
    let events = await API.events.filter(
      HIVE_API as string,
      cookies,
      requestBody
    );
    console.log("Fetched events:", events.length);
    // Apply type filter if present (since API doesn't support it)
    if (searchParams.has("type")) {
      const type = searchParams.get("type");
      if (type && type !== "all") {
        events = events.filter((event) => event.type === type);
      }
    }
    return { events };
  } catch (error) {
    console.error("Error fetching events:", error);
    return { events: [] };
  }
};
