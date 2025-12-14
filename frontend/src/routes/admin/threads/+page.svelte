<script lang="ts">
  import { breadcrumbs } from "$lib/stores/breadcrumbs";
  import { Ban } from "@lucide/svelte";
  import { resolve } from "$app/paths";
  import UserAvatar from "$lib/components/UserAvatar.svelte";
  import { API, type ThreadView } from "$lib/api";
  import { HIVE_API } from "$lib/config";
  import { auth } from "$lib/stores/user";
  import { toast } from "$lib/stores/toast";
  import { invalidateAll } from "$app/navigation";

  breadcrumbs.set([{ text: "Admin", href: "/admin" }, { text: "Threads" }]);

  let threads = $state<ThreadView[]>([]);
  let loading = $state(true);
  let showClosed = $state(false);

  async function closeThread(thread: ThreadView) {
    try {
      await API.admin.threads.close(HIVE_API, $auth.token!, thread.id);
      invalidateAll();
      threads = threads
        .filter((view) => view.id === thread.id)
        .map((view) => (view.active = false));
      toast.success(`Thread closed successfully`);
    } catch (error) {
      toast.error("Failed to update thread status. " + error);
    }
  }

  let filteredThreads = $derived(threads.filter((t) => showClosed || t.active));
  $effect(() => {
    if ($auth.token) {
      (async () => {
        loading = true;
        threads = await API.threads.all(HIVE_API!, $auth.token!);
        loading = false;
      })();
    } else {
      loading = false;
    }
  });
</script>

<div class="w-full">
  <div class="mb-4 flex justify-end">
    <label class="label cursor-pointer">
      <span class="label-text mr-2">Show Inactive Threads</span>
      <input
        type="checkbox"
        class="toggle toggle-primary"
        bind:checked={showClosed}
      />
    </label>
  </div>
</div>
<div class="overflow-x-auto border border-base-content/5 bg-base-100 h-full">
  <table class="table table-zebra w-full">
    <thead>
      <tr class="bg-base-200">
        <th>ID</th>
        <th>Title</th>
        <th>Owner</th>
        <th>Date</th>
        <th>Active</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      {#if loading}
        {#each [0, 1, 2, 3, 4] as i (i)}
          <tr>
            <td class="h-12 skeleton skeleton-text"></td>
            <td class="h-12 skeleton skeleton-text"></td>
            <td class="h-12 skeleton skeleton-text"></td>
            <td class="h-12 skeleton skeleton-text"></td>
            <td class="h-12 skeleton skeleton-text"></td>
            <td class="h-12 skeleton skeleton-text"></td>
          </tr>
        {/each}
      {:else}
        {#each filteredThreads as thread (thread.id)}
          <tr>
            <td>
              <a class="btn btn-link" href={resolve(`/forum/${thread.id}`)}
                >{thread.id}</a
              >
            </td>
            <td>{thread.title}</td>
            <td>
              <UserAvatar userId={thread.ownerId} hideText={true} />
            </td>
            <td>{new Date(thread.createdAt).toLocaleDateString()}</td>
            <td>{thread.active}</td>
            <td>
              <button
                class="btn btn-sm btn-error"
                disabled={!thread.active}
                onclick={() => closeThread(thread)}
              >
                <Ban class="h-4 w-4" />
                Close
              </button>
            </td>
          </tr>
        {/each}
      {/if}
    </tbody>
  </table>
</div>
