import { API } from "$lib/api";
import { HIVE_API } from "$lib/serverConfig";
import type { Cookies } from "@sveltejs/kit";

export const load = async ({ cookies }: { cookies: Cookies }) => {
  try {
    const notifications = await API.inbox.list(HIVE_API!, cookies);
    return { notifications };
  } catch (error) {
    console.error("Failed to load notifications:", error);
    return { notifications: [] };
  }
};
