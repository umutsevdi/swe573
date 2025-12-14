<script lang="ts">
  import { onMount } from "svelte";
  import { API } from "$lib/api";
  import { HIVE_API } from "$lib/config";
  import { auth } from "$lib/stores/user";
  import type { ThreadView } from "$lib/api";

  let threads: ThreadView[] = [];
  let loading = true;
  let error: string | null = null;

  onMount(async () => {
    try {
      const token = $auth.token;
      if (!token) throw new Error("Not authenticated");
      threads = await API.admin.threads.list(HIVE_API, token);
    } catch (e) {
      error = e instanceof Error ? e.message : "Failed to load threads";
    } finally {
      loading = false;
    }
  });

  async function deleteThread(threadId: number) {
    if (!confirm("Are you sure you want to delete this thread?")) return;
    try {
      const token = $auth.token;
      if (!token) throw new Error("Not authenticated");
      await API.admin.threads.delete(HIVE_API, token, threadId);
      threads = threads.filter((t) => t.id !== threadId);
    } catch (e) {
      alert(e instanceof Error ? e.message : "Failed to delete thread");
    }
  }

  async function toggleActive(threadId: number, currentActive: boolean) {
    try {
      const token = $auth.token;
      if (!token) throw new Error("Not authenticated");
      await API.admin.threads.update(HIVE_API, token, threadId, {
        active: !currentActive
      });
      threads = threads.map((t) =>
        t.id === threadId ? { ...t, active: !currentActive } : t
      );
    } catch (e) {
      alert(e instanceof Error ? e.message : "Failed to update thread");
    }
  }
</script>

<div class="card bg-base-100 shadow-xl">
  <div class="card-body">
    <h2 class="card-title">Threads Management</h2>

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
              <th>Owner</th>
              <th>Created At</th>
              <th>Active</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {#each threads as thread}
              <tr>
                <td>{thread.id}</td>
                <td>{thread.title}</td>
                <td>{thread.ownerId}</td>
                <td>{new Date(thread.createdAt).toLocaleDateString()}</td>
                <td>
                  <input
                    type="checkbox"
                    class="toggle toggle-primary"
                    checked={thread.active}
                    on:change={() => toggleActive(thread.id, thread.active)}
                  />
                </td>
                <td>
                  <button
                    class="btn btn-error btn-sm"
                    on:click={() => deleteThread(thread.id)}
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
