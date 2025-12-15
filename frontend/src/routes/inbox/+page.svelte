<script lang="ts">
  import {
    Bell,
    Ban,
    MessagesSquare,
    Handshake,
    CalendarX,
    CalendarCheck,
    CalendarCog,
    ReplyAll,
    Archive,
    ShieldUser,
    DoorOpen
  } from "@lucide/svelte";
  import EventReviewModal from "$lib/components/EventReviewModal.svelte";
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
        notification.read = true;
      }
    } catch (error) {
      console.error("Failed to mark notification as read:", error);
    } finally {
      loading = false;
    }
  }

  async function handleNotificationClick(notification: Notification) {
    // Handle EVENT_COMPLETED notifications differently
    if (notification.type === "EVENT_COMPLETED" && !notification.read) {
      handleEventCompleted(notification);
      return;
    }

    // For unread notifications, mark as read first
    if (!notification.read) {
      await markAsRead(notification.id);
    }

    // Redirect if notification has a redirect URL
    if (notification.redirect) {
      window.location.href = getRedirectUrl(notification.redirect);
    }
  }

  function handleEventCompleted(notification: Notification) {
    // Don't allow review if notification is already read (review already submitted)
    if (notification.read) {
      return;
    }

    // Use the redirect identifier to get event ID
    if (notification.redirect.type === "EVENT") {
      selectedEventId = notification.redirect.id;
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
        notification.read = true;
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

      notifications.forEach((n: Notification) => (n.read = true));
    } catch (error) {
      console.error("Failed to mark all notifications as read:", error);
    } finally {
      loading = false;
    }
  }

  function getNotificationTypeInfo(type: Notification["type"]) {
    switch (type) {
      case "EVENT_EDIT":
        return {
          icon: CalendarCog,
          badgeColor: "badge-primary",
          label: "Offer/Need Updated",
          message: "An offer/need you're participating in has been updated."
        };
      case "EVENT_SENT":
        return {
          icon: MessagesSquare,
          badgeColor: "badge-secondary",
          label: "Participation Request",
          message: "You have a new participation request to respond to."
        };
      case "EVENT_WITHDRAWN":
        return {
          icon: DoorOpen,
          badgeColor: "badge-error",
          label: "Withdrawn",
          message: "A participant pulled back their participation."
        };
      case "EVENT_NEGOTIATING":
        return {
          icon: MessagesSquare,
          badgeColor: "badge-secondary",
          label: "Negotiating",
          message: "Your participation request has been approved."
        };
      case "EVENT_ACCEPTED":
        return {
          icon: Handshake,
          badgeColor: "badge-success",
          label: "Accepted",
          message: "Your participation has been accepted!"
        };
      case "EVENT_REJECTED":
        return {
          icon: Ban,
          badgeColor: "badge-error",
          label: "Rejected",
          message: "Your participation was rejected."
        };
      case "EVENT_CANCELLED":
        return {
          icon: CalendarX,
          badgeColor: "badge-warning",
          label: "Cancelled",
          message: "An offer/need you're participating in has been cancelled."
        };
      case "EVENT_COMPLETED":
        return {
          icon: CalendarCheck,
          badgeColor: "badge-success",
          label: "Completed",
          message:
            "An offer/need you participated in has been completed. Please leave a review."
        };
      case "THREAD_REPLY":
        return {
          icon: ReplyAll,
          badgeColor: "badge-accent",
          label: "Forum Reply",
          message: "Someone replied to a forum thread you participated."
        };
      case "THREAD_CLOSED":
        return {
          icon: Archive,
          badgeColor: "badge-warning",
          label: "Thread Closed",
          message: "A forum thread you participated in has been closed."
        };
      case "USER_ROLE_UPDATE":
        return {
          icon: ShieldUser,
          badgeColor: "badge-info",
          label: "Role Updated",
          message: "Your user role has been updated by an administrator."
        };

      default:
        return {
          icon: Bell,
          badgeColor: "badge-ghost",
          label: "Notification",
          message: "You have a new notification."
        };
    }
  }

  function getRedirectUrl(redirect: {
    id: number;
    type: "USER" | "EVENT" | "THREAD";
  }) {
    switch (redirect.type) {
      case "USER":
        return `/user/${redirect.id}`;
      case "EVENT":
        return `/event/${redirect.id}`;
      case "THREAD":
        return `/forum/${redirect.id}`;
      default:
        return "#";
    }
  }

  let unreadCount = $derived(
    notifications.filter((n: Notification) => !n.read).length
  );
</script>

<div class="hero min-h-screen bg-base-200">
  <div class="hero-content w-full max-w-4xl flex-col">
    <div class="w-full">
      {#if unreadCount > 0}
        <button
          class="btn btn-ghost btn-sm"
          onclick={markAllAsRead}
          disabled={loading}
        >
          Mark all as read
        </button>
        <div class="badge badge-lg badge-primary">{unreadCount} unread</div>
      {:else if notifications.length === 0}
        <div class="py-12 text-center">
          <h3 class="mb-2 text-xl font-semibold">No notifications yet</h3>
          <p class="text-base-content/70">
            When you receive notifications about events, forum replies, or
            system updates, they'll appear here.
          </p>
        </div>
      {:else}
        <div class="badge badge-ghost badge-lg">All caught up!</div>
      {/if}
      {#each notifications as notification (notification.id)}
        {@const typeInfo = getNotificationTypeInfo(notification.type)}
        <div
          class="card bg-base-100 shadow-md hover:shadow-lg {notification.read
            ? 'opacity-75'
            : ''} cursor-pointer transition-all duration-200 hover:scale-[1.02]"
          role="button"
          tabindex="0"
          onclick={() => handleNotificationClick(notification)}
          onkeydown={(e) => {
            if (e.key === "Enter" || e.key === " ") {
              e.preventDefault();
              handleNotificationClick(notification);
            }
          }}
          aria-label={`View ${typeInfo.label} notification`}
        >
          <div class="card-body p-4">
            <div class="flex items-start gap-4">
              <div class="flex-shrink-0">
                <div class="avatar">
                  <div
                    class="flex w-12 items-center justify-center rounded-full bg-base-200"
                  >
                    <typeInfo.icon class="h-6 w-6" />
                  </div>
                </div>
              </div>

              <div class="min-w-0 flex-1">
                <div class="mb-2 flex items-center gap-2">
                  <span class="badge {typeInfo.badgeColor} badge-sm">
                    {typeInfo.label}
                  </span>
                  {#if !notification.read}
                    <span class="badge badge-xs badge-error">New</span>
                  {/if}
                </div>

                <p class="mb-2 text-sm text-base-content/70">
                  {typeInfo.message}
                </p>

                <div class="flex items-center justify-between">
                  <span class="text-xs text-base-content/50">
                    {new Date(notification.createdAt).toLocaleString()}
                  </span>

                  <div class="flex gap-2">
                    {#if notification.type === "EVENT_COMPLETED" && !notification.read}
                      <button
                        class="btn btn-sm btn-primary"
                        onclick={(e) => {
                          e.stopPropagation();
                          handleEventCompleted(notification);
                        }}
                        disabled={loading}
                      >
                        <CalendarCheck class="h-4 w-4" />
                        Review
                      </button>
                    {:else if !notification.read}
                      <button
                        class="btn btn-sm btn-outline"
                        onclick={(e) => {
                          e.stopPropagation();
                          markAsRead(notification.id);
                        }}
                        disabled={loading}
                      >
                        Mark as read
                      </button>
                    {/if}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      {/each}
    </div>
  </div>
</div>

<EventReviewModal bind:open={showReviewModal} onsubmit={submitEventReview} />

<style>
  .card {
    transition: all 0.2s ease-in-out;
  }

  .card:hover {
    transform: translateY(-2px);
  }
</style>
