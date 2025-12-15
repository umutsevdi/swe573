<script lang="ts">
  import { Star } from "@lucide/svelte";
  import BaseModal from "./BaseModal.svelte";
  import { toast } from "$lib/stores/toast";

  let { open = $bindable(false), onsubmit } = $props<{
    open: boolean;
    onsubmit?: (data: {
      rating: number;
      deliveryStatus: "UNDERDELIVERED" | "DELIVERED" | "OVERDELIVERED";
      message: string;
    }) => void;
  }>();

  let rating = $state(0);
  let deliveryStatus = $state<"UNDERDELIVERED" | "DELIVERED" | "OVERDELIVERED">(
    "DELIVERED"
  );
  let message = $state("");
  let hoveredRating = $state(0);

  function setStarRating(value: number) {
    rating = value;
  }

  function submitReview() {
    if (rating === 0) {
      toast.error("Please provide a rating");
      return;
    }

    if (!message.trim()) {
      toast.error("Please provide a review message");
      return;
    }

    if (onsubmit) {
      onsubmit({
        rating,
        deliveryStatus,
        message: message.trim()
      });
    }

    // Reset form
    rating = 0;
    deliveryStatus = "DELIVERED";
    message = "";
  }

  function closeModal() {
    open = false;
  }

  // Reset form when modal closes
  $effect(() => {
    if (!open) {
      rating = 0;
      deliveryStatus = "DELIVERED";
      message = "";
      hoveredRating = 0;
    }
  });
</script>

<BaseModal bind:open size="max-w-md">
  <h3 class="mb-6 text-lg font-bold">Review Event</h3>

  <form onsubmit={submitReview}>
    <!-- Rating Section -->
    <div class="mb-6">
      <label class="mb-2 block text-sm font-medium" for="rating-group">
        How well was the event?
      </label>
      <div
        id="rating-group"
        class="flex gap-1"
        role="radiogroup"
        aria-label="Event rating"
      >
        <div class="flex gap-1">
          {#each [1, 2, 3, 4, 5] as star (star)}
            <button
              type="button"
              class="btn btn-ghost btn-square p-2"
              onclick={() => setStarRating(star)}
              onmouseenter={() => (hoveredRating = star)}
              onmouseleave={() => (hoveredRating = 0)}
              aria-label={`Rate ${star} stars`}
              role="radio"
              aria-checked={rating === star}
              aria-setsize={5}
              aria-posinset={star}
            >
              <Star
                class="h-8 w-8 transition-colors"
                fill={star <= (hoveredRating || rating)
                  ? "currentColor"
                  : "none"}
                color={star <= (hoveredRating || rating)
                  ? "rgb(251 191 36)"
                  : "rgb(156 163 175)"}
              />
            </button>
          {/each}
        </div>
        {#if rating > 0}
          <p class="mt-2 text-sm text-base-content/70">
            {rating}
            {rating === 1 ? "star" : "stars"}
          </p>
        {/if}
      </div>

      <!-- Delivery Status Section -->
      <div class="mb-6">
        <label class="mb-2 block text-sm font-medium" for="delivery-status">
          Delivery Status
        </label>
        <select
          id="delivery-status"
          class="select-bordered select w-full"
          bind:value={deliveryStatus}
        >
          <option value="UNDERDELIVERED">Underdelivered</option>
          <option value="DELIVERED">Delivered</option>
          <option value="OVERDELIVERED">Overdelivered</option>
        </select>
      </div>

      <!-- Message Section -->
      <div class="mb-6">
        <label class="mb-2 block text-sm font-medium" for="review-message">
          Your Review
        </label>
        <textarea
          id="review-message"
          class="textarea-bordered textarea h-24 w-full"
          placeholder="Share your experience with this event..."
          bind:value={message}
          maxlength="500"
          required
        ></textarea>
        <div class="mt-1 text-xs text-base-content/60">
          {message.length} / 500
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="modal-action">
        <button type="button" class="btn" onclick={closeModal}>Cancel</button>
        <button type="submit" class="btn btn-primary"> Send Review </button>
      </div>
    </div>
  </form>
</BaseModal>
