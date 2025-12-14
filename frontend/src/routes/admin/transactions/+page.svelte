<script lang="ts">
  import { goto } from "$app/navigation";
  import { resolve } from "$app/paths";
  import { Search } from "@lucide/svelte";
  import { breadcrumbs } from "$lib/stores/breadcrumbs";
  import UserAvatar from "$lib/components/UserAvatar.svelte";
  import type { TransactionType } from "$lib/api";

  let { data } = $props();
  breadcrumbs.set([
    { text: "Admin", href: "/admin" },
    { text: "Transactions" }
  ]);

  let message = $state(data.message);
  let types = $state(data.types);
  let selectedType = $state<TransactionType | "">("");
  let before = $state(data.before);
  let after = $state(data.after);
  let idType = $state(data.idType || "USER");
  let idValue = $state(data.idValue || "");

  const transactionTypes: TransactionType[] = [
    "USER_REGISTER",
    "USER_LOGIN",
    "USER_UPDATE",
    "USER_DELETE",
    "EVENT_CREATE",
    "EVENT_CANCEL",
    "EVENT_UPDATE",
    "EVENT_COMPLETE",
    "PARTICIPATION_NEW",
    "PARTICIPATION_WITHDRAW",
    "PARTICIPATION_STATUS",
    "PARTICIPATION_TRX",
    "THREAD_CREATE",
    "THREAD_UPDATE",
    "THREAD_CLOSE",
    "THREAD_REPLY",
    "UPDATE_ASSET"
  ];

  // Automatically search for last 7 days transactions if no parameters are provided
  $effect(() => {
    if (
      typeof window !== "undefined" &&
      window.location.search === "" &&
      !data.hasSearched
    ) {
      const today = new Date();
      const sevenDaysAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
      const afterStr = sevenDaysAgo.toISOString().split("T")[0];
      const beforeStr = today.toISOString().split("T")[0];
      const params = new URLSearchParams();
      params.set("after", afterStr);
      params.set("before", beforeStr);
      goto(`?${params.toString()}`, { replaceState: true });
    }
  });

  function addType() {
    if (selectedType && !types.includes(selectedType)) {
      types = [...types, selectedType];
      selectedType = "";
    }
  }

  function removeType(type: string) {
    types = types.filter((t) => t !== type);
  }

  function handleSubmit(event: Event) {
    event.preventDefault();
    const params = new URLSearchParams();
    if (message.trim()) params.set("message", message.trim());
    if (types.length) params.set("types", types.join(","));
    if (before) params.set("before", `${before}T23:59:59`);
    if (after) params.set("after", `${after}T00:00:00`);
    if (idValue.trim()) {
      params.set("id_type", idType);
      params.set("id_value", idValue.trim());
    }
    goto(`?${params.toString()}`, { replaceState: true });
  }
</script>

<div class="hero min-h-screen bg-base-200">
  <div class="hero-content w-full max-w-8xl flex-col">
    <div class="w-full">
      <!-- Filters section -->
      <div class="mb-4 bg-base-100 p-4">
        <form onsubmit={handleSubmit} class="space-y-4">
          <!-- Search Row -->
          <div class="flex flex-col gap-4 lg:flex-row lg:items-center">
            <div class="flex flex-1 items-center gap-2">
              <span class="label-text">Message</span>
              <input
                type="text"
                placeholder="Filter by message..."
                class="input-bordered input flex-1"
                bind:value={message}
              />
            </div>
          </div>
          <div class="flex flex-col gap-4 lg:flex-row lg:items-center">
            <div class="flex items-center gap-2">
              <span class="label-text">Add Type</span>
              <div class="flex flex-1 gap-2">
                <select
                  class="select select-bordered flex-1"
                  bind:value={selectedType}
                >
                  <option value="">Transaction Type</option>
                  {#each transactionTypes as type}
                    <option value={type}>{type}</option>
                  {/each}
                </select>
                <button type="button" class="btn btn-outline" onclick={addType}
                  >+</button
                >
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="label-text">From</span>
              <div class="flex flex-1 gap-2">
                <input
                  type="date"
                  class="input-bordered input"
                  bind:value={after}
                />
                <span class="self-center">to</span>
                <input
                  type="date"
                  class="input-bordered input"
                  bind:value={before}
                />
              </div>
            </div>
            <div class="flex flex-1 items-center gap-2">
              <span class="label-text">ID</span>
              <div class="join flex-1">
                <select
                  class="select select-bordered join-item flex-1"
                  bind:value={idType}
                >
                  <option value="USER">User</option>
                  <option value="EVENT">Event</option>
                  <option value="THREAD">Thread</option>
                </select>
                <input
                  type="number"
                  placeholder="ID"
                  class="input input-bordered join-item flex-1"
                  bind:value={idValue}
                />
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
              {#if message}
                <span class="badge gap-2 badge-primary">
                  Message: {message}
                  <button
                    type="button"
                    class="btn btn-ghost btn-xs"
                    onclick={() => (message = "")}>×</button
                  >
                </span>
              {/if}
              {#each types as type (type)}
                <span class="badge gap-2 badge-outline badge-accent">
                  Type: {type}
                  <button
                    type="button"
                    class="btn btn-ghost btn-xs"
                    onclick={() => removeType(type)}>×</button
                  >
                </span>
              {/each}
              {#if after}
                <span class="badge gap-2 badge-accent">
                  After: {new Date(after).toLocaleString()}
                  <button
                    type="button"
                    class="btn btn-ghost btn-xs"
                    onclick={() => (after = "")}>×</button
                  >
                </span>
              {/if}
              {#if before}
                <span class="badge gap-2 badge-accent">
                  Before: {new Date(before).toLocaleString()}
                  <button
                    type="button"
                    class="btn btn-ghost btn-xs"
                    onclick={() => (before = "")}>×</button
                  >
                </span>
              {/if}
              {#if idValue}
                <span class="badge gap-2 badge-info">
                  ID: {idType}:{idValue}
                  <button
                    type="button"
                    class="btn btn-ghost btn-xs"
                    onclick={() => (idValue = "")}>×</button
                  >
                </span>
              {/if}
            </div>
          </div>
        </form>
      </div>

      <div
        class="overflow-x-auto border border-base-content/5 bg-base-100 h-full"
      >
        <table class="table table-zebra w-full">
          <thead>
            <tr class="bg-base-200">
              <th>Time</th>
              <th>Type</th>
              <th>Source</th>
              <th>Destination</th>
              <th>Message</th>
            </tr>
          </thead>
          <tbody>
            {#each data.transactions as transaction (transaction.type + transaction.source.id + transaction.destination.id)}
              <tr>
                <td>{transaction.createdAt}</td>
                <td>{transaction.type}</td>
                <td>
                  {#if transaction.source.type === "USER"}
                    <UserAvatar
                      userId={transaction.source.id}
                      hideText={true}
                    />
                  {:else if transaction.source.type === "EVENT"}
                    <a
                      class="btn btn-link"
                      href={resolve(`/event/${transaction.source.id}`)}
                      >Event {transaction.source.id}</a
                    >
                  {:else if transaction.source.type === "THREAD"}
                    <a
                      class="btn btn-link btn-accent"
                      href={resolve(`/forum/${transaction.source.id}`)}
                      >Thread {transaction.source.id}</a
                    >
                  {:else}
                    {transaction.source.type}:{transaction.source.id}
                  {/if}
                </td>
                <td>
                  {#if transaction.destination.type === "USER"}
                    <UserAvatar
                      userId={transaction.destination.id}
                      hideText={true}
                    />
                  {:else if transaction.destination.type === "EVENT"}
                    <a
                      class="btn btn-link"
                      href={resolve(`/event/${transaction.destination.id}`)}
                      >Event {transaction.destination.id}</a
                    >
                  {:else if transaction.destination.type === "THREAD"}
                    <a
                      class="btn btn-link btn-accent"
                      href={resolve(`/forum/${transaction.destination.id}`)}
                      >Thread {transaction.destination.id}</a
                    >
                  {:else}
                    {transaction.destination.type}:{transaction.destination.id}
                  {/if}
                </td>
                <td>{transaction.message}</td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>
