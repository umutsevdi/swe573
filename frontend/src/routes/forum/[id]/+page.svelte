<script lang="ts">
  import { ArrowLeft, MessageSquare, Calendar } from "@lucide/svelte";
  import { resolve } from "$app/paths";
  import ForumPost from "$lib/components/ForumPost.svelte";
  import UserAvatar from "$lib/components/UserAvatar.svelte";
  import ThreadModal from "$lib/components/ThreadModal.svelte";
  import { auth } from "$lib/stores/user";
  import { toast } from "$lib/stores/toast";
  import { breadcrumbs } from "$lib/stores/breadcrumbs";
  import { HIVE_API } from "$lib/config";
  import { API } from "$lib/api";

  type ForumPostData = {
    content: string;
    timestamp: string;
    userId: number;
  };

  type ForumTopicData = {
    id: number;
    title: string;
    description: string;
    createdAt: string;
    repliesCount: number;
    tags: string[];
    posts: ForumPostData[];
    userId: number;
    active: boolean;
  };

  let { data } = $props();
  breadcrumbs.set([{ text: "Forum", href: "/forum" }, { text: data.title }]);
  let topic = $state(data as ForumTopicData);

  let newMessageContent = $state("");
  let replyingTo = $state<ForumPostData | null>(null);
  let editModalOpen = $state(false);

  function startReply(post: ForumPostData) {
    replyingTo = post;
    newMessageContent =
      post.content
        .split("\n")
        .map((line) => `>> ${line}`)
        .join("\n") + "\n\n";
  }

  function cancelReply() {
    replyingTo = null;
    newMessageContent = "";
  }

  async function closeThread() {
    try {
      await API.threads.close(HIVE_API, $auth.token!, topic.id);
      toast.success("Thread closed successfully!");
      window.location.reload();
    } catch (error) {
      toast.error(`Failed to close thread. ${error}`);
    }
  }

  async function updateThread(updates: {
    title: string;
    description: string;
    tags: string[];
  }) {
    console.log("Updating thread with:", updates);
    try {
      await API.threads.update(HIVE_API, $auth.token!, topic.id, updates);
      console.log("Thread updated successfully");
      toast.success("Thread updated successfully!");
      window.location.reload();
    } catch (error) {
      toast.error(`Failed to update thread. ${error}`);
    }
  }

  async function handleSubmit(event: Event) {
    event.preventDefault();
    const message = newMessageContent.trim();
    if (!message) {
      toast.error("Message cannot be empty");
      return;
    }

    try {
      const result = await API.threads.reply(
        HIVE_API,
        $auth.token!,
        topic.id,
        message
      );
      toast.success("Reply posted successfully!");
      // Add the new reply to the posts array
      topic.posts = [
        ...topic.posts,
        {
          content: result.message,
          timestamp: new Date(result.createdAt).toLocaleString(),
          userId: result.ownerId
        }
      ];
      topic.repliesCount += 1;
      newMessageContent = "";
      replyingTo = null;
    } catch (error) {
      toast.error(`Failed to post reply. ${error}`);
    }
  }
</script>

{#if topic}
  <div class="hero min-h-screen bg-base-200">
    <div class="hero-content w-full max-w-4xl flex-col">
      <div class="w-full">
        <a href={resolve("/forum")} class="btn mb-4">
          <ArrowLeft />
          Back to Forum
        </a>
        <div class="card mb-6 bg-base-100 shadow-xl">
          <div class="card-body">
            <h1 class="mb-2 card-title text-3xl">{topic.title}</h1>
            <p class="mb-4 text-base-content/70" style="white-space: pre-wrap;">
              {topic.description}
            </p>
            <div class="mb-4 flex items-center gap-3">
              <UserAvatar userId={topic.userId} />
              <div class="flex items-center gap-2 text-sm opacity-70">
                <Calendar />
                <span>{topic.createdAt}</span>
                <MessageSquare />
                <span>{topic.repliesCount} replies</span>
              </div>
            </div>
            <div class="mb-4 flex gap-1">
              {#each topic.tags as tag (tag)}
                <span class="badge badge-outline badge-accent">{tag}</span>
              {/each}
            </div>
            {#if topic.active && $auth.user?.id === topic.userId}
              <div class="flex gap-2">
                <button
                  class="btn btn-warning"
                  onclick={() => (editModalOpen = true)}
                >
                  Update Thread
                </button>
                <button class="btn btn-error" onclick={closeThread}>
                  Close Thread
                </button>
              </div>
            {/if}
          </div>
        </div>
        <div class="space-y-4">
          {#each topic.posts as post, index (index)}
            <ForumPost
              content={post.content}
              timestamp={post.timestamp}
              userId={post.userId}
              onReply={startReply}
            />
          {/each}
        </div>
        {#if topic.active}
          <div class="mt-8">
            <div class="card bg-base-100 shadow-sm">
              <div class="card-body p-4">
                <form onsubmit={handleSubmit}>
                  <textarea
                    class="textarea-bordered textarea mb-2 w-full"
                    placeholder="Write your message..."
                    rows="3"
                    bind:value={newMessageContent}
                    maxlength="400"
                    required
                  ></textarea>
                  <div class="mb-4 text-xs text-base-content/60">
                    {newMessageContent.length} / 400
                  </div>
                  <div class="flex justify-end gap-2">
                    {#if replyingTo}
                      <button
                        class="btn btn-ghost"
                        type="button"
                        onclick={cancelReply}
                      >
                        Cancel
                      </button>
                    {/if}
                    <button class="btn btn-primary" type="submit">
                      <MessageSquare class="mr-2 h-4 w-4" />
                      Post Message
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        {/if}
      </div>
    </div>
  </div>
  <ThreadModal
    mode="edit"
    bind:open={editModalOpen}
    currentTitle={topic.title}
    currentDescription={topic.description}
    currentTags={topic.tags}
    on:update={(e) => updateThread(e.detail)}
  />
{:else}
  <div class="hero flex min-h-screen items-center justify-center bg-base-200">
    <div class="loading loading-lg loading-spinner"></div>
  </div>
{/if}
