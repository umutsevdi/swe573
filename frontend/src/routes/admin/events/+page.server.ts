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
  const typeParam = params.get("type");
  const type =
    typeParam === "OFFER" || typeParam === "NEED"
      ? (typeParam as "OFFER" | "NEED")
      : "";
  const includeCancelled = params.get("includeCancelled") === "true";

  let events: EventResponse[] = [];
  let hasSearched = false;

  // Always search, but apply filters if provided
  try {
    const requestBody: EventFilterRequest = {};
    if (q.trim()) requestBody.textSearch = q.trim();
    if (tags.length) requestBody.tags = tags;
    if (start) requestBody.startDate = start;
    if (end) requestBody.endDate = end;
    if (type) requestBody.type = type;
    requestBody.includeCancelled = includeCancelled; // Always include cancelled for admin

    events = await API.events.filter(HIVE_API!, cookies, requestBody);
    hasSearched = true;
  } catch (error) {
    console.error("Error fetching events:", error);
    events = [];
    hasSearched = true;
  }

  return {
    events,
    hasSearched,
    q,
    tags,
    start,
    end,
    type,
    includeCancelled
  };
};
