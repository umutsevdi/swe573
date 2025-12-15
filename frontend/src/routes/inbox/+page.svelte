<script lang="ts">
  import {
    Bell,
    Ban,
    Check,
    ExternalLink,
    Inbox,
    MessagesSquare,
    Handshake,
    CalendarX,
    CalendarCheck,
    CalendarCog,
    ReplyAll,
    Archive,
    ShieldUser
  } from "@lucide/svelte";
  import EventReviewModal from "$lib/components/EventReviewModal.svelte";
  import { resolve } from "$app/paths";
  import { breadcrumbs } from "$lib/stores/breadcrumbs";
  import { API, type Notification } from "$lib/api";
  import { auth } from "$lib/stores/user";
  import { HIVE_API } from "$lib/config";

  breadcrumbs.set([{ text: "Inbox" }]);

  let { data } = $props<{ notifications: Notification[] }>();
  let notifications = $state(data.notifications);
  let loading = $state(false);

  // Event review modal state
  let showReviewModal = $state(false);
  let selectedEventId = $state(0);
  let selectedNotificationId = $state(0);

  async function markAsRead(notificationId: number) {
    try {
      loading = true;
      await API.inbox.markAsRead(HIVE_API, $auth.token || "", notificationId);

      const notification = notifications.find(
        (n: Notification) => n.id === notificationId
      );
      if (notification) {
        notification.isRead = true;
      }
    } catch (error) {
      console.error("Failed to mark notification as read:", error);
    } finally {
      loading = false;
    }
  }

  function handleEventCompleted(notification: Notification) {
    // Don't allow review if notification is already read (review already submitted)
    if (notification.isRead) {
      return;
    }

    // Extract event ID from redirect URL
    const match = notification.redirectTo.match(/\/event\/(\d+)/);
    if (match) {
      selectedEventId = parseInt(match[1]);
      selectedNotificationId = notification.id;
      showReviewModal = true;
    }
  }

  async function submitEventReview(data: {
    rating: number;
    deliveryStatus: "UNDERDELIVERED" | "DELIVERED" | "OVERDELIVERED";
    message: string;
  }) {
    try {
      loading = true;

      // Submit review
      await API.events.submitReview(
        HIVE_API,
        $auth.token || "",
        selectedEventId,
        data
      );

      // Mark notification as read
      await API.inbox.markAsRead(
        HIVE_API,
        $auth.token || "",
        selectedNotificationId
      );

      // Update local state
      const notification = notifications.find(
        (n: Notification) => n.id === selectedNotificationId
      );
      if (notification) {
        notification.isRead = true;
      }

      showReviewModal = false;
    } catch (error) {
      console.error("Failed to submit review:", error);
    } finally {
      loading = false;
    }
  }

  async function markAllAsRead() {
    try {
      loading = true;
      await API.inbox.readAll(HIVE_API, $auth.token || "");

      notifications.forEach((n: Notification) => (n.isRead = true));
    } catch (error) {
      console.error("Failed to mark all notifications as read:", error);
    } finally {
      loading = false;
    }
  }

  function getTypeIcon(type: Notification["type"]) {
    switch (type) {
      case "EVENT_EDIT":
        return CalendarCog;
      case "EVENT_NEGOTIATING":
        return MessagesSquare;
      case "EVENT_ACCEPTED":
        return Handshake;
      case "EVENT_REJECTED":
        return Ban;
      case "EVENT_CANCELLED":
        return CalendarX;
      case "EVENT_COMPLETED":
        return CalendarCheck;
      case "THREAD_REPLY":
        return ReplyAll;
      case "THREAD_CLOSED":
        return Archive;
      case "USER_ROLE_UPDATE":
        return ShieldUser;
      default:
        return Bell;
    }
  }

  function getTypeBadgeColor(type: Notification["type"]) {
    switch (type) {
      case "EVENT_EDIT":
        return "badge-primary";
      case "EVENT_NEGOTIATING":
        return "badge-secondary";
      case "EVENT_ACCEPTED":
        return "badge-success";
      case "EVENT_REJECTED":
        return "badge-error";
      case "EVENT_CANCELLED":
        return "badge-warning";
      case "EVENT_COMPLETED":
        return "badge-success";
      case "THREAD_REPLY":
        return "badge-accent";
      case "THREAD_CLOSED":
        return "badge-warning";
      case "USER_ROLE_UPDATE":
        return "badge-info";
      default:
        return "badge-ghost";
    }
  }

  function getTypeLabel(type: Notification["type"]) {
    switch (type) {
      case "EVENT_EDIT":
        return "Event Updated";
      case "EVENT_NEGOTIATING":
        return "Event Invitation";
      case "EVENT_ACCEPTED":
        return "Event Accepted";
      case "EVENT_REJECTED":
        return "Event Rejected";
      case "EVENT_CANCELLED":
        return "Event Cancelled";
      case "EVENT_COMPLETED":
        return "Event Completed";
      case "THREAD_REPLY":
        return "Forum Reply";
      case "THREAD_CLOSED":
        return "Thread Closed";
      case "USER_ROLE_UPDATE":
        return "Role Updated";
      default:
        return "Notification";
    }
  }

  function formatTimestamp(timestamp: string) {
    return new Date(timestamp).toLocaleString();
  }

  let unreadCount = $derived(
    notifications.filter((n: Notification) => !n.isRead).length
  );
</script>

<div class="w-full">
  <div class="mb-4 flex justify-between items-center">
    <h2 class="text-2xl font-bold">Notifications</h2>
    <div class="flex gap-2">
      {#if unreadCount > 0}
        <button
          class="btn btn-ghost btn-sm"
          onclick={markAllAsRead}
          disabled={loading}
        >
          Mark all as read
        </button>
        <div class="badge badge-lg badge-primary">{unreadCount} unread</div>
      {:else}
        <div class="badge badge-ghost badge-lg">All caught up!</div>
      {/if}
    </div>
  </div>

  <div class="h-[75vh] space-y-4 overflow-y-auto">
    {#each notifications as notification (notification.id)}
      {@const IconComponent = getTypeIcon(notification.type)}
      <div
        class="card bg-base-100 shadow-md hover:shadow-lg {notification.isRead
          ? 'opacity-75'
          : ''}"
      >
        <div class="card-body p-4">
          <div class="flex items-start gap-4">
            <div class="flex-shrink-0">
              <div class="avatar">
                <div
                  class="flex w-12 items-center justify-center rounded-full bg-base-200"
                >
                  <IconComponent class="h-6 w-6" />
                </div>
              </div>
            </div>

            <div class="min-w-0 flex-1">
              <div class="mb-2 flex items-center gap-2">
                <span
                  class="badge {getTypeBadgeColor(notification.type)} badge-sm"
                >
                  {getTypeLabel(notification.type)}
                </span>
                {#if !notification.isRead}
                  <span class="badge badge-xs badge-error">New</span>
                {/if}
              </div>

              <p class="mb-2 text-sm text-base-content/70">
                {notification.message}
              </p>

              <div class="flex items-center justify-between">
                <span class="text-xs text-base-content/50">
                  {formatTimestamp(notification.createdAt)}
                </span>

                <div class="flex gap-2">
                  {#if !notification.isRead}
                    <button
                      class="btn btn-ghost btn-sm"
                      onclick={() => markAsRead(notification.id)}
                      disabled={loading}
                    >
                      <Check class="h-4 w-4" />
                      Mark as read
                    </button>
                  {/if}

                  {#if notification.type === "EVENT_COMPLETED" && !notification.isRead}
                    <button
                      class="btn btn-sm btn-primary"
                      onclick={() => handleEventCompleted(notification)}
                      disabled={loading}
                    >
                      <CalendarCheck class="h-4 w-4" />
                      Review
                    </button>
                  {:else if notification.redirectTo}
                    <a
                      href={resolve(notification.redirectTo)}
                      class="btn btn-sm btn-primary"
                    >
                      <ExternalLink class="h-4 w-4" />
                      View
                    </a>
                  {/if}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    {/each}

    {#if notifications.length === 0}
      <div class="py-12 text-center">
        <Inbox />
        <h3 class="mb-2 text-xl font-semibold">No notifications yet</h3>
        <p class="text-base-content/70">
          When you receive notifications about events, forum replies, or system
          updates, they'll appear here.
        </p>
      </div>
    {/if}
  </div>
</div>

<EventReviewModal
  bind:open={showReviewModal}
  eventId={selectedEventId}
  notificationId={selectedNotificationId}
  onsubmit={submitEventReview}
/>

<style>
  .card {
    transition: all 0.2s ease-in-out;
  }

  .card:hover {
    transform: translateY(-2px);
  }
</style>
