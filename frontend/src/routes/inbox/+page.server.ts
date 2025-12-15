import { API } from "$lib/api";
import { HIVE_API } from "$lib/serverConfig";

export const load = async ({ cookies }: { cookies: any }) => {
  try {
    const notifications = await API.inbox.list(HIVE_API!, cookies);
    return { notifications };
  } catch (error) {
    console.error("Failed to load notifications:", error);
    return { notifications: [] };
  }
};
