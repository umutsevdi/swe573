import { writable } from "svelte/store";
import { browser } from "$app/environment";
import type { UserCard } from "$lib/api";

export interface AuthState {
  token: string | null;
  user: UserCard | null;
  isLoggedIn: boolean;
}

function createAuthStore() {
  const initialState: AuthState = {
    token: null,
    user: null,
    isLoggedIn: false
  };

  const { subscribe, set, update } = writable<AuthState>(initialState);

  // Initialize from localStorage if in browser
  if (browser) {
    const token = localStorage.getItem("token");
    const userStr = localStorage.getItem("user");
    let user: UserCard | null = null;
    if (userStr) {
      try {
        user = JSON.parse(userStr);
      } catch (e) {
        console.error("Failed to parse user from localStorage", e);
      }
    }

    // Also set the cookie for server-side access
    if (token) {
      const expires = new Date();
      expires.setDate(expires.getDate() + 7);
      document.cookie = `token=${encodeURIComponent(token)}; expires=${expires.toUTCString()}; path=/; SameSite=Lax`;
    }

    set({
      token,
      user,
      isLoggedIn: !!token
    });
  }

  return {
    subscribe,
    login: (token: string, user: UserCard) => {
      localStorage.setItem("token", token);
      localStorage.setItem("user", JSON.stringify(user));

      // Set cookie for server-side access (expires in 7 days)
      if (browser) {
        const expires = new Date();
        expires.setDate(expires.getDate() + 7);
        const cookieValue = `token=${encodeURIComponent(token)}; expires=${expires.toUTCString()}; path=/; SameSite=Lax`;
        document.cookie = cookieValue;
      }

      set({ token, user, isLoggedIn: true });
    },
    logout: () => {
      localStorage.removeItem("token");
      localStorage.removeItem("user");

      // Clear cookie
      if (browser) {
        document.cookie =
          "token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
      }

      set({ token: null, user: null, isLoggedIn: false });
    },
    update
  };
}

export const auth = createAuthStore();

// Global cache for user card data
export const userCardCache = writable<Map<number, UserCard>>(new Map());

// Pending promises to prevent duplicate fetches
export const userDataPromises = writable<Map<number, Promise<UserCard>>>(
  new Map()
);
export const avatarPromises = writable<Map<number, Promise<string>>>(new Map());

// Cached avatar URLs (blob URLs or static paths)
export const avatarUrls = writable<Map<number, string>>(new Map());
