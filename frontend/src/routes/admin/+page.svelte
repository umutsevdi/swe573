<script lang="ts">
  import { onMount } from "svelte";
  import { breadcrumbs } from "$lib/stores/breadcrumbs";

  breadcrumbs.set([{ text: "Admin" }]);
  import { API } from "$lib/api";
  import { HIVE_API } from "$lib/config";
  import { auth } from "$lib/stores/user";
  import { toast } from "$lib/stores/toast";
  import type { UserCard } from "$lib/api";
  import UserAvatar from "$lib/components/UserAvatar.svelte";

  let currentUser: UserCard = $derived($auth.user!);

  let users: UserCard[] = $state([]);
  let loading = $state(true);
  let error: string | null = $state(null);

  onMount(async () => {
    try {
      users = await API.admin.users.list(HIVE_API, $auth.token!);
    } catch (e) {
      error = e instanceof Error ? e.message : "Failed to load users";
      toast.error(error);
    } finally {
      loading = false;
    }
  });

  async function toggleSuspend(userId: number, suspend: boolean) {
    try {
      await API.admin.users.terminate(HIVE_API, $auth.token!, userId, suspend);
      users = users.map((u) =>
        u.id === userId ? { ...u, suspended: suspend } : u
      );
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to update user");
    }
  }

  async function assignRole(userId: number, role: "MEMBER" | "ADMIN") {
    try {
      await API.admin.users.assignRole(HIVE_API, $auth.token!, userId, role);
      users = users.map((u) => (u.id === userId ? { ...u, role: role } : u));
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to update user");
    }
  }
</script>

{#if loading}
  <div class="loading loading-spinner loading-lg"></div>
{:else if error}
  <p class="text-error">Failed to load users.</p>
{:else}
  <div class="overflow-x-auto">
    <table class="table table-zebra w-full">
      <thead>
        <tr>
          <th>Account</th>
          <th>Full Name</th>
          <th>User Score</th>
          <th>Role</th>
          <th>Suspend</th>
        </tr>
      </thead>
      <tbody>
        {#each users as user (user.id)}
          <tr>
            <td>
              <UserAvatar userId={user.id} hideText={true} />
            </td>
            <td>{user.fullName}</td>
            <td>{user.userScore}</td>
            <td>
              <select
                class="select select-bordered join-item flex-1"
                onchange={(e) =>
                  assignRole(
                    user.id,
                    e.currentTarget.value as "MEMBER" | "ADMIN"
                  )}
                bind:value={user.role}
                disabled={user.role === "OWNER" || currentUser.role !== "OWNER"}
              >
                <option value="MEMBER">Member</option>
                <option value="ADMIN">Admin</option>
                <option disabled={true} value="OWNER">Owner</option>
              </select>
            </td>
            <td>
              <input
                type="checkbox"
                class="toggle toggle-primary"
                disabled={user.role !== "MEMBER"}
                checked={user.suspended}
                onchange={() => toggleSuspend(user.id, !user.suspended)}
              />
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  </div>
{/if}
