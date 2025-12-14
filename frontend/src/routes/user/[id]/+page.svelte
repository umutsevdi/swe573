<script lang="ts">
  import {
    Annoyed,
    Frown,
    HandHelping,
    HeartHandshake,
    Meh,
    Smile,
    Hourglass,
    Pen,
    AlertTriangleIcon,
    AlertTriangle,
    TriangleAlert
  } from "@lucide/svelte";
  import { breadcrumbs } from "$lib/stores/breadcrumbs";

  import type { PageProps } from "./$types";
  import SearchResultCard from "$lib/components/SearchResultCard.svelte";
  import ProfileModal from "$lib/components/ProfileModal.svelte";
  import { HIVE_API } from "$lib/config";
  import { auth } from "$lib/stores/user";
  import { page } from "$app/stores";
  import { toast } from "$lib";
  import { API } from "$lib/api";

  let { data }: PageProps = $props();

  breadcrumbs.set([{ text: "User" }, { text: data.fullName }]);

  let biography = $state(data?.biography || "");
  let interests = $state(data?.interests || []);
  let editModalOpen = $state(false);

  const badgeColors = ["text-success", "text-warning", "text-error"];
  const bgColor = (i: number) =>
    i > 70 ? badgeColors[0] : i > 49 ? badgeColors[1] : badgeColors[2];

  let activeTab = $state("offers");
  let avatarBlobUrl = $state<string | null>(null);

  let showAll = $state(false);
  let events = $state<EventData[]>([]);
  let loading = $state(true);

  async function uploadAsset(
    file: File | undefined,
    type: "profile",
    reload: () => Promise<void>
  ) {
    if (!file) return;

    await API.assets.upload(
      HIVE_API as string,
      $auth.token as string,
      type,
      Number($page.params.id),
      file
    );

    await reload();
  }

  async function handleProfileUpdate(
    event: CustomEvent<{
      biography: string;
      interests: string[];
      avatar?: File;
    }>
  ) {
    const {
      biography: newBiography,
      interests: newInterests,
      avatar: newAvatar
    } = event.detail;

    try {
      // Always send both biography and interests as per API spec
      const updateData = {
        biography: newBiography,
        interests: newInterests
      };

      const updatedData = await API.users.updateUser(
        HIVE_API as string,
        $auth.token as string,
        updateData
      );
      // Update local state with server response
      biography = updatedData.biography;
      interests = updatedData.interests;

      // Upload assets if changed
      await uploadAsset(newAvatar, "profile", loadAvatar);

      // Success
      toast.success("Profile updated successfully!");
    } catch (error) {
      toast.error(`Failed to update profile. ${error}`);
    }
  }

  async function loadAvatar() {
    avatarBlobUrl = await API.assets.getProfile(
      HIVE_API as string,
      $auth.token as string,
      Number($page.params.id),
      "/assets/profile.png"
    );
  }

  async function loadEvents() {
    loading = true;
    try {
      events = await API.events.getByOwner(
        HIVE_API as string,
        $auth.token as string,
        parseInt($page.params.id as string),
        showAll
      );
    } catch (error) {
      console.error("Error loading events:", error);
      events = [];
    } finally {
      loading = false;
    }
  }

  let offers = $derived(events.filter((e) => e.type === "OFFER").map(mapEvent));
  let needs = $derived(events.filter((e) => e.type === "NEED").map(mapEvent));

  interface EventData {
    id: number;
    title: string;
    location: { name: string };
    type: "OFFER" | "NEED";
    maxParticipants: number;
    tags: string[];
    ownerId: number;
  }

  interface MappedEvent {
    id: number;
    title: string;
    description: string;
    tags: string[];
    location: string;
    participantCount: number;
    type: "OFFER" | "NEED";
    eventId: number;
    ownerId: number;
  }

  function mapEvent(event: EventData): MappedEvent {
    return {
      id: event.id,
      title: event.title,
      description: "",
      tags: event.tags,
      location: event.location.name,
      participantCount: event.maxParticipants,
      type: event.type === "OFFER" ? "OFFER" : "NEED",
      eventId: event.id,
      ownerId: event.ownerId
    };
  }

  // Load avatar when component mounts
  $effect(() => {
    if ($auth.token && $page.params.id) {
      loadAvatar();
    }

    // Cleanup blob URLs on unmount (only if they are blob URLs)
    return () => {
      if (avatarBlobUrl && avatarBlobUrl.startsWith("blob:")) {
        URL.revokeObjectURL(avatarBlobUrl);
      }
    };
  });

  $effect(() => {
    if ($auth.token && $page.params.id) {
      loadEvents();
    }
  });
</script>

{#if data.suspended}
  <div class="hero min-h-screen bg-base-200">
    <div class="hero-content w-full max-w-4xl flex-col">
      <div class="alert alert-warning">
        <TriangleAlert />
        <span>This account has been suspended.</span>
      </div>
    </div>
  </div>
{:else}
  <div class="hero min-h-screen bg-base-200">
    <div class="hero-content w-full max-w-4xl flex-col">
      <div class="w-full">
        <!-- User Info Card -->
        <div class="card mb-6 bg-base-100 shadow-xl">
          <div class="card-body">
            <div class="flex justify-between">
              <div
                class="flex flex-col items-center gap-6 md:flex-row md:items-start"
              >
                <!-- Avatar -->
                <div class="flex flex-col items-center">
                  <div class="avatar">
                    <div
                      class="w-32 rounded-full ring ring-primary ring-offset-2 ring-offset-base-100"
                    >
                      <img
                        src={avatarBlobUrl}
                        alt={`${data?.fullName} avatar`}
                      />
                    </div>
                  </div>
                  {#if data?.self}
                    <div class="mt-4">
                      <button
                        class="btn btn-primary"
                        onclick={() => (editModalOpen = true)}
                      >
                        <Pen class="mr-2 h-4 w-4" />
                        Edit
                      </button>
                    </div>
                  {/if}
                </div>

                <!-- User Info -->
                <div class="flex-1 text-center md:text-left">
                  <div
                    class="flex items-center justify-center gap-2 md:justify-start"
                  >
                    <h1 class="text-3xl font-bold">
                      {data?.fullName}
                    </h1>
                    {#if data?.self}
                      <span class="badge badge-primary">You</span>
                    {/if}
                    {#if data?.role === "ADMIN" || data?.role === "OWNER"}
                      <span class="badge badge-warning">ADMIN</span>
                    {/if}
                    <div
                      class="tooltip avatar tooltip-bottom avatar-placeholder h-12 w-12"
                      data-tip={`User behavior score: ${data?.userScore}%`}
                    >
                      <div
                        class={`w-12 rounded-full ${bgColor(data?.userScore)}`}
                      >
                        {#if data?.userScore > 70}
                          <Smile class="h-6 w-6" />
                        {:else if data?.userScore > 49}
                          <Meh class="h-6 w-6" />
                        {:else if data?.userScore > 20}
                          <Frown class="h-6 w-6" />
                        {:else}
                          <Annoyed class="h-6 w-6" />
                        {/if}
                      </div>
                    </div>
                  </div>
                  <p class="text-sm italic opacity-70">
                    @{data?.username}
                  </p>
                  <p class="mt-2 text-lg whitespace-pre-wrap">
                    {biography}
                  </p>
                  <div class="mt-4 flex items-center gap-4">
                    <div class="flex flex-wrap gap-2">
                      {#each interests as interest (interest)}
                        <span class="badge badge-outline badge-lg badge-accent"
                          >{interest}</span
                        >
                      {/each}
                    </div>
                    <div class="flex items-center gap-2">
                      <Hourglass class="h-4 w-4" />
                      <span class="text-sm font-medium"
                        >Available: {data?.timeBank} Hours{#if data?.frozenAmount}
                          ({data?.frozenAmount} Hours Frozen){/if}</span
                      >
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Tabs for Offers/Needs -->
        <div class="card bg-base-100 shadow-xl">
          <div class="card-body">
            <div class="mb-4 flex justify-end">
              <label class="label cursor-pointer">
                <span class="label-text mr-2">Show all events</span>
                <input
                  type="checkbox"
                  class="toggle toggle-primary"
                  bind:checked={showAll}
                />
              </label>
            </div>
            <div class="tabs-bordered tabs">
              <button
                class="tab-active tab"
                class:tab-active={activeTab === "offers"}
                onclick={() => (activeTab = "offers")}
              >
                <HeartHandshake class="h-5 w-5" />
                Offers ({offers.length})
              </button>
              <button
                class="tab"
                class:tab-active={activeTab === "needs"}
                onclick={() => (activeTab = "needs")}
              >
                <HandHelping class="h-5 w-5" />
                Needs ({needs.length})
              </button>
            </div>

            <div class="mt-6">
              {#if activeTab === "offers"}
                {#if loading}
                  <div class="grid grid-cols-1 gap-4">
                    {#each [0, 1, 2, 3, 4, 5] as i (i)}
                      <div class="h-32 w-full skeleton"></div>
                    {/each}
                  </div>
                {:else}
                  <div class="grid grid-cols-1 gap-4">
                    {#if offers.length}
                      {#each offers as card (card.id)}
                        <SearchResultCard
                          title={card.title}
                          description={card.description}
                          tags={card.tags}
                          location={card.location}
                          participantCount={card.participantCount}
                          type={card.type}
                          eventId={card.eventId}
                          ownerId={card.ownerId}
                        />
                      {/each}
                    {:else}
                      <div class="col-span-full py-12 text-center">
                        <HeartHandshake
                          class="mx-auto h-16 w-16 text-base-content/30"
                        />
                        <p class="mt-4 text-lg text-base-content/60">
                          No offers yet
                        </p>
                      </div>
                    {/if}
                  </div>
                {/if}
              {:else if activeTab === "needs"}
                {#if loading}
                  <div class="grid grid-cols-1 gap-4">
                    {#each [0, 1, 2, 3, 4, 5] as i (i)}
                      <div class="h-32 w-full skeleton"></div>
                    {/each}
                  </div>
                {:else}
                  <div class="grid grid-cols-1 gap-4">
                    {#if needs.length}
                      {#each needs as card (card.id)}
                        <SearchResultCard
                          title={card.title}
                          description={card.description}
                          tags={card.tags}
                          location={card.location}
                          participantCount={card.participantCount}
                          type={card.type}
                          eventId={card.eventId}
                          ownerId={card.ownerId}
                        />
                      {/each}
                    {:else}
                      <div class="col-span-full py-12 text-center">
                        <HandHelping
                          class="mx-auto h-16 w-16 text-base-content/30"
                        />
                        <p class="mt-4 text-lg text-base-content/60">
                          No needs yet
                        </p>
                      </div>
                    {/if}
                  </div>
                {/if}
              {/if}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
{/if}

<ProfileModal
  bind:open={editModalOpen}
  currentBiography={biography}
  currentInterests={interests}
  currentAvatar={avatarBlobUrl}
  on:update={handleProfileUpdate}
/>
