<script lang="ts">
  import { goto, invalidateAll } from "$app/navigation";
  import { resolve } from "$app/paths";
  import { Search, Ban } from "@lucide/svelte";
  import { breadcrumbs } from "$lib/stores/breadcrumbs";
  import UserAvatar from "$lib/components/UserAvatar.svelte";
  import { API, type EventResponse } from "$lib/api";
  import { HIVE_API } from "$lib/config";
  import { auth } from "$lib/stores/user";
  import { toast } from "$lib/stores/toast";

  let { data } = $props();
  breadcrumbs.set([{ text: "Admin", href: "/admin" }, { text: "Events" }]);

  let q = $state(data.q);
  let tags = $state(data.tags);
  let tagInput = $state("");
  let start = $state(data.start);
  let end = $state(data.end);
  let type = $state(data.type);
  let includeCancelled = $state(data.includeCancelled);

  $effect(() => {
    if (typeof window !== "undefined" && window.location.search === "") {
      const today = new Date().toISOString().split("T")[0];
      const params = new URLSearchParams();
      params.set("start", today);
      goto(`?${params.toString()}`, { replaceState: true });
    }
  });

  function addTag() {
    const trimmed = tagInput
      .trim()
      .toLowerCase()
      .replace(/\s+/g, "-")
      .replace(/[^a-z0-9-]/g, "");
    if (trimmed && !tags.includes(trimmed)) {
      tags = [...tags, trimmed];
      tagInput = "";
    }
  }

  function removeTag(tag: string) {
    tags = tags.filter((t) => t !== tag);
  }
  function handleSubmit(event: Event) {
    event.preventDefault();
    const params = new URLSearchParams();
    if (q.trim()) params.set("q", q.trim());
    if (tags.length) params.set("tags", tags.join(","));
    if (start) params.set("start", start);
    if (end) params.set("end", end);
    if (type) params.set("type", type);
    if (includeCancelled) params.set("includeCancelled", "true");
    goto(`?${params.toString()}`, { replaceState: true });
  }

  async function cancelEvent(event: EventResponse) {
    try {
      await API.admin.events.close(HIVE_API, $auth.token!, event.id);
      invalidateAll();
      toast.success(`Event cancelled successfully`);
    } catch (error) {
      console.error("Error toggling event status:", error);
      toast.error("Failed to update event status. "+ error);
    }
  }
</script>

<!-- Filters section -->
<div class="mb-4 bg-base-100 p-4">
  <form onsubmit={handleSubmit} class="space-y-4">
    <!-- Search Row -->
    <div class="flex flex-col gap-4 lg:flex-row lg:items-center">
      <div class="flex flex-1 items-center gap-2">
        <span class="label-text">Search</span>
        <input
          type="text"
          placeholder="Search events..."
          class="input-bordered input flex-1"
          bind:value={q}
        />
      </div>
      <div class="flex flex-1 items-center gap-2">
        <span class="label-text">Add Tag</span>
        <div class="flex flex-1 gap-2">
          <input
            type="text"
            placeholder="Enter tag..."
            class="input-bordered input flex-1"
            bind:value={tagInput}
            onkeydown={(e) => {
              if (e.key === "Enter") {
                e.preventDefault();
                addTag();
              }
            }}
          />
          <button type="button" class="btn btn-outline" onclick={addTag}
            >+</button
          >
        </div>
      </div>
      <div class="flex items-center gap-2">
        <span class="label-text">Type</span>
        <div class="join">
          <input
            class="btn join-item {type === 'OFFER'
              ? 'btn-success'
              : 'btn-outline'}"
            type="radio"
            value="OFFER"
            bind:group={type}
            aria-label="OFFER"
            onclick={() => {
              if (type === "OFFER") type = "";
            }}
          />
          <input
            class="btn join-item {type === 'NEED'
              ? 'btn-accent'
              : 'btn-outline'}"
            type="radio"
            value="NEED"
            bind:group={type}
            aria-label="NEED"
            onclick={() => {
              if (type === "NEED") type = "";
            }}
          />
        </div>
      </div>
      <div class="flex items-center gap-2">
        <label class="label cursor-pointer">
          <span class="label-text mr-2">All</span>
          <input
            type="checkbox"
            class="toggle toggle-primary"
            bind:checked={includeCancelled}
          />
        </label>
      </div>
    </div>

    <!-- Filters Row -->
    <div class="flex flex-col gap-4 lg:flex-row lg:items-center">
      <div class="flex flex-1 items-center gap-2">
        <span class="label-text">From</span>
        <div class="flex flex-1 gap-2">
          <input type="date" class="input-bordered input" bind:value={start} />
          <span class="self-center">to</span>
          <input type="date" class="input-bordered input" bind:value={end} />
        </div>
      </div>
      <button type="submit" class="btn btn-primary">
        <Search class="h-4 w-4" />
        Search
      </button>
    </div>

    <!-- Active filters display -->
    <div class="mt-4">
      <div class="flex flex-wrap gap-2">
        {#if q}
          <span class="badge gap-2 badge-primary">
            Search: {q}
            <button
              type="button"
              class="btn btn-ghost btn-xs"
              onclick={() => (q = "")}>×</button
            >
          </span>
        {/if}
        {#each tags as tag (tag)}
          <span class="badge gap-2 badge-outline badge-accent">
            Tag: {tag}
            <button
              type="button"
              class="btn btn-ghost btn-xs"
              onclick={() => removeTag(tag)}>×</button
            >
          </span>
        {/each}
        {#if start}
          <span class="badge gap-2 badge-accent">
            Start: {start}
            <button
              type="button"
              class="btn btn-ghost btn-xs"
              onclick={() => (start = "")}>×</button
            >
          </span>
        {/if}
        {#if end}
          <span class="badge gap-2 badge-accent">
            End: {end}
            <button
              type="button"
              class="btn btn-ghost btn-xs"
              onclick={() => (end = "")}>×</button
            >
          </span>
        {/if}
        {#if type}
          <span class="badge gap-2 badge-secondary">
            Type: {type}
            <button
              type="button"
              class="btn btn-ghost btn-xs"
              onclick={() => (type = "")}>×</button
            >
          </span>
        {/if}
        {#if includeCancelled}
          <span class="badge gap-2 badge-warning">
            All
            <button
              type="button"
              class="btn btn-ghost btn-xs"
              onclick={() => (includeCancelled = false)}>×</button
            >
          </span>
        {/if}
      </div>
    </div>
  </form>
</div>

<div class="overflow-x-auto border border-base-content/5 bg-base-100 h-full">
  <table class="table table-zebra w-full">
    <thead>
      <tr class="bg-base-200">
        <th>ID</th>
        <th>Title</th>
        <th>Owner</th>
        <th>Type</th>
        <th>Date</th>
        <th>Location</th>
        <th>Participants</th>
        <th>Cancelled</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      {#each data.events as event (event.id)}
        {@const eventDate = new Date(event.eventDate)}
        {@const today = new Date()}
        {@const todayStart = new Date(
          today.getFullYear(),
          today.getMonth(),
          today.getDate()
        )}
        {@const isImmutable = event.cancelled || eventDate < todayStart}
        <tr>
          <td>
            <a class="btn btn-link" href={resolve(`/event/${event.id}`)}
              >{event.id}</a
            >
          </td>
          <td>{event.title}</td>
          <td>
            <UserAvatar userId={event.ownerId} hideText={true} />
          </td>
          <td>
            <span
              class="badge {event.type === 'OFFER'
                ? 'badge-success'
                : 'badge-accent'}"
            >
              {event.type}
            </span>
          </td>
          <td>{new Date(event.eventDate).toLocaleDateString()}</td>
          <td>{event.location.name}</td>
          <td>{event.maxParticipants}</td>
          <td> {event.cancelled} </td>
          <td>
            <button
              class="btn btn-sm btn-error"
              disabled={isImmutable}
              onclick={() => cancelEvent(event)}
            >
              <Ban class="h-4 w-4" />
              Cancel
            </button>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>
