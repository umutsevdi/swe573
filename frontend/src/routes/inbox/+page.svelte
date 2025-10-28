<script lang="ts">
	import { MessageSquare, HeartHandshake, HandHelping, Check, ExternalLink } from '@lucide/svelte';
	import { resolve } from '$app/paths';

	type NotificationType = 'forum_reply' | 'offer_response' | 'need_response';

	type NotificationData = {
		id: string;
		type: NotificationType;
		title: string;
		message: string;
		sourceId: string;
		sourceUrl: string;
		timestamp: string;
		isRead: boolean;
	};

	let { data } = $props<{ notifications: NotificationData[] }>();
	let notifications = $state(data.notifications);

	function markAsRead(notificationId: string) {
		const notification = notifications.find((n: NotificationData) => n.id === notificationId);
		if (notification) {
			notification.isRead = true;
		}
	}

	function getTypeBadgeColor(type: NotificationType) {
		switch (type) {
			case 'forum_reply':
				return 'badge-primary';
			case 'offer_response':
				return 'badge-secondary';
			case 'need_response':
				return 'badge-accent';
		}
	}

	function getTypeLabel(type: NotificationType) {
		switch (type) {
			case 'forum_reply':
				return 'Forum Reply';
			case 'offer_response':
				return 'Offer Response';
			case 'need_response':
				return 'Need Response';
		}
	}

	let unreadCount = $derived(notifications.filter((n: NotificationData) => !n.isRead).length);
</script>

<div class="hero min-h-screen bg-base-200">
	<div class="hero-content w-full max-w-4xl flex-col">
		<div class="w-full">
			<div class="mb-6 flex items-center justify-between">
				<h1 class="text-4xl font-bold">Inbox</h1>
				{#if unreadCount > 0}
					<div class="badge badge-lg badge-primary">{unreadCount} unread</div>
				{:else}
					<div class="badge badge-ghost badge-lg">All caught up!</div>
				{/if}
			</div>

			<div class="space-y-4">
				{#each notifications as notification (notification.id)}
					<div class="card bg-base-100 shadow-sm {notification.isRead ? 'opacity-75' : ''}">
						<div class="card-body p-4">
							<div class="flex items-start gap-4">
								<div class="flex-shrink-0">
									<div class="avatar">
										<div class="flex w-12 items-center justify-center rounded-full bg-base-200">
											{#if notification.type === 'forum_reply'}
												<MessageSquare class="h-6 w-6" />
											{:else if notification.type === 'offer_response'}
												<HeartHandshake class="h-6 w-6" />
											{:else if notification.type === 'need_response'}
												<HandHelping class="h-6 w-6" />
											{/if}
										</div>
									</div>
								</div>

								<div class="min-w-0 flex-1">
									<div class="mb-2 flex items-center gap-2">
										<span class="badge {getTypeBadgeColor(notification.type)} badge-sm">
											{getTypeLabel(notification.type)}
										</span>
										{#if !notification.isRead}
											<span class="badge badge-xs badge-error">New</span>
										{/if}
									</div>

									<h3 class="mb-1 text-lg font-semibold {notification.isRead ? '' : 'font-bold'}">
										{notification.title}
									</h3>

									<p class="mb-2 text-sm text-base-content/70">
										{notification.message}
									</p>

									<div class="flex items-center justify-between">
										<span class="text-xs text-base-content/50">
											{notification.timestamp}
										</span>

										<div class="flex gap-2">
											{#if !notification.isRead}
												<button
													class="btn btn-ghost btn-sm"
													onclick={() => markAsRead(notification.id)}
												>
													<Check class="h-4 w-4" />
													Mark as read
												</button>
											{/if}

											<a href={resolve(notification.sourceUrl)} class="btn btn-sm btn-primary">
												<ExternalLink class="h-4 w-4" />
												View
											</a>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				{/each}
			</div>

			{#if notifications.length === 0}
				<div class="py-12 text-center">
					<div class="mb-4 text-6xl">📭</div>
					<h3 class="mb-2 text-xl font-semibold">No notifications yet</h3>
					<p class="text-base-content/70">
						When you get replies to your forum posts or responses to your offers/needs, they'll
						appear here.
					</p>
				</div>
			{/if}
		</div>
	</div>
</div>
