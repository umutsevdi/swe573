<script lang="ts">
  import ThreadsAdmin from "$lib/components/ThreadsAdmin.svelte";
  import { breadcrumbs } from "$lib/stores/breadcrumbs";

  breadcrumbs.set([{ text: "Admin", href: "/admin" }, { text: "Threads" }]);
</script>

<div class="container mx-auto p-4">
  <ThreadsAdmin />
</div>
