import { HIVE_API } from "$lib/serverConfig";
import { type Cookies } from "@sveltejs/kit";
import { API } from "$lib/api";
import type { ThreadReply } from "$lib/api";

export const load = async ({
  params,
  cookies
}: {
  params: { id: string };
  cookies: Cookies;
}) => {
  console.log("Fetching thread:", `${HIVE_API}/thread/${params.id}`);
  const thread = await API.threads.get(HIVE_API!, cookies, params.id);

  // Transform the server response to match the expected format
  const parsedPosts = (thread.messages || []).map(
    (msg: ThreadReply, index: number) => {
      let content = msg.message.trim();

      if (content.startsWith(">>")) {
        const parts = content.split("\n\n");
        if (parts.length >= 2) {
          const quoted = parts[0].replace(/^>>\s*/, "");
          const reply = parts.slice(1).join("\n\n");
          // Find the parent message
          for (let i = index - 1; i >= 0; i--) {
            if (thread.messages?.[i]?.message.trim() === quoted) {
              content = reply;
              break;
            }
          }
        }
      }

      return {
        userId: msg.ownerId,
        content,
        timestamp: new Date(msg.createdAt).toLocaleString()
      };
    }
  );
  return {
    id: thread.id,
    title: thread.title,
    description: thread.description,
    createdAt: new Date(thread.createdAt).toLocaleDateString(),
    repliesCount: thread.messages?.length || 0,
    tags: thread.tags,
    posts: parsedPosts,
    userId: thread.ownerId,
    active: thread.active
  };
};
