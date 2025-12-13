import { fetchJSON, fetchImage } from "$lib/utils/fetch";
import type { Cookies } from "@sveltejs/kit";

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  fullName: string;
  biography: string;
  interests: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface UserCard {
  id: number;
  fullName: string;
  biography: string;
  interests: string[];
  userScore: number;
}

export interface LoginResponse {
  token: string | Cookies;
  user: UserCard;
}

export interface UpdateUserRequest {
  biography: string;
  interests: string[];
}

export interface UserProfileResponse {
  id: number;
  username: string;
  email: string;
  fullName: string;
  biography: string;
  interests: string[];
  role: string;
  createdAt: string;
  userScore: number;
  timeBank: number;
  frozenAmount: number;
  active: boolean;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  fullName: string;
  biography: string;
  interests: string[];
  role: string;
  createdAt: string;
  userScore: number;
  timeBank: number;
  frozenAmount: number;
  enabled: boolean;
  active: boolean;
  authorities: Array<{ authority: string }>;
  accountTerminated: boolean;
  accountNonLocked: boolean;
  accountNonExpired: boolean;
  credentialsNonExpired: boolean;
}

export interface EventFilterRequest {
  textSearch?: string;
  tags?: string[];
  userLocation?: {
    lat: number;
    lon: number;
  };
  proximity?: number;
  startDate?: string;
  endDate?: string;
  includeCancelled?: boolean;
  type?: "OFFER" | "NEED";
}

export interface EventResponse {
  id: number;
  title: string;
  description: string;
  tags: string[];
  location: { lat: number; lon: number; name: string };
  maxParticipants: number;
  type: "OFFER" | "NEED";
  duration: number;
  eventDate: string;
  createdAt: string;
  updatedAt: string;
  ownerId: number;
  cancelled: boolean;
}
export interface EventListResponse {
  id: number;
  title: string;
  tags: string[];
  location: { lat: number; lon: number; name: string };
  maxParticipants: number;
  type: "OFFER" | "NEED";
  duration: number;
  eventDate: string;
  createdAt: string;
  updatedAt: string;
  ownerId: number;
  cancelled: boolean;
}

export interface EventRequest {
  title: string;
  description: string;
  tags: string[];
  location: { lat: number; lon: number; name: string };
  maxParticipants: number;
  type: "OFFER" | "NEED";
  duration: number;
  eventDate: string;
}

export type ParticipationState =
  | "SENT"
  | "PENDING"
  | "CREATED"
  | "NEGOTIATING"
  | "ACCEPTED"
  | "REJECTED"
  | "WITHDRAWN"
  | "COMPLETED";

export interface ThreadRequest {
  title: string;
  description: string;
  tags: string[];
}

export interface ThreadReply {
  message: string;
  createdAt: string;
  ownerId: number;
}

export interface ThreadResponse {
  active: boolean;
  createdAt: string;
  description: string;
  id: number;
  ownerId: number;
  tags: string[];
  title: string;
  updatedAt: string;
  messages: ThreadReply[] | null;
}

export interface ThreadView {
  id: number;
  ownerId: number;
  title: string;
  description: string;
  createdAt: string;
  tags: string[];
  active: boolean;
}

export interface LocationSearchResult {
  place_id: number;
  licence: string;
  osm_type: string;
  osm_id: number;
  boundingbox: string[];
  lat: string;
  lon: string;
  display_name: string;
  class: string;
  type: string;
  importance: number;
  icon?: string;
}

export const API = {
  auth: {
    login: async function (
      baseUrl: string,
      request: LoginRequest
    ): Promise<LoginResponse> {
      const response = await fetch(`${baseUrl}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request)
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(
          errorData.message || `Login failed: ${response.status}`
        );
      }

      return response.json();
    },

    register: async function (
      baseUrl: string,
      request: RegisterRequest
    ): Promise<void> {
      const response = await fetch(`${baseUrl}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request)
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Registration failed");
      }
    }
  },
  users: {
    getById: async function (
      baseUrl: string,
      auth: string | Cookies,
      id: number
    ): Promise<UserProfileResponse> {
      return fetchJSON(`${baseUrl}/user/${id}`, auth);
    },

    getByIdAsCard: async function (
      baseUrl: string,
      auth: string | Cookies,
      id: number
    ): Promise<UserCard> {
      return fetchJSON(`${baseUrl}/user/${id}/card`, auth);
    },

    updateUser: async function (
      baseUrl: string,
      auth: string | Cookies,
      request: UpdateUserRequest
    ): Promise<UserProfileResponse> {
      return fetchJSON(`${baseUrl}/user`, auth, {
        method: "PUT",
        body: JSON.stringify(request)
      });
    },

    deleteUser: async function (
      baseUrl: string,
      auth: string | Cookies
    ): Promise<void> {
      await fetchJSON(`${baseUrl}/user`, auth, { method: "DELETE" });
    }
  },

  events: {
    filter: async function (
      baseUrl: string,
      auth: string | Cookies,
      request: EventFilterRequest
    ): Promise<EventResponse[]> {
      return fetchJSON(`${baseUrl}/event/search`, auth, {
        method: "POST",
        body: JSON.stringify(request)
      });
    },

    getById: async function (
      baseUrl: string,
      auth: string | Cookies,
      eventId: number
    ): Promise<EventResponse> {
      return fetchJSON(`${baseUrl}/event/${eventId}`, auth);
    },

    getByOwner: async function (
      baseUrl: string,
      auth: string | Cookies,
      userId: number,
      all: boolean
    ): Promise<EventListResponse[]> {
      return fetchJSON(
        `${baseUrl}/event/owner/${userId}${all ? "?all=true" : ""}`,
        auth
      );
    },

    create: async function (
      baseUrl: string,
      auth: string | Cookies,
      request: EventRequest
    ): Promise<EventResponse> {
      return fetchJSON(`${baseUrl}/event`, auth, {
        method: "POST",
        body: JSON.stringify(request)
      });
    },

    update: async function (
      baseUrl: string,
      auth: string | Cookies,
      eventId: number,
      request: EventRequest
    ): Promise<EventResponse> {
      return fetchJSON(`${baseUrl}/event/${eventId}`, auth, {
        method: "PUT",
        body: JSON.stringify(request)
      });
    },

    cancel: async function (
      baseUrl: string,
      auth: string | Cookies,
      eventId: number
    ): Promise<void> {
      return fetchJSON(`${baseUrl}/event/${eventId}`, auth, {
        method: "DELETE"
      });
    },

    participants: {
      get: async function (
        baseUrl: string,
        auth: string | Cookies,
        eventId: number
      ): Promise<Record<number, ParticipationState>> {
        return fetchJSON(`${baseUrl}/event/${eventId}/participation`, auth);
      },

      join: async function (
        baseUrl: string,
        auth: string | Cookies,
        eventId: number,
        message: string
      ): Promise<void> {
        const query = message ? `message=${message.trim()}` : "";
        await fetchJSON(
          `${baseUrl}/event/${eventId}/participation?${query}`,
          auth,
          {
            method: "POST",
            body: JSON.stringify({ message })
          }
        );
      },

      manageParticipant: async function (
        baseUrl: string,
        auth: string | Cookies,
        eventId: number,
        userId: number,
        state: ParticipationState
      ): Promise<void> {
        await fetchJSON(`${baseUrl}/event/${eventId}/participation`, auth, {
          method: "PUT",
          body: JSON.stringify({ userId, state })
        });
      },

      withdraw: async function (
        baseUrl: string,
        auth: string | Cookies,
        eventId: number
      ): Promise<void> {
        await fetchJSON(`${baseUrl}/event/${eventId}/participation`, auth, {
          method: "DELETE"
        });
      }
    }
  },

  threads: {
    all: async function (
      baseUrl: string,
      auth: string | Cookies
    ): Promise<ThreadView[]> {
      return fetchJSON(`${baseUrl}/thread`, auth);
    },

    get: async function (
      baseUrl: string,
      auth: string | Cookies,
      id: string
    ): Promise<ThreadResponse> {
      return fetchJSON(`${baseUrl}/thread/${id}`, auth);
    },

    create: async function (
      baseUrl: string,
      auth: string | Cookies,
      request: ThreadRequest
    ): Promise<ThreadResponse> {
      return fetchJSON(`${baseUrl}/thread`, auth, {
        method: "POST",
        body: JSON.stringify(request)
      });
    },

    update: async function (
      baseUrl: string,
      auth: string | Cookies,
      threadId: number,
      updates: Partial<ThreadRequest>
    ): Promise<ThreadResponse> {
      return fetchJSON(`${baseUrl}/thread/${threadId}`, auth, {
        method: "PUT",
        body: JSON.stringify(updates)
      });
    },
    reply: async function (
      baseUrl: string,
      auth: string | Cookies,
      threadId: number,
      message: string
    ): Promise<ThreadReply> {
      return fetchJSON(`${baseUrl}/thread/${threadId}/reply`, auth, {
        method: "POST",
        body: JSON.stringify({ message: message })
      });
    },

    close: async function (
      baseUrl: string,
      auth: string | Cookies,
      threadId: number
    ): Promise<void> {
      await fetchJSON(`${baseUrl}/thread/${threadId}/close`, auth, {
        method: "PUT"
      });
    }
  },

  assets: {
    getProfile: async function (
      baseUrl: string,
      auth: string | Cookies,
      id: number,
      fallback?: string
    ): Promise<string | null> {
      return fetchImage(`${baseUrl}/assets/profile/${id}`, auth, fallback);
    },
    getEvent: async function (
      baseUrl: string,
      auth: string | Cookies,
      id: number,
      fallback?: string
    ): Promise<string | null> {
      return fetchImage(`${baseUrl}/assets/event/${id}`, auth, fallback);
    },
    getBackground: async function (
      baseUrl: string,
      auth: string | Cookies,
      id: number,
      fallback?: string
    ): Promise<string | null> {
      return fetchImage(`${baseUrl}/assets/bg/${id}`, auth, fallback);
    },

    upload: async function (
      baseUrl: string,
      auth: string | Cookies,
      type: "profile" | "event",
      id: number,
      file: File
    ): Promise<void> {
      const formData = new FormData();
      formData.append("file", file);

      await fetchJSON(`${baseUrl}/assets/${type}/${id}`, auth, {
        method: "POST",
        body: formData
      });
    }
  },

  maps: {
    search: async function (query: string): Promise<LocationSearchResult[]> {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}`
      );

      if (!response.ok) {
        throw new Error("Location search failed");
      }

      return response.json();
    },

    reverse: async function (
      lat: number,
      lon: number
    ): Promise<LocationSearchResult> {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}&zoom=10&addressdetails=1`
      );

      if (!response.ok) {
        throw new Error("Reverse geocoding failed");
      }

      return response.json();
    }
  }
};
