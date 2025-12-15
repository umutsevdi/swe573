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
  suspended: boolean;
  role: string;
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
  suspended: boolean;
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
  cancelled?: boolean;
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
  active?: boolean;
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

export type TransactionType =
  | "USER_REGISTER"
  | "USER_LOGIN"
  | "USER_UPDATE"
  | "USER_DELETE"
  | "EVENT_CREATE"
  | "EVENT_CANCEL"
  | "EVENT_UPDATE"
  | "EVENT_COMPLETE"
  | "PARTICIPATION_NEW"
  | "PARTICIPATION_WITHDRAW"
  | "PARTICIPATION_STATUS"
  | "PARTICIPATION_TRX"
  | "THREAD_CREATE"
  | "THREAD_UPDATE"
  | "THREAD_CLOSE"
  | "THREAD_REPLY"
  | "UPDATE_ASSET"
  | "ADMIN_SUSPEND_USER"
  | "ADMIN_ASSIGN_ROLE";
export interface Identifier {
  id: number;
  type: "USER" | "EVENT" | "THREAD";
}

export interface TransactionRequest {
  type?: TransactionType[];
  id?: Identifier;
  message?: string;
  before?: string;
  after?: string;
}
export interface TransactionResponse {
  type: TransactionType;
  source: Identifier;
  destination: Identifier;
  message: string;
  createdAt: string;
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

export interface Notification {
  id: number;
  ownerId: number;
  type:
    | "EVENT_EDIT"
    | "EVENT_NEGOTIATING"
    | "EVENT_ACCEPTED"
    | "EVENT_REJECTED"
    | "EVENT_CANCELLED"
    | "EVENT_COMPLETED"
    | "THREAD_REPLY"
    | "THREAD_CLOSED"
    | "USER_ROLE_UPDATE";
  message: string;
  isRead: boolean;
  createdAt: string;
  redirectTo: string;
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

    submitReview: async function (
      baseUrl: string,
      auth: string | Cookies,
      eventId: number,
      request: {
        rating: number;
        deliveryStatus: "UNDERDELIVERED" | "DELIVERED" | "OVERDELIVERED";
        message: string;
      }
    ): Promise<void> {
      return fetchJSON(`${baseUrl}/event/${eventId}/review`, auth, {
        method: "POST",
        body: JSON.stringify(request)
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
  },

  inbox: {
    list: async function (
      baseUrl: string,
      auth: string | Cookies
    ): Promise<Notification[]> {
      return fetchJSON(`${baseUrl}/inbox`, auth);
    },
    count: async function (
      baseUrl: string,
      auth: string | Cookies
    ): Promise<number> {
      return fetchJSON(`${baseUrl}/inbox/count`, auth);
    },
    markAsRead: async function (
      baseUrl: string,
      auth: string | Cookies,
      notificationId: number
    ): Promise<void> {
      await fetchJSON(`${baseUrl}/inbox/${notificationId}/read`, auth, {
        method: "PUT"
      });
    },
    readAll: async function (
      baseUrl: string,
      auth: string | Cookies
    ): Promise<void> {
      await fetchJSON(`${baseUrl}/inbox/read-all`, auth, {
        method: "PUT"
      });
    }
  },

  admin: {
    users: {
      list: async function (
        baseUrl: string,
        auth: string | Cookies
      ): Promise<UserCard[]> {
        return fetchJSON(`${baseUrl}/admin/users`, auth);
      },

      assignRole: async function (
        baseUrl: string,
        auth: string | Cookies,
        userId: number,
        role: "MEMBER" | "ADMIN"
      ): Promise<void> {
        await fetchJSON(`${baseUrl}/admin/users/${userId}?role=${role}`, auth, {
          method: "PUT"
        });
      },

      terminate: async function (
        baseUrl: string,
        auth: string | Cookies,
        userId: number,
        suspend: boolean
      ): Promise<void> {
        await fetchJSON(
          `${baseUrl}/admin/users/${userId}?suspend=${suspend}`,
          auth,
          {
            method: "DELETE"
          }
        );
      }
    },

    events: {
      close: async function (
        baseUrl: string,
        auth: string | Cookies,
        eventId: number
      ): Promise<void> {
        await fetchJSON(`${baseUrl}/admin/events/${eventId}`, auth, {
          method: "DELETE"
        });
      }
    },

    threads: {
      close: async function (
        baseUrl: string,
        auth: string | Cookies,
        threadId: number
      ): Promise<void> {
        await fetchJSON(`${baseUrl}/admin/threads/${threadId}`, auth, {
          method: "DELETE"
        });
      }
    },

    transactions: {
      filter: async function (
        baseUrl: string,
        auth: string | Cookies,
        request: TransactionRequest
      ): Promise<TransactionResponse[]> {
        return fetchJSON(`${baseUrl}/admin/transactions`, auth, {
          method: "POST",
          body: JSON.stringify(request)
        });
      }
    }
  }
};
