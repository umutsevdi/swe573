<script lang="ts">
  import { goto } from "$app/navigation";
  import { page } from "$app/stores";
  import { Search } from "@lucide/svelte";
  import {
    API,
    type TransactionRequest,
    type TransactionType,
    type Identifier,
    type TransactionResponse
  } from "$lib/api";
  import { HIVE_API } from "$lib/config";
  import { auth } from "$lib/stores/user";

  let transactions = $state<TransactionResponse[]>([]);
  let loading = $state(false);
  let error = $state<string | null>(null);

  // Filter state - read from URL params
  let selectedTypes: TransactionType[] = $state([]);
  let messageFilter = $state("");
  let before = $state("");
  let after = $state("");
  let idFilter: Identifier | null = $state(null);

  let selectedTypeToAdd = $state("");

  // Update state from URL params
  $effect(() => {
    const params = new URLSearchParams($page.url.search);
    messageFilter = params.get("message") || "";
    before = params.get("before") || "";
    after = params.get("after") || "";
    const typesParam = params.get("types");
    selectedTypes = typesParam
      ? (typesParam.split(",").filter(Boolean) as TransactionType[])
      : [];
    const idType = params.get("id_type");
    const idValue = params.get("id_value");
    if (idType && idValue) {
      idFilter = {
        type: idType as "USER" | "EVENT" | "THREAD",
        id: parseInt(idValue)
      };
    } else {
      idFilter = null;
    }
  });

  // Available transaction types
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

  function updateUrlParams() {
    const url = new URL($page.url);
    // Clear existing transaction params
    url.searchParams.delete("message");
    url.searchParams.delete("before");
    url.searchParams.delete("after");
    url.searchParams.delete("types");
    url.searchParams.delete("id_type");
    url.searchParams.delete("id_value");

    // Set new params
    if (messageFilter.trim())
      url.searchParams.set("message", messageFilter.trim());
    if (before) url.searchParams.set("before", before);
    if (after) url.searchParams.set("after", after);
    if (selectedTypes.length > 0)
      url.searchParams.set("types", selectedTypes.join(","));
    if (idFilter) {
      url.searchParams.set("id_type", idFilter.type);
      url.searchParams.set("id_value", idFilter.id.toString());
    }

    goto(url.toString(), { replaceState: true });
  }

  async function handleFilterSubmit() {
    updateUrlParams();
    // Also load immediately to avoid waiting for URL change to trigger effects
    await loadTransactions();
  }

  function addType(type: TransactionType) {
    if (!selectedTypes.includes(type)) {
      selectedTypes = [...selectedTypes, type];
      handleFilterSubmit();
    }
  }

  function removeType(type: TransactionType) {
    selectedTypes = selectedTypes.filter((t) => t !== type);
    handleFilterSubmit();
  }

  function removeIdFilter() {
    idFilter = null;
    handleFilterSubmit();
  }

  async function loadTransactions() {
    try {
      loading = true;
      error = null;
      const token = $auth.token;
      if (!token) throw new Error("Not authenticated");

      const request: TransactionRequest = {
        before: before || new Date().toISOString(),
        after:
          after || new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString() // 30 days ago
      };

      if (selectedTypes.length > 0) request.type = selectedTypes;
      if (messageFilter.trim()) request.message = messageFilter.trim();
      if (idFilter) request.id = idFilter;

      transactions = await API.admin.transactions.filter(
        HIVE_API,
        token,
        request
      );
    } catch (e) {
      error = e instanceof Error ? e.message : "Failed to load transactions";
    } finally {
      loading = false;
    }
  }

  // Load transactions when filters change
  $effect(() => {
    if (
      $auth.token &&
      messageFilter !== undefined &&
      before !== undefined &&
      after !== undefined &&
      selectedTypes !== undefined &&
      idFilter !== undefined
    ) {
      loadTransactions();
    }
  });
</script>

<div class="card bg-base-100 shadow-xl">
  <div class="card-body">
    <h2 class="card-title">Transactions Management</h2>

    <!-- Filters section -->
    <div class="mb-4 bg-base-100 p-4">
      <form
        onsubmit={(e) => {
          e.preventDefault();
          handleFilterSubmit();
        }}
        class="space-y-4"
      >
        <!-- First Row: Types and Message -->
        <div class="flex flex-col gap-4 lg:flex-row lg:items-center">
          <div class="flex flex-1 items-center gap-2">
            <span class="label-text">Message</span>
            <input
              type="text"
              placeholder="Filter by message..."
              class="input-bordered input flex-1"
              bind:value={messageFilter}
            />
          </div>
          <div class="flex flex-1 items-center gap-2">
            <span class="label-text">Add Transaction Type</span>
            <select
              class="select select-bordered flex-1"
              bind:value={selectedTypeToAdd}
            >
              <option value="" disabled selected>Select type...</option>
              {#each transactionTypes as type (type)}
                <option value={type}>{type}</option>
              {/each}
            </select>
            <button
              type="button"
              class="btn btn-outline"
              onclick={() => {
                if (selectedTypeToAdd) {
                  addType(selectedTypeToAdd as TransactionType);
                  selectedTypeToAdd = "";
                }
              }}>+</button
            >
          </div>
        </div>

        <!-- Second Row: Date Range and Types -->
        <div class="flex flex-col gap-4 lg:flex-row lg:items-center">
          <div class="flex flex-1 items-center gap-2">
            <span class="label-text">From</span>
            <input
              type="datetime-local"
              class="input-bordered input flex-1"
              bind:value={after}
            />
            <span class="label-text">to</span>
            <input
              type="datetime-local"
              class="input-bordered input flex-1"
              bind:value={before}
            />
          </div>

          <button type="submit" class="btn btn-primary">
            <Search class="h-4 w-4" />
            Filter
          </button>
        </div>

        <!-- Active filters display -->
        <div class="mt-4">
          <div class="flex flex-wrap gap-2">
            {#if messageFilter}
              <span class="badge gap-2 badge-primary">
                Message: {messageFilter}
                <button
                  type="button"
                  class="btn btn-ghost btn-xs"
                  onclick={() => {
                    messageFilter = "";
                    handleFilterSubmit();
                  }}>×</button
                >
              </span>
            {/if}
            {#if idFilter}
              <span class="badge gap-2 badge-outline badge-accent">
                ID: {idFilter.type}:{idFilter.id}
                <button
                  type="button"
                  class="btn btn-ghost btn-xs"
                  onclick={() => {
                    removeIdFilter();
                  }}>×</button
                >
              </span>
            {/if}
            {#each selectedTypes as type (type)}
              <span class="badge gap-2 badge-secondary">
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
                  onclick={() => {
                    after = "";
                    handleFilterSubmit();
                  }}>×</button
                >
              </span>
            {/if}
            {#if before}
              <span class="badge gap-2 badge-accent">
                Before: {new Date(before).toLocaleString()}
                <button
                  type="button"
                  class="btn btn-ghost btn-xs"
                  onclick={() => {
                    before = "";
                    handleFilterSubmit();
                  }}>×</button
                >
              </span>
            {/if}
          </div>
        </div>
      </form>
    </div>

    {#if loading}
      <div class="loading loading-spinner loading-lg"></div>
    {:else if error}
      <div class="alert alert-error">{error}</div>
    {:else}
      <div class="overflow-x-auto">
        <table class="table table-zebra w-full">
          <thead>
            <tr>
              <th>Type</th>
              <th>Source → Destination</th>
              <th>Message</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {#each transactions as transaction (transaction.type + transaction.source.id + transaction.destination.id)}
              <tr>
                <td>{transaction.type}</td>
                <td
                  >{transaction.source.type}:{transaction.source.id} → {transaction
                    .destination.type}:{transaction.destination.id}</td
                >
                <td>{transaction.message}</td>
                <td>
                  <!-- Actions if needed -->
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </div>
</div>
