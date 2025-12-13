<script lang="ts">
  import {
    MapPin,
    Calendar,
    Users,
    HeartHandshake,
    HandHelping,
    TriangleAlert,
    Info,
    Clock
  } from "@lucide/svelte";
  import { breadcrumbs } from "$lib/stores/breadcrumbs";

  import MapComponent from "$lib/components/MapComponent.svelte";
  import EventModal from "$lib/components/EventModal.svelte";
  import UserAvatar from "$lib/components/UserAvatar.svelte";
  import ParticipationProgress from "$lib/components/ParticipationProgress.svelte";
  import { HIVE_API } from "$lib/config";
  import { auth } from "$lib/stores/user";
  import { toast } from "$lib/stores/toast";
  import { page } from "$app/stores";
  import { API, type ParticipationState } from "$lib/api";

  type ParticipationRequest = {
    id: string;
    userId: number;
    userName: string;
    message: string;
    status: ParticipationState;
  };

  type EventData = {
    id: number;
    title: string;
    description: string;
    ownerId: number;
    tags: string[];
    location: string;
    coordinates?: { lat: number; lng: number };
    type: "OFFER" | "NEED";
    maxParticipants: number;
    duration: string;
    eventDate?: string;
    createdAt: string;
    updatedAt: string;
    cancelled: boolean;
  };

  let { data } = $props<{
    event: EventData;
  }>();
  let event = $derived(data.event);
  let userTimeBank = $state<number | null>(null);

  let isOwner = $derived($auth.user?.id === event.ownerId);
  let currentUserId = $derived($auth.user?.id);

  // User location functionality
  let userLatitude = $state<number | undefined>(undefined);
  let userLongitude = $state<number | undefined>(undefined);
  let userLocationName = $state<string | undefined>(undefined);
  let locationError = $state<string | undefined>(undefined);

  // Check if event is completed (date has passed)
  let isEventCompleted = () => {
    if (!event.eventDate) return false;
    const eventDate = new Date(event.eventDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Set to start of day
    return eventDate < today;
  };

  let isEventCancelled = () => event.cancelled;

  let isImmutable = () => {
    if (!event.eventDate) return false;
    const eventDate = new Date(event.eventDate);
    const today = new Date();
    return eventDate <= today || isEventCompleted() || isEventCancelled();
  };

  const asTitle = (str: string): string =>
    `${str.charAt(0).toUpperCase()}${str.toLowerCase().slice(1).toLowerCase()}`;

  // Check if user has enough time for the event
  let hasEnoughTime = $derived(() => {
    const durationHours = parseDuration(event.duration || "0 hour");
    return userTimeBank === null || userTimeBank >= durationHours;
  });

  breadcrumbs.set([
    { text: asTitle(event.type), href: `/?type=${event.type}` },
    { text: data.event.title }
  ]);

  // Edit functionality
  let showEditModal = $state(false);
  let showParticipationModal = $state(false);

  async function updateEvent(event: CustomEvent) {
    const eventData = event.detail;
    try {
      const eventId = $page.params.id;
      if (!eventId || !$auth.token) return;

      // Map the data to server format, full object with updated fields
      const location =
        eventData.location !== undefined
          ? {
              lat: eventData.location.lat,
              lon: eventData.location.lon,
              name: eventData.location.name
            }
          : data.event.coordinates
            ? {
                lat: data.event.coordinates.lat,
                lon: data.event.coordinates.lng,
                name: data.event.location
              }
            : { lat: 0, lon: 0, name: "" }; // Provide default if undefined

      const serverData = {
        title:
          eventData.title !== undefined ? eventData.title : data.event.title,
        description:
          eventData.description !== undefined
            ? eventData.description
            : data.event.description,
        tags: eventData.tags !== undefined ? eventData.tags : data.event.tags,
        location,
        maxParticipants:
          eventData.maxParticipants !== undefined
            ? eventData.maxParticipants
            : data.event.maxParticipants,
        type: eventData.type || data.event.type,
        duration:
          eventData.duration !== undefined
            ? eventData.duration
            : parseDuration(data.event.duration),
        eventDate:
          eventData.eventDate !== undefined
            ? eventData.eventDate
            : data.event.eventDate
      };

      // Make API call
      await API.events.update(
        HIVE_API as string,
        $auth.token,
        parseInt(eventId),
        serverData
      );

      // Upload image if provided
      if (eventData.image) {
        await API.assets.upload(
          HIVE_API as string,
          $auth.token,
          "event",
          parseInt(eventId),
          eventData.image
        );
      }

      showEditModal = false;
      toast.success("Updated successfully!");
      // Reload to see changes
      window.location.reload();
    } catch (error) {
      toast.error(`Failed to update event. Please try again. ${error}`);
    }
  }

  async function cancelEvent() {
    try {
      const eventId = $page.params.id;
      if (!eventId || !$auth.token) return;

      await API.events.cancel(
        HIVE_API as string,
        $auth.token,
        parseInt(eventId)
      );

      toast.success("Cancelled successfully!");
      // Reload to see changes
      window.location.reload();
    } catch (error) {
      toast.error(`Failed to cancel event. Please try again. ${error}`);
    }
  }

  function parseDuration(durationStr: string): number {
    if (!durationStr) return 0;
    const match = durationStr.match(/^(\d+)\s*(hour|day|week|month)/i);
    if (!match) return 0;
    const num = parseInt(match[1]);
    const unit = match[2].toLowerCase();
    switch (unit) {
      case "hour":
        return num;
      case "day":
        return num * 24;
      case "week":
        return num * 24 * 7;
      case "month":
        return num * 24 * 30; // approximate
      default:
        return num;
    }
  }

  let backgroundBlobUrl = $state<string | null>(null);

  // Participation functionality
  let userParticipationStatus = $state<ParticipationState | undefined>(
    undefined
  );
  let participationRequests = $state<ParticipationRequest[]>([]);

  // Get current user's participation status
  let userRequest = $derived(
    participationRequests.find(
      (r: ParticipationRequest) => r.userId === currentUserId
    )
  );
  let currentStatus = $derived(userRequest?.status);
  let currentStepIndex = $derived(
    currentStatus === "SENT"
      ? 0
      : currentStatus === "NEGOTIATING"
        ? 1
        : currentStatus === "ACCEPTED" || currentStatus === "REJECTED"
          ? 2
          : currentStatus === "COMPLETED"
            ? 3
            : -1
  );

  let participationMessage = $state("");

  async function sendParticipationRequest() {
    const message = participationMessage.trim();
    try {
      const eventId = $page.params.id;
      if (!eventId || !$auth.token) return;
      await API.events.participants.join(
        HIVE_API as string,
        $auth.token,
        parseInt(eventId),
        message
      );
      // Update local state
      const currentUserId = $auth.user?.id;
      if (!currentUserId) return;
      const request = {
        id: currentUserId.toString(),
        userId: currentUserId,
        userName: "", // Will be loaded by UserAvatar
        message,
        status: "SENT" as const
      };
      participationRequests = [...participationRequests, request];
      userParticipationStatus = "SENT";
      participationMessage = "";
      toast.success("Participation request sent successfully!");
    } catch (error) {
      toast.error(
        `Failed to send participation request. Please try again. ${error}`
      );
    }
  }

  async function withdraw() {
    try {
      const eventId = $page.params.id;
      if (!eventId || !$auth.token) return;
      await API.events.participants.withdraw(
        HIVE_API as string,
        $auth.token,
        parseInt(eventId)
      );
      // Update local state
      const currentUserId = $auth.user?.id;
      if (!currentUserId) return;
      participationRequests = participationRequests.filter(
        (req: ParticipationRequest) => req.userId !== currentUserId
      );
      userParticipationStatus = "WITHDRAWN";
      toast.success("Participation request withdrawn successfully!");
    } catch (error) {
      toast.error(
        `Failed to withdraw participation. Please try again. ${error}`
      );
    }
  }

  async function updateParticipationStatus(
    requestId: string,
    state: "ACCEPTED" | "REJECTED" | "NEGOTIATING"
  ) {
    const request = participationRequests.find(
      (r: ParticipationRequest) => r.id === requestId
    );
    if (!request) return;

    try {
      const eventId = $page.params.id;
      if (!eventId || !$auth.token) return;
      await API.events.participants.manageParticipant(
        HIVE_API as string,
        $auth.token,
        parseInt(eventId),
        request.userId,
        state
      );

      participationRequests = participationRequests.map(
        (req: ParticipationRequest) =>
          req.id === requestId ? { ...req, status: state } : req
      );

      // Update user status if it's their request
      const currentUserId = $auth.user?.id;
      if (request?.userId === currentUserId) {
        userParticipationStatus = state;
      }

      const actionMessages = {
        ACCEPTED: "Participant accepted successfully!",
        REJECTED: "Participant rejected successfully!",
        NEGOTIATING: "Negotiation started successfully!"
      };
      toast.success(actionMessages[state]);
    } catch (error) {
      toast.error(
        `Failed to update participation status. Please try again. ${error}`
      );
    }
  }

  // Wrapper functions for backward compatibility and clarity
  async function acceptRequest(requestId: string) {
    await updateParticipationStatus(requestId, "ACCEPTED");
  }

  async function negotiate(requestId: string) {
    //TODO visit /chat/${eventId}/{requestId}
  }

  async function rejectRequest(requestId: string) {
    await updateParticipationStatus(requestId, "REJECTED");
  }

  async function removeParticipant(requestId: string) {
    await updateParticipationStatus(requestId, "REJECTED");
  }

  async function startNegotiation(requestId: string) {
    await updateParticipationStatus(requestId, "NEGOTIATING");
  }

  // Map and distance functionality
  let userLocation = $state<{ lat: number; lng: number } | null>(null);
  let distance = $state<string | null>(null);

  // Calculate distance using Haversine formula
  function calculateDistance(
    lat1: number,
    lon1: number,
    lat2: number,
    lon2: number
  ): number {
    const R = 6371; // Earth's radius in kilometers
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos((lat1 * Math.PI) / 180) *
        Math.cos((lat2 * Math.PI) / 180) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  function getParticipantCount(): number {
    // Calculate active participant count from participation requests
    return participationRequests.filter(
      (r: ParticipationRequest) =>
        r.status === "ACCEPTED" || r.status === "NEGOTIATING"
    ).length;
  }

  // Get user's location
  function getUserLocation() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          userLocation = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
          };
          const dist = calculateDistance(
            userLocation.lat,
            userLocation.lng,
            data.event.coordinates?.lat || data.event.location.lat,
            data.event.coordinates?.lng || data.event.location.lon
          );
          distance =
            dist < 1 ? `${Math.round(dist * 1000)}m` : `${dist.toFixed(1)}km`;
        },
        (error) => {
          console.log("Error getting location:", error);
          distance = "Location access denied";
        }
      );
    } else {
      distance = "Geolocation not supported";
    }
  }

  // Get location on component mount
  $effect(() => {
    getUserLocation();
  });

  async function loadBackground() {
    const token = $auth.token;
    const id = $page.params.id;
    if (token && id) {
      backgroundBlobUrl = await API.assets.getEvent(
        HIVE_API as string,
        token,
        parseInt(id)
      );
    }
  }

  async function loadUserTimeBank() {
    try {
      const userData = await API.users.getById(
        HIVE_API as string,
        $auth.token!,
        currentUserId!
      );
      userTimeBank = userData.timeBank || 0;
    } catch (error) {
      console.error("Failed to load user timeBank:", error);
      userTimeBank = 0; // Default to 0 on error
    }
  }

  async function loadParticipationData() {
    try {
      const participationData: Record<number, ParticipationState> =
        await API.events.participants.get(
          HIVE_API as string,
          $auth.token!,
          parseInt($page.params.id!)
        );

      // Map participation data to ParticipationRequest array
      participationRequests = Object.entries(participationData).map(
        ([userIdStr, status]) => ({
          id: userIdStr,
          userId: parseInt(userIdStr),
          userName: "", // Will be loaded by UserAvatar
          message: "",
          status: status
        })
      );

      // Map user participation status
      const userStatus = currentUserId
        ? participationData[currentUserId]
        : undefined;
      userParticipationStatus = userStatus;
    } catch (error) {
      console.error("Failed to load participation data:", error);
    }
  }

  // Load background, user timeBank, and participation data when component mounts
  $effect(() => {
    if ($auth.token && $page.params.id && data.event) {
      loadBackground();
      loadParticipationData();
      if (!isOwner) {
        loadUserTimeBank();
      }
    }

    // Cleanup blob URL on unmount (only if it's a blob URL)
    return () => {
      if (backgroundBlobUrl && backgroundBlobUrl.startsWith("blob:")) {
        URL.revokeObjectURL(backgroundBlobUrl);
      }
    };
  });
</script>

<div class="hero min-h-screen bg-base-200">
  <div class="hero-content w-full max-w-4xl flex-col">
    <div class="w-full">
      {#if !data.event}
        <div class="loading loading-lg loading-spinner"></div>
      {:else}
        <!-- Event Description -->
        <div class="card mb-6 bg-base-100 shadow-xl">
          <div class="card-body">
            <div class="flex justify-between">
              <h1 class="mb-2 card-title text-3xl">{event.title}</h1>
              <div class="mb-2 flex items-center gap-3">
                <div
                  class="badge badge-lg {event.type === 'OFFER'
                    ? 'badge-success'
                    : 'badge-accent'}"
                >
                  {#if event.type === "OFFER"}
                    <HeartHandshake class="h-4 w-4" />
                  {:else}
                    <HandHelping class="h-4 w-4" />
                  {/if}
                  {event.type}
                </div>
              </div>
            </div>
            <div class="mb-4 flex gap-1">
              {#each event.tags as tag (tag)}
                <span class="badge badge-outline badge-accent">{tag}</span>
              {/each}
            </div>

            {#if backgroundBlobUrl}
              <img
                src={backgroundBlobUrl}
                alt="Event background"
                class="w-full h-64 object-cover rounded-lg mb-4"
              />
            {/if}

            <!-- Event status alerts -->
            {#if isEventCancelled()}
              <div class="mb-4 alert alert-warning">
                <TriangleAlert class="h-6 w-6" />
                <span>{`${asTitle(event.type)} is cancelled`}</span>
              </div>
            {:else if isEventCompleted()}
              <div class="mb-4 alert alert-info">
                <Info class="h-6 w-6" />
                <span>{`${asTitle(event.type)} is completed`}</span>
              </div>
            {:else if !isOwner && (!userParticipationStatus || userParticipationStatus === "WITHDRAWN" || userParticipationStatus === "REJECTED") && userTimeBank !== null && !hasEnoughTime}
              <div class="mb-4 alert alert-warning">
                <TriangleAlert class="h-6 w-6" />
                <span>Not enough time available</span>
              </div>
            {/if}

            <p class="mb-4 text-base-content/70" style="white-space: pre-wrap;">
              {event.description}
            </p>
            <div class="align-content justify-between lg:flex">
              <div class="mb-4 flex items-center gap-3">
                <UserAvatar userId={event.ownerId} />
                <div class="flex items-center gap-2 text-sm opacity-70">
                  <Calendar />
                  <span>{event.createdAt}</span>
                  {#if event.duration}
                    <Clock />
                    <span>{event.duration}</span>
                  {/if}
                  <Users />
                  <span
                    >{getParticipantCount()}/{event.maxParticipants} participants</span
                  >
                </div>
              </div>

              {#if isOwner}
                <div class="join join-vertical lg:join-horizontal">
                  <button
                    class="btn btn-secondary join-item"
                    class:btn-disabled={isImmutable()}
                    disabled={isImmutable()}
                    onclick={() => (showEditModal = true)}
                  >
                    Edit
                  </button>
                  <button
                    class="btn btn-error join-item"
                    class:btn-disabled={isImmutable()}
                    disabled={isImmutable()}
                    onclick={cancelEvent}
                  >
                    Cancel
                  </button>
                </div>
              {:else if !userParticipationStatus || userParticipationStatus === "WITHDRAWN" || userParticipationStatus === "REJECTED"}
                {#if isEventCompleted() || isEventCancelled()}
                  <div class="mt-2">
                    <button class="btn btn-disabled" disabled>
                      {#if event.type === "OFFER"}
                        <HeartHandshake class="h-5 w-5" />
                      {:else}
                        <HandHelping class="h-5 w-5" />
                      {/if}
                      Apply to {event.type.toLowerCase()}
                    </button>
                  </div>
                {:else if userTimeBank !== null && !hasEnoughTime}
                  <div class="mt-2">
                    <button class="btn btn-disabled" disabled>
                      {#if event.type === "OFFER"}
                        <HeartHandshake class="h-5 w-5" />
                      {:else}
                        <HandHelping class="h-5 w-5" />
                      {/if}
                      Apply to {event.type.toLowerCase()}
                    </button>
                  </div>
                {:else if getParticipantCount() >= event.maxParticipants}
                  <span class="mt-2 text-sm opacity-70">Full capacity</span>
                {:else}
                  <button
                    class="btn mt-2 btn-primary"
                    onclick={() => (showParticipationModal = true)}
                  >
                    {#if event.type === "OFFER"}
                      <HeartHandshake class="h-5 w-5" />
                    {:else}
                      <HandHelping class="h-5 w-5" />
                    {/if}
                    Apply to {event.type.toLowerCase()}
                  </button>
                {/if}
              {:else if userParticipationStatus && !["REJECTED", "WITHDRAWN"].includes(userParticipationStatus)}
                <button
                  class="btn mt-2 btn-secondary"
                  class:btn-disabled={isImmutable()}
                  disabled={isImmutable()}
                  onclick={withdraw}
                >
                  Withdraw
                </button>
              {:else if userParticipationStatus === ("REJECTED" as ParticipationState)}
                <!-- Rejected users cannot withdraw or reapply -->
              {/if}
            </div>
            <!-- Map Section -->
            <p class="flex items-center gap-2">
              <span class="font-medium">{event.location}</span>
              {#if distance}
                <span class="badge badge-ghost">~{distance} away</span>
              {/if}
            </p>
            {#if locationError}
              <div class="mb-4 alert alert-warning">
                <MapPin class="h-6 w-6" />
                <span>{locationError}</span>
              </div>
            {:else if !userLatitude || !userLongitude}
              <div class="mb-4 alert alert-info">
                <MapPin class="h-6 w-6" />
                <span>Getting your location...</span>
              </div>
            {/if}
            <MapComponent
              latitude={event.coordinates?.lat}
              longitude={event.coordinates?.lng}
              locationName={event.location}
              {userLatitude}
              {userLongitude}
              {userLocationName}
              height="h-64"
            />
          </div>

          {#if !isOwner && userRequest && currentStepIndex >= 0}
            <ParticipationProgress {currentStepIndex} {currentStatus} />
          {/if}

          {#if isOwner && !isEventCancelled()}
            <div class="grid grid-cols-1 gap-8 lg:grid-cols-2">
              <!-- Pending Requests -->
              <div class="card bg-base-100 shadow-xl">
                <div class="card-body">
                  <h3 class="card-title">
                    <Users class="h-5 w-5" />
                    Pending Requests ({participationRequests.filter(
                      (r: ParticipationRequest) =>
                        r.status === "SENT" ||
                        r.status === "PENDING" ||
                        r.status === "CREATED"
                    ).length})
                  </h3>
                  {#if participationRequests.some((r: ParticipationRequest) => r.status === "SENT" || r.status === "PENDING" || r.status === "CREATED")}
                    {#each participationRequests.filter((r: ParticipationRequest) => r.status === "SENT" || r.status === "PENDING" || r.status === "CREATED") as request (request.id)}
                      <div
                        class="mb-4 border-b border-base-300 pb-4 last:mb-0 last:border-b-0 last:pb-0"
                      >
                        <div class="flex items-start justify-between">
                          <div class="flex-1">
                            <UserAvatar userId={request.userId} />
                            {#if request.message}
                              <p class="mt-1 text-sm opacity-70">
                                {request.message}
                              </p>
                            {/if}
                          </div>
                          <div class="flex join">
                            <button
                              class="btn btn-sm btn-info join-item"
                              class:btn-disabled={isImmutable()}
                              disabled={isImmutable()}
                              onclick={() => startNegotiation(request.id)}
                            >
                              Accept
                            </button>
                            <button
                              class="btn btn-sm btn-error join-item"
                              class:btn-disabled={isImmutable()}
                              disabled={isImmutable()}
                              onclick={() => rejectRequest(request.id)}
                            >
                              Reject
                            </button>
                          </div>
                        </div>
                      </div>
                    {/each}
                  {:else}
                    <p class="text-sm opacity-70">
                      No pending participation requests.
                    </p>
                  {/if}
                </div>
              </div>

              <!-- Negotiating -->
              <div class="card bg-base-100 shadow-xl">
                <div class="card-body">
                  <h3 class="card-title">
                    <Users class="h-5 w-5" />
                    Negotiation ({participationRequests.filter(
                      (r: ParticipationRequest) => r.status === "NEGOTIATING"
                    ).length})
                  </h3>
                  {#if participationRequests.some((r: ParticipationRequest) => r.status === "NEGOTIATING")}
                    {#each participationRequests.filter((r: ParticipationRequest) => r.status === "NEGOTIATING") as request (request.id)}
                      <div
                        class="mb-4 border-b border-base-300 pb-4 last:mb-0 last:border-b-0 last:pb-0"
                      >
                        <div class="flex items-start justify-between">
                          <div class="flex-1">
                            <UserAvatar userId={request.userId} />
                            {#if request.message}
                              <p class="mt-1 text-sm opacity-70">
                                {request.message}
                              </p>
                            {/if}
                          </div>
                          <div class="flex join">
                            <button
                              class="btn btn-sm btn-success join-item"
                              class:btn-disabled={isImmutable()}
                              disabled={isImmutable()}
                              onclick={() => acceptRequest(request.id)}
                            >
                              Accept
                            </button>
                            <button
                              class="btn btn-sm btn-accent join-item"
                              class:btn-disabled={isImmutable()}
                              disabled={isImmutable()}
                              onclick={() => negotiate(request.id)}
                            >
                              Negotiate
                            </button>
                            <button
                              class="btn btn-sm btn-error join-item"
                              class:btn-disabled={isImmutable()}
                              disabled={isImmutable()}
                              onclick={() => removeParticipant(request.id)}
                            >
                              Remove
                            </button>
                          </div>
                        </div>
                      </div>
                    {/each}
                  {:else}
                    <p class="text-sm opacity-70">
                      There are no participants in negotiation.
                    </p>
                  {/if}
                </div>
              </div>
            </div>
          {/if}
        </div>
      {/if}
      <!-- Attendees -->
      <div class="card bg-base-100 shadow-xl">
        <div class="card-body">
          <h3 class="card-title">
            <Users class="h-5 w-5" />
            Attendees ({participationRequests.filter(
              (r: ParticipationRequest) =>
                r.status === "ACCEPTED" || r.status === "COMPLETED"
            ).length})
          </h3>
          {#if participationRequests.some((r: ParticipationRequest) => r.status === "ACCEPTED" || r.status === "COMPLETED")}
            <div
              class="mb-4 border-b border-base-300 pb-4 last:mb-0 last:border-b-0 last:pb-0 flex gap-3 items-center"
            >
              <UserAvatar userId={event.ownerId} />
              <span class="badge badge-accent">Owner</span>
            </div>
            {#each participationRequests.filter((r: ParticipationRequest) => r.status === "ACCEPTED" || r.status === "COMPLETED") as request (request.id)}
              <div
                class="mb-4 border-b border-base-300 pb-4 last:mb-0 last:border-b-0 last:pb-0"
              >
                <UserAvatar userId={request.userId} />
              </div>
            {/each}
          {:else}
            <p class="text-sm opacity-70">
              There are no accepted participants.
            </p>
          {/if}
        </div>
      </div>
    </div>
  </div>
</div>

<!-- Participation Modal -->
<div class="modal" class:modal-open={showParticipationModal}>
  <div class="modal-box">
    <h3 class="text-lg font-bold">Send Participation Request</h3>
    <p class="py-4">
      Provide additional information for your request (optional).
    </p>
    <textarea
      class="textarea-bordered textarea w-full"
      bind:value={participationMessage}
      placeholder="Additional information..."
    ></textarea>
    <div class="modal-action">
      <button
        class="btn"
        onclick={() => {
          showParticipationModal = false;
          participationMessage = "";
        }}>Cancel</button
      >
      <button
        class="btn btn-primary"
        onclick={() => {
          sendParticipationRequest();
          showParticipationModal = false;
        }}>Send Request</button
      >
    </div>
  </div>
  <div
    class="modal-backdrop"
    role="button"
    tabindex="0"
    onclick={() => {
      showParticipationModal = false;
      participationMessage = "";
    }}
    onkeydown={(e) => {
      if (e.key === "Enter" || e.key === " ") {
        showParticipationModal = false;
        participationMessage = "";
      }
    }}
  ></div>
</div>

<EventModal
  bind:open={showEditModal}
  mode="edit"
  currentTitle={data.event.title}
  currentDescription={data.event.description}
  currentTags={data.event.tags}
  currentLocation={data.event.coordinates
    ? {
        lat: data.event.coordinates.lat,
        lon: data.event.coordinates.lng,
        name: data.event.location
      }
    : undefined}
  currentMaxParticipants={data.event.maxParticipants}
  currentType={data.event.type}
  currentDuration={data.event.duration
    ? parseInt(data.event.duration.split(" ")[0])
    : 0}
  currentEventDate={data.event.eventDate}
  currentImage={backgroundBlobUrl}
  on:update={updateEvent}
/>
