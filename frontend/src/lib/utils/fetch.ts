import { error, redirect, type Cookies } from "@sveltejs/kit";
import { browser } from "$app/environment";
import { auth as authStore } from "$lib/stores/user";

/**
 * Fetches JSON data from a URL with authentication.
 * @param url - The URL to fetch.
 * @param auth - The auth token (string) or cookies (Cookies).
 * @param options - Additional fetch options.
 * @returns The JSON data.
 */
export async function fetchJSON(
  url: string,
  auth: string | Cookies,
  options: RequestInit = {}
): Promise<any> {
  let token: string | undefined;
  if (typeof auth === "string") {
    token = auth;
  } else {
    token = auth.get("token");
    if (!token) {
      throw redirect(302, "/auth");
    }
  }

  const isFormData = options.body instanceof FormData;
  const response = await fetch(url, {
    ...options,
    headers: {
      Authorization: `Bearer ${token}`,
      ...(isFormData ? {} : { "Content-Type": "application/json" }),
      ...options.headers
    }
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const body = JSON.stringify(errorData);
    console.error(
      `API Error: ${url} - ${response.status} ${response.statusText} - Payload: ${body}`
    );
    const errorMsg =
      errorData.message || `Failed to fetch: ${response.statusText}`;
    if (typeof auth !== "string" && auth) {
      if (response.status === 401) {
        auth.delete("token", { path: "/" });
        throw redirect(302, "/auth");
      }
      if (response.status === 404) {
        throw error(404, "Not found");
      }
      throw error(response.status, errorMsg);
    }
    if (response.status === 401) {
      // Client-side 401 handling
      if (browser) {
        authStore.logout();
        window.location.href = "/auth";
        return;
      }
    }
    throw new Error(errorMsg);
  }

  const contentLength = response.headers.get("content-length");
  if (contentLength === "0" || response.status === 204) {
    return null;
  }
  return response.json();
}

/**
 * Fetches an authenticated image from a URL.
 * @param url - The URL to fetch.
 * @param auth - The auth token (string) or cookies (Cookies).
 * @param fallback - Optional fallback URL if 404.
 * @returns The blob URL or null.
 */
export async function fetchImage(
  url: string,
  auth: string | Cookies,
  fallback?: string
): Promise<string | null> {
  let token: string | undefined;

  if (typeof auth === "string") {
    token = auth;
  } else {
    token = auth.get("token");
    if (!token) {
      throw redirect(302, "/auth");
    }
  }

  try {
    const response = await fetch(url, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    if (response.status === 401) {
      // Client-side 401 handling
      if (browser) {
        authStore.logout();
        window.location.href = "/auth";
        return null;
      }
    }
    if (response.ok) {
      const blob = await response.blob();
      return URL.createObjectURL(blob);
    } else if (response.status === 404 && fallback) {
      return fallback;
    } else {
      console.error(`Failed to load image: ${url} - ${response.status}`);
      return null;
    }
  } catch (err) {
    console.error("Failed to load image:", err);
    return null;
  }
}
