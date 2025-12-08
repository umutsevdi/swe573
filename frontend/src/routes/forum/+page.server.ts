import { HIVE_API } from "$lib/serverConfig";
import { type Cookies } from "@sveltejs/kit";
import { validateRequired } from "$lib/utils/actions";
import { API } from "$lib/api";
import type { ThreadView } from "$lib/api";

export const load = async ({ cookies }: { cookies: Cookies }) => {
  const threads: ThreadView[] = await API.threads.all(HIVE_API!, cookies);

  // Transform tagsList: split by comma and flatten
  const transformedThreads = threads.map((thread) => ({
    ...thread,
    tags: thread.tagsList.flatMap((tag) => tag.split(",")).map((t) => t.trim())
  }));

  return { threads: transformedThreads };
};

export const actions = {
  create: async ({
    request,
    cookies
  }: {
    request: Request;
    cookies: Cookies;
  }) => {
    const data = await request.formData();
    const title = data.get("title") as string;
    const description = data.get("description") as string;
    const tags = JSON.parse(data.get("tags") as string) as string[];

    const validationError = validateRequired({ title, description });
    if (validationError) {
      return { success: false, error: validationError };
    }

    try {
      const result = await API.threads.create(HIVE_API!, cookies, {
        title: title.trim(),
        description: description.trim(),
        tags
      });
      return { success: true, thread: result };
    } catch (error) {
      console.error("Error creating thread:", error);
      return { success: false, error: "Failed to create thread" };
    }
  }
};
