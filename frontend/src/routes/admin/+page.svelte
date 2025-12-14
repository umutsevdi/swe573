<script lang="ts">
  import UsersAdmin from "$lib/components/UsersAdmin.svelte";
  import { breadcrumbs } from "$lib/stores/breadcrumbs";

  breadcrumbs.set([{ text: "Admin" }]);
</script>

<div class="container mx-auto p-4">
  <h1 class="text-3xl font-bold mb-6">Admin Panel</h1>

  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
    <a href="/admin" class="btn btn-outline btn-lg">
      <div class="text-center">
        <div class="text-2xl mb-2">👥</div>
        <div>Users</div>
      </div>
    </a>
    <a href="/admin/events" class="btn btn-outline btn-lg">
      <div class="text-center">
        <div class="text-2xl mb-2">📅</div>
        <div>Events</div>
      </div>
    </a>
    <a href="/admin/threads" class="btn btn-outline btn-lg">
      <div class="text-center">
        <div class="text-2xl mb-2">💬</div>
        <div>Threads</div>
      </div>
    </a>
    <a href="/admin/transactions" class="btn btn-outline btn-lg">
      <div class="text-center">
        <div class="text-2xl mb-2">📊</div>
        <div>Transactions</div>
      </div>
    </a>
  </div>

  <UsersAdmin />
</div>
