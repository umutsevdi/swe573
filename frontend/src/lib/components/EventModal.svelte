<script lang="ts">
  import { createEventDispatcher } from "svelte";
  import { Tag, MapPin, Upload, Image } from "@lucide/svelte";
  import TagInput from "./TagInput.svelte";
  import BaseModal from "./BaseModal.svelte";
  import LocationPicker from "./LocationPicker.svelte";
  import { toast } from "$lib/stores/toast";
  import { createFilePreview } from "$lib/utils/file";

  const dispatch = createEventDispatcher<{
    submit: {
      title: string;
      description: string;
      tags: string[];
      location: { lat: number; lon: number; name: string };
      maxParticipants: number;
      type: "OFFER" | "NEED";
      duration: number;
      eventDate: string;
      image?: File;
    };
    update: {
      title: string;
      description: string;
      tags: string[];
      location: { lat: number; lon: number; name: string };
      maxParticipants: number;
      type: "OFFER" | "NEED";
      duration: number;
      eventDate: string;
      image?: File;
    };
  }>();

  let {
    mode,
    open = $bindable(false),
    currentTitle = "",
    currentDescription = "",
    currentTags = [],
    currentLocation,
    currentMaxParticipants = 0,
    currentType = "OFFER",
    currentDuration = 0,
    currentEventDate = "",
    currentImage
  } = $props<{
    mode: "create" | "edit";
    open: boolean;
    currentTitle?: string;
    currentDescription?: string;
    currentTags?: string[];
    currentLocation?: { lat: number; lon: number; name: string };
    currentMaxParticipants?: number;
    currentType?: "OFFER" | "NEED";
    currentDuration?: number;
    currentEventDate?: string;
    currentImage?: string | null;
  }>();

  let title = $state("");
  let description = $state("");
  let tags = $state<string[]>([]);
  let location = $state({ lat: 41.0082, lon: 28.9784, name: "Istanbul" });
  let maxParticipants = $state(0);
  let type = $state<"OFFER" | "NEED">("OFFER");
  let duration = $state(1);
  let eventDate = $state("");
  let imageFile = $state<File | null>(null);
  let imagePreview = $state<string | null>(null);

  function openModal() {
    open = true;
  }

  function closeModal() {
    open = false;
  }

  function resetForm() {
    if (mode === "edit") {
      title = currentTitle || "";
      description = currentDescription || "";
      tags = currentTags ? [...currentTags] : [];
      location = currentLocation
        ? { ...currentLocation }
        : { lat: 41.0082, lon: 28.9784, name: "Istanbul" };
      maxParticipants = currentMaxParticipants || 0;
      type = currentType || "OFFER";
      duration = currentDuration || 1;
      eventDate = currentEventDate || "";
      imagePreview = currentImage || null;
    } else {
      title = "";
      description = "";
      tags = [];
      location = { lat: 41.0082, lon: 28.9784, name: "Istanbul" };
      maxParticipants = 0;
      type = "OFFER";
      duration = 1;
      eventDate = new Date().toISOString().split("T")[0];
      imagePreview = null;
    }
    imageFile = null;
  }

  async function handleImageChange(event: Event) {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (file) {
      imageFile = file;
      imagePreview = await createFilePreview(file);
    }
  }

  function submitEvent() {
    if (!title.trim() || !description.trim()) {
      toast.error("Title and description are required");
      return;
    }

    if (maxParticipants < 1) {
      toast.error("Maximum participants must be at least 1");
      return;
    }

    if (duration < 1) {
      toast.error("Duration must be at least 1 hour");
      return;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const selectedDate = new Date(eventDate);
    if (selectedDate < today) {
      toast.error("Event date cannot be earlier than today");
      return;
    }

    if (!location || !location.name) {
      location = { lat: 41.0082, lon: 28.9784, name: "Istanbul" };
    }

    const data = {
      title: title.trim(),
      description: description,
      tags,
      location,
      maxParticipants,
      type,
      duration: duration,
      eventDate,
      image: imageFile || undefined
    };
    dispatch(mode === "edit" ? "update" : "submit", data);
    closeModal();
  }

  // Initialize form on open
  $effect(() => {
    if (open) {
      resetForm();
    }
  });

  // Reset form on close
  $effect(() => {
    if (!open) {
      resetForm();
    }
  });

  // Export function for parent component access
  export { openModal };
</script>

<BaseModal bind:open size="max-w-2xl">
  <h3 class="mb-4 text-lg font-bold">
    {mode === "edit" ? "Edit" : "Create"}
  </h3>

  <form onsubmit={submitEvent}>
    <!-- Top Section: Title, Description, Tags on left; Map on right -->
    <div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <!-- Left Column -->
      <div class="space-y-4">
        <!-- Title -->
        <div class="form-control">
          <label class="label" for="title-input">
            <span class="label-text">Title</span>
          </label>
          <input
            id="title-input"
            type="text"
            placeholder="Enter event title"
            class="input-bordered input w-full"
            bind:value={title}
            maxlength="256"
            required
          />
          <div class="mt-1 text-xs text-base-content/60">
            {title.length} / 256
          </div>
        </div>

        <!-- Type -->
        <div class="form-control">
          <div class="join w-full">
            <input
              class="btn join-item {type === 'OFFER'
                ? 'btn-success'
                : 'btn-outline'}"
              type="radio"
              bind:group={type}
              value="OFFER"
              aria-label="OFFER"
            />
            <input
              class="btn join-item {type === 'NEED'
                ? 'btn-accent'
                : 'btn-outline'}"
              type="radio"
              bind:group={type}
              value="NEED"
              aria-label="NEED"
            />
          </div>
        </div>

        <!-- Description -->
        <div class="form-control">
          <label class="label" for="description-input">
            <span class="label-text">Description</span>
          </label>
          <textarea
            id="description-input"
            placeholder="Describe your event"
            class="textarea-bordered textarea h-24 w-full"
            bind:value={description}
            maxlength="1024"
            required
          ></textarea>
          <div class="mt-1 text-xs text-base-content/60">
            {description.length} / 1024
          </div>
        </div>

        <!-- Tags -->
        <div class="form-control">
          <label class="label">
            <span class="label-text flex items-center gap-2">
              <Tag class="h-4 w-4" />
              Tags
            </span>
          </label>
          <TagInput bind:tags />
        </div>

        <!-- Event Image -->
        <div class="form-control">
          <label class="label" for="event-image-input">
            <span class="label-text flex items-center gap-2">
              <Image class="h-4 w-4" />
              Event Image (Optional)
            </span>
          </label>
          <div class="flex items-center gap-4">
            <div class="avatar">
              <div class="h-16 w-16 rounded">
                {#if imagePreview}
                  <img
                    src={imagePreview}
                    alt="Event image preview"
                    class="object-cover"
                  />
                {:else}
                  <div
                    class="flex h-full items-center justify-center bg-neutral text-neutral-content"
                  >
                    <Image class="h-8 w-8" />
                  </div>
                {/if}
              </div>
            </div>
            <label class="btn btn-outline btn-sm">
              <Upload class="mr-2 h-4 w-4" />
              Upload Image
              <input
                id="event-image-input"
                type="file"
                accept="image/*"
                class="hidden"
                onchange={handleImageChange}
              />
            </label>
          </div>
        </div>
      </div>

      <!-- Right Column -->
      <div>
        <!-- Location -->
        <div class="form-control">
          <label class="label">
            <span class="label-text flex items-center gap-2">
              <MapPin class="h-4 w-4" />
              Location
            </span>
          </label>
          <LocationPicker
            initialLat={mode === "edit" && currentLocation
              ? currentLocation.lat
              : undefined}
            initialLon={mode === "edit" && currentLocation
              ? currentLocation.lon
              : undefined}
            initialName={mode === "edit" && currentLocation
              ? currentLocation.name
              : undefined}
            height="h-96"
            onLocationChange={(loc) => (location = loc)}
          />
        </div>
      </div>
    </div>

    <!-- Bottom Row: Max Participants, Duration, Event Date -->
    <div class="mt-6 grid grid-cols-1 gap-4 lg:grid-cols-3">
      <!-- Max Participants -->
      <div class="form-control">
        <label class="label" for="max-participants">
          <span class="label-text">Max Participants</span>
        </label>
        <input
          id="max-participants"
          type="number"
          class="input-bordered input w-full"
          bind:value={maxParticipants}
          min="0"
        />
      </div>

      <!-- Duration -->
      <div class="form-control">
        <label class="label" for="duration">
          <span class="label-text">Duration (hours)</span>
        </label>
        <input
          id="duration"
          type="number"
          class="input-bordered input w-full"
          bind:value={duration}
          min="1"
          max="12"
        />
      </div>

      <!-- Event Date -->
      <div class="form-control">
        <label class="label" for="event-date">
          <span class="label-text">Event Date</span>
        </label>
        <input
          id="event-date"
          type="date"
          class="input-bordered input w-full"
          bind:value={eventDate}
        />
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="modal-action mt-6">
      <button type="button" class="btn" onclick={closeModal}>Cancel</button>
      <button type="submit" class="btn btn-primary">
        {mode === "edit" ? "Update" : "Create"}
      </button>
    </div>
  </form>
</BaseModal>
