<script lang="ts">
  import { onMount } from "svelte";
  import { API } from "$lib/api";
  import { HIVE_API } from "$lib/config";
  import { auth } from "$lib/stores/user";
  import type { UserResponse } from "$lib/api";

  let users: UserResponse[] = [];
  let loading = true;
  let error: string | null = null;

  onMount(async () => {
    try {
      const token = $auth.token;
      if (!token) throw new Error("Not authenticated");
      users = await API.admin.users.list(HIVE_API, token);
    } catch (e) {
      error = e instanceof Error ? e.message : "Failed to load users";
    } finally {
      loading = false;
    }
  });

  async function deleteUser(userId: number) {
    if (!confirm("Are you sure you want to delete this user?")) return;
    try {
      const token = $auth.token;
      if (!token) throw new Error("Not authenticated");
      await API.admin.users.delete(HIVE_API, token, userId);
      users = users.filter((u) => u.id !== userId);
    } catch (e) {
      alert(e instanceof Error ? e.message : "Failed to delete user");
    }
  }

  async function toggleActive(userId: number, currentActive: boolean) {
    try {
      const token = $auth.token;
      if (!token) throw new Error("Not authenticated");
      await API.admin.users.update(HIVE_API, token, userId, {
        active: !currentActive
      });
      users = users.map((u) =>
        u.id === userId ? { ...u, active: !currentActive } : u
      );
    } catch (e) {
      alert(e instanceof Error ? e.message : "Failed to update user");
    }
  }
</script>

<div class="card bg-base-100 shadow-xl">
  <div class="card-body">
    <h2 class="card-title">Users Management</h2>

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
              <th>Username</th>
              <th>Email</th>
              <th>Full Name</th>
              <th>Role</th>
              <th>Active</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {#each users as user}
              <tr>
                <td>{user.id}</td>
                <td>{user.username}</td>
                <td>{user.email}</td>
                <td>{user.fullName}</td>
                <td>{user.role}</td>
                <td>
                  <input
                    type="checkbox"
                    class="toggle toggle-primary"
                    checked={user.active}
                    on:change={() => toggleActive(user.id, user.active)}
                  />
                </td>
                <td>
                  <button
                    class="btn btn-error btn-sm"
                    on:click={() => deleteUser(user.id)}
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
