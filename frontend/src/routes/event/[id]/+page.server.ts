import type { PageServerLoad } from "./$types";
import { error } from "@sveltejs/kit";
import { HIVE_API } from "$lib/serverConfig";
import { API, type EventResponse } from "$lib/api";

type EventData = {
  id: number;
  title: string;
  description: string;
  ownerId: number;
  tags: string[];
  location: string;
  coordinates: { lat: number; lng: number };
  type: "OFFER" | "NEED";
  participantCount: number;
  maxParticipants?: number;
  createdAt: string;
  updatedAt: string;
  eventDate?: string;
  duration?: string;
  capacity?: number;
  cancelled: boolean;
};

export const load: PageServerLoad = async ({ params, cookies }) => {
  const id = Number(params.id);
  console.log("Loading event:", id);

  if (!Number.isInteger(id) || id < 0) {
    console.log("Invalid event id:", id);
    throw error(404, "Event not found");
  }

  const serverData: EventResponse = await API.events.getById(
    HIVE_API as string,
    cookies,
    id
  );
  console.log(serverData);

  const mappedData: EventData = {
    id: serverData.id,
    title: serverData.title,
    description: serverData.description,
    ownerId: serverData.ownerId,
    tags: serverData.tags,
    location: serverData.location.name,
    coordinates: { lat: serverData.location.lat, lng: serverData.location.lon },
    type: serverData.type,
    participantCount: 0, // Default, as not provided in server response
    maxParticipants: serverData.maxParticipants,
    createdAt: new Date(serverData.createdAt).toLocaleDateString(),
    updatedAt: serverData.updatedAt
      ? new Date(serverData.updatedAt).toLocaleDateString()
      : "",
    eventDate: serverData.eventDate,
    duration: serverData.duration
      ? `${serverData.duration} hour(s)`
      : undefined,
    capacity: serverData.maxParticipants,
    cancelled: serverData.cancelled
  };

  return { event: mappedData };
};
