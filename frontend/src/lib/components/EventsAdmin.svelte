<script lang="ts">
  import { onMount } from "svelte";
  import { API } from "$lib/api";
  import { HIVE_API } from "$lib/config";
  import { auth } from "$lib/stores/user";
  import type { EventResponse } from "$lib/api";

  let events: EventResponse[] = [];
  let loading = true;
  let error: string | null = null;

  onMount(async () => {
    try {
      const token = $auth.token;
      if (!token) throw new Error("Not authenticated");
      events = await API.admin.events.list(HIVE_API, token);
    } catch (e) {
      error = e instanceof Error ? e.message : "Failed to load events";
    } finally {
      loading = false;
    }
  });

  async function deleteEvent(eventId: number) {
    if (!confirm("Are you sure you want to delete this event?")) return;
    try {
      const token = $auth.token;
      if (!token) throw new Error("Not authenticated");
      await API.admin.events.delete(HIVE_API, token, eventId);
      events = events.filter((e) => e.id !== eventId);
    } catch (e) {
      alert(e instanceof Error ? e.message : "Failed to delete event");
    }
  }

  async function toggleCancelled(eventId: number, currentCancelled: boolean) {
    try {
      const token = $auth.token;
      if (!token) throw new Error("Not authenticated");
      await API.admin.events.update(HIVE_API, token, eventId, {
        cancelled: !currentCancelled
      });
      events = events.map((e) =>
        e.id === eventId ? { ...e, cancelled: !currentCancelled } : e
      );
    } catch (e) {
      alert(e instanceof Error ? e.message : "Failed to update event");
    }
  }
</script>

<div class="card bg-base-100 shadow-xl">
  <div class="card-body">
    <h2 class="card-title">Events Management</h2>

    {#if loading}
      <div class="loading loading-spinner loading-lg"></div>
    {:else if error}
      <div class="alert alert-error">{error}</div>
    {:else}
      <div class="overflow-x-auto">
        <table class="table table-zebra w-full">
          <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>Type</th>
              <th>Owner</th>
              <th>Event Date</th>
              <th>Cancelled</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {#each events as event}
              <tr>
                <td>{event.id}</td>
                <td>{event.title}</td>
                <td>{event.type}</td>
                <td>{event.ownerId}</td>
                <td>{new Date(event.eventDate).toLocaleDateString()}</td>
                <td>
                  <input
                    type="checkbox"
                    class="toggle toggle-primary"
                    checked={event.cancelled}
                    on:change={() => toggleCancelled(event.id, event.cancelled)}
                  />
                </td>
                <td>
                  <button
                    class="btn btn-error btn-sm"
                    on:click={() => deleteEvent(event.id)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </div>
</div>
