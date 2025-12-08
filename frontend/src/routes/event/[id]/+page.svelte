<script lang="ts">
	import {
		MapPin,
		Calendar,
		MessageSquare,
		Users,
		Send,
		HeartHandshake,
		HandHelping,
		TriangleAlert,
		Info,
		Handshake,
		CircleCheck,
		Clock
	} from '@lucide/svelte';
	import { breadcrumbs } from '$lib/stores/breadcrumbs';

	import MapComponent from '$lib/components/MapComponent.svelte';
	import EventModal from '$lib/components/EventModal.svelte';
	import UserAvatar from '$lib/components/UserAvatar.svelte';
	import { HIVE_API } from '$lib/config';
	import { auth } from '$lib/stores/auth';
	import { toast } from '$lib/stores/toast';
	import { page } from '$app/stores';
	import { fetchAuthenticatedImage, fetchWithAuthClient } from '$lib/utils/fetch';

	type ChatMessage = {
		id: string;
		author: string;
		avatar: string;
		content: string;
		timestamp: string;
	};

	type ParticipationRequest = {
		id: string;
		userId: number;
		userName: string;
		message: string;
		status:
			| 'SENT'
			| 'PENDING'
			| 'CREATED'
			| 'NEGOTIATING'
			| 'ACCEPTED'
			| 'REJECTED'
			| 'WITHDRAWN'
			| 'COMPLETED';
	};

	type EventData = {
		id: number;
		title: string;
		description: string;
		tags: string[];
		location: string;
		maxParticipants: number;
		type: 'OFFER' | 'NEED';
		duration: string;
		targetDate?: string;
		createdAt: string;
		updatedAt: string;
		ownerId: number;
		// Additional fields for full event view

		coordinates?: { lat: number; lng: number };
		userParticipationStatus?: 'none' | 'pending' | 'accepted' | 'rejected';
		participationRequests?: ParticipationRequest[];
		chatMessages?: ChatMessage[];
	};

	let { data } = $props<{
		event: EventData;
		isOwner: boolean;
		currentUserId: number;
	}>();
	let event = $derived(data.event);
	let userTimeBank = $state<number | null>(null);

	// Check if event is completed (date has passed)
	let isEventCompleted = $derived(() => {
		if (!event.targetDate) return false;
		const eventDate = new Date(event.targetDate);
		const today = new Date();
		today.setHours(0, 0, 0, 0); // Set to start of day
		return eventDate < today;
	});

	// Check if user has enough time for the event
	let hasEnoughTime = $derived(() => {
		const durationHours = parseDuration(event.duration || '0 hour');
		return userTimeBank === null || userTimeBank >= durationHours;
	});

	breadcrumbs.set([{ text: 'Event' }, { text: data.event.title }]);

	// Edit functionality
	let showEditModal = $state(false);
	let showParticipationModal = $state(false);

	async function updateEvent(event: CustomEvent) {
		const eventData = event.detail;
		try {
			const eventId = $page.params.id;

			// Map the data to server format, full object with updated fields
			const serverData = {
				title: eventData.title !== undefined ? eventData.title : data.event.title,
				description:
					eventData.description !== undefined ? eventData.description : data.event.description,
				tags: eventData.tags !== undefined ? eventData.tags : data.event.tags,
				location:
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
							: undefined,
				maxParticipants:
					eventData.maxParticipants !== undefined
						? eventData.maxParticipants
						: data.event.maxParticipants,
				type: eventData.type || data.event.type,
				duration:
					eventData.duration !== undefined
						? eventData.duration
						: parseDuration(data.event.duration),
				eventDate: eventData.eventDate !== undefined ? eventData.eventDate : data.event.targetDate
			};

			// Make API call
			const response = await fetchWithAuthClient(`${HIVE_API}/event/${eventId}`, $auth.token!, {
				method: 'PUT',
				body: JSON.stringify(serverData)
			});

			showEditModal = false;
			toast.success('Event updated successfully!');
			// Reload to see changes
			window.location.reload();
		} catch (error) {
			toast.error(`Failed to update event. Please try again. ${error}`);
		}
	}

	function parseDuration(durationStr: string): number {
		if (!durationStr) return 0;
		const match = durationStr.match(/^(\d+)\s*(hour|day|week|month)/i);
		if (!match) return 0;
		const num = parseInt(match[1]);
		const unit = match[2].toLowerCase();
		switch (unit) {
			case 'hour':
				return num;
			case 'day':
				return num * 24;
			case 'week':
				return num * 24 * 7;
			case 'month':
				return num * 24 * 30; // approximate
			default:
				return num;
		}
	}

	// Chat functionality
	let activeTab = $state('public');
	let newMessage = $state('');
	let chatMessages = $state<ChatMessage[]>([]);

	$effect(() => {
		chatMessages = event.chatMessages || [];
	});
	let negotiationMessages = $state<ChatMessage[]>([]);
	let newNegotiationMessage = $state('');

	let backgroundBlobUrl = $state<string | null>(null);

	// Use default event image when no background is loaded
	let backgroundImageSrc = $derived(backgroundBlobUrl || '/assets/event.png');

	function sendMessage() {
		if (newMessage.trim()) {
			const message: ChatMessage = {
				id: Date.now().toString(),
				author: 'You', // In a real app, this would be the current user
				avatar:
					'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=100&q=80',
				content: newMessage.trim(),
				timestamp: new Date().toLocaleString()
			};
			chatMessages = [...chatMessages, message];
			newMessage = '';
			// Scroll to bottom after sending
			setTimeout(() => {
				if (chatContainer) {
					chatContainer.scrollTop = chatContainer.scrollHeight;
				}
			}, 0);
		}
	}

	function sendNegotiationMessage() {
		if (newNegotiationMessage.trim()) {
			const message: ChatMessage = {
				id: Date.now().toString(),
				author: 'You', // In a real app, this would be the current user
				avatar:
					'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=100&q=80',
				content: newNegotiationMessage.trim(),
				timestamp: new Date().toLocaleString()
			};
			negotiationMessages = [...negotiationMessages, message];
			newNegotiationMessage = '';
			// Scroll to bottom after sending
			setTimeout(() => {
				if (negotiationContainer) {
					negotiationContainer.scrollTop = negotiationContainer.scrollHeight;
				}
			}, 0);
		}
	}

	// Participation functionality
	let userParticipationStatus = $state(event.userParticipationStatus);
	let participationRequests = $state(event.participationRequests);

	// Steps for participation status
	let steps = $state([
		{ label: 'Request Sent', status: 'SENT' },
		{ label: 'Negotiating', status: 'NEGOTIATING' },
		{ label: 'Decision Made', status: 'DECIDED' },
		{ label: 'Completed', status: 'COMPLETED' }
	]);

	$effect(() => {
		steps[2].label =
			currentStatus === 'ACCEPTED'
				? 'Accepted'
				: currentStatus === 'REJECTED'
					? 'Rejected'
					: 'Decision Made';
	});

	// Get current user's participation status
	let userRequest = $derived(
		participationRequests.find((r: ParticipationRequest) => r.userId === data.currentUserId)
	);
	let currentStatus = $derived(userRequest?.status);
	let currentStepIndex = $derived(
		currentStatus === 'SENT'
			? 0
			: currentStatus === 'NEGOTIATING'
				? 1
				: currentStatus === 'ACCEPTED' || currentStatus === 'REJECTED'
					? 2
					: currentStatus === 'COMPLETED'
						? 3
						: -1
	);

	type StepType = {
		color: string;
		message?: string;
		icon?: typeof CircleCheck;
	};

	let stepClass = $derived((): StepType => {
		switch (currentStatus) {
			case 'COMPLETED':
				return { color: 'success' };
			case 'ACCEPTED':
				return {
					color: 'success',
					icon: CircleCheck,
					message: 'Negotiation was succesful. See you there.'
				};
			case 'REJECTED':
				return { color: 'error', message: 'You were rejeceted by the owner.', icon: TriangleAlert };
			case 'SENT':
				return { color: 'accent', message: 'Waiting for approval.', icon: Info };
			case 'NEGOTIATING':
				return { color: 'info', message: 'Negotiating.', icon: Handshake };
			default:
				return { color: 'primary' };
		}
	});
	let participationMessage = $state('');

	async function sendParticipationRequest() {
		const message = participationMessage.trim();
		try {
			const eventId = $page.params.id;
			await fetchWithAuthClient(
				`${HIVE_API}/event/${eventId}/participation?message=${encodeURIComponent(message)}`,
				$auth.token!,
				{
					method: 'POST'
				}
			);
			// Update local state
			const currentUserId = $auth.userCard?.id;
			if (!currentUserId) return;
			const request = {
				id: currentUserId.toString(),
				userId: currentUserId,
				userName: '', // Will be loaded by UserAvatar
				message,
				status: 'SENT' as const
			};
			participationRequests = [...participationRequests, request];
			userParticipationStatus = 'pending';
			participationMessage = '';
			toast.success('Participation request sent successfully!');
		} catch (error) {
			toast.error(`Failed to send participation request. Please try again. ${error}`);
		}
	}

	async function leaveEvent() {
		try {
			const eventId = $page.params.id;
			await fetchWithAuthClient(`${HIVE_API}/event/${eventId}/participation`, $auth.token!, {
				method: 'DELETE'
			});
			// Update local state
			const currentUserId = $auth.userCard?.id;
			if (!currentUserId) return;
			participationRequests = participationRequests.filter(
				(req: ParticipationRequest) => req.userId !== currentUserId
			);
			userParticipationStatus = 'none';
			toast.success('Participation request withdrawn successfully!');
		} catch (error) {
			toast.error(`Failed to withdraw participation. Please try again. ${error}`);
		}
	}

	async function updateParticipationStatus(
		requestId: string,
		state: 'ACCEPTED' | 'REJECTED' | 'NEGOTIATING'
	) {
		const request = participationRequests.find((r: ParticipationRequest) => r.id === requestId);
		if (!request) return;

		try {
			const eventId = $page.params.id;
			await fetchWithAuthClient(`${HIVE_API}/event/${eventId}/participation`, $auth.token!, {
				method: 'PUT',
				body: JSON.stringify({
					userId: request.userId,
					state
				})
			});

			participationRequests = participationRequests.map((req: ParticipationRequest) =>
				req.id === requestId ? { ...req, status: state } : req
			);

			// Update user status if it's their request
			const currentUserId = $auth.userCard?.id;
			if (request?.userId === currentUserId) {
				if (state === 'ACCEPTED') {
					userParticipationStatus = 'accepted';
				} else if (state === 'REJECTED') {
					userParticipationStatus = 'rejected';
				}
			}

			const actionMessages = {
				ACCEPTED: 'Participant accepted successfully!',
				REJECTED: 'Participant rejected successfully!',
				NEGOTIATING: 'Negotiation started successfully!'
			};
			toast.success(actionMessages[state]);
		} catch (error) {
			toast.error(`Failed to update participation status. Please try again. ${error}`);
		}
	}

	// Wrapper functions for backward compatibility and clarity
	async function acceptRequest(requestId: string) {
		await updateParticipationStatus(requestId, 'ACCEPTED');
	}

	async function rejectRequest(requestId: string) {
		await updateParticipationStatus(requestId, 'REJECTED');
	}

	async function removeParticipant(requestId: string) {
		await updateParticipationStatus(requestId, 'REJECTED');
	}

	async function startNegotiation(requestId: string) {
		await updateParticipationStatus(requestId, 'NEGOTIATING');
	}

	async function loadBackground() {
		backgroundBlobUrl = await fetchAuthenticatedImage(
			`${HIVE_API}/assets/event/${$page.params.id}`,
			$auth.token!,
			'/assets/event.png'
		);
	}

	async function loadUserTimeBank() {
		try {
			const response = await fetchWithAuthClient(
				`${HIVE_API}/user/${data.currentUserId}`,
				$auth.token!
			);
			const userData = await response.json();
			userTimeBank = userData.timeBank || 0;
		} catch (error) {
			console.error('Failed to load user timeBank:', error);
			userTimeBank = 0; // Default to 0 on error
		}
	}

	// User location functionality
	let userLatitude = $state<number | undefined>(undefined);
	let userLongitude = $state<number | undefined>(undefined);
	let userLocationName = $state<string | undefined>(undefined);
	let locationError = $state<string | undefined>(undefined);

	// Get user's current location
	$effect(() => {
		if (typeof navigator !== 'undefined' && navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(
				async (position) => {
					userLatitude = position.coords.latitude;
					userLongitude = position.coords.longitude;

					// Try to get location name using reverse geocoding
					try {
						const response = await fetch(
							`https://nominatim.openstreetmap.org/reverse?format=json&lat=${userLatitude}&lon=${userLongitude}&zoom=10&addressdetails=1`
						);
						const data = await response.json();
						if (data && data.display_name) {
							userLocationName = data.display_name.split(',')[0]; // Get city name
						}
					} catch (error) {
						toast.error(`Failed to get location name ${error}`);
						userLocationName = 'Your Location';
					}
				},
				(error) => {
					console.log('Geolocation error:', error);
					locationError = 'Unable to get your location. Please enable location services.';
				},
				{
					enableHighAccuracy: true,
					timeout: 10000,
					maximumAge: 300000 // 5 minutes
				}
			);
		} else {
			locationError = 'Geolocation is not supported by this browser.';
		}
	});

	// Map and distance functionality
	let userLocation = $state<{ lat: number; lng: number } | null>(null);
	let distance = $state<string | null>(null);

	// Calculate distance using Haversine formula
	function calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
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
			(r: ParticipationRequest) => r.status === 'ACCEPTED' || r.status === 'NEGOTIATING'
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
					distance = dist < 1 ? `${Math.round(dist * 1000)}m` : `${dist.toFixed(1)}km`;
				},
				(error) => {
					console.log('Error getting location:', error);
					distance = 'Location access denied';
				}
			);
		} else {
			distance = 'Geolocation not supported';
		}
	}

	// Get location on component mount
	$effect(() => {
		getUserLocation();
	});

	// Load background and user timeBank when component mounts
	$effect(() => {
		if ($auth.token && $page.params.id && data.event) {
			loadBackground();
			if (!data.isOwner) {
				loadUserTimeBank();
			}
		}

		// Cleanup blob URL on unmount (only if it's a blob URL)
		return () => {
			if (backgroundBlobUrl && backgroundBlobUrl.startsWith('blob:')) {
				URL.revokeObjectURL(backgroundBlobUrl);
			}
		};
	});

	// Scroll to bottom of chat when new messages arrive
	let chatContainer = $state<HTMLElement | undefined>();
	let negotiationContainer = $state<HTMLElement | undefined>();
	$effect(() => {
		if (chatContainer) {
			chatContainer.scrollTop = chatContainer.scrollHeight;
		}
	});
	$effect(() => {
		if (negotiationContainer) {
			negotiationContainer.scrollTop = negotiationContainer.scrollHeight;
		}
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
									class="badge badge-lg {event.type === 'OFFER' ? 'badge-success' : 'badge-accent'}"
								>
									{#if event.type === 'OFFER'}
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
									<span>{getParticipantCount()}/{event.maxParticipants} participants</span>
								</div>
							</div>

							{#if data.isOwner}
								<button class="btn mt-2 btn-secondary" onclick={() => (showEditModal = true)}>
									Edit Event
								</button>
							{:else if userParticipationStatus === 'none'}
								{#if isEventCompleted}
									<div class="mt-2">
										<button class="btn btn-disabled" disabled>
											{#if event.type === 'OFFER'}
												<HeartHandshake class="h-5 w-5" />
											{:else}
												<HandHelping class="h-5 w-5" />
											{/if}
											Apply to {event.type.toLowerCase()}
										</button>
										<div class="mt-2 alert alert-info">
											<Info class="h-6 w-6" />
											<span>Event is completed</span>
										</div>
									</div>
								{:else if userTimeBank !== null && !hasEnoughTime}
									<div class="mt-2">
										<button class="btn btn-disabled" disabled>
											{#if event.type === 'OFFER'}
												<HeartHandshake class="h-5 w-5" />
											{:else}
												<HandHelping class="h-5 w-5" />
											{/if}
											Apply to {event.type.toLowerCase()}
										</button>
										<div class="mt-2 alert alert-warning">
											<TriangleAlert class="h-6 w-6" />
											<span>Not enough time available</span>
										</div>
									</div>
								{:else if getParticipantCount() >= event.maxParticipants}
									<span class="mt-2 text-sm opacity-70">Event is full</span>
								{:else}
									<button
										class="btn mt-2 btn-primary"
										onclick={() => (showParticipationModal = true)}
									>
										{#if event.type === 'OFFER'}
											<HeartHandshake class="h-5 w-5" />
										{:else}
											<HandHelping class="h-5 w-5" />
										{/if}
										Apply to {event.type.toLowerCase()}
									</button>
								{/if}
							{:else if userParticipationStatus !== 'accepted' && userParticipationStatus !== 'rejected' && userParticipationStatus !== 'completed'}
								<button class="btn mt-2 btn-info" onclick={leaveEvent}>
									Withdraw Participation
								</button>
							{/if}
						</div>
					</div>

					{#if !data.isOwner && userRequest && currentStepIndex >= 0}
						<div class="container mx-auto px-6 py-8">
							<div class="card">
								<div class="card-body">
									<ul class="steps steps-vertical lg:steps-horizontal">
										{#each steps as step, i (step.status)}
											<li class="step {i <= currentStepIndex ? 'step-' + stepClass().color : ''}">
												{step.label}
											</li>
										{/each}
									</ul>
									{#if stepClass().icon}
										<div role="alert" class="mt-4 alert alert-soft {'alert-' + stepClass().color}">
											<svelte:component
												this={stepClass().icon}
												class="h-6 w-6 shrink-0 stroke-current"
											/>
											<span>{stepClass().message}</span>
										</div>
									{/if}
								</div>
							</div>
						</div>
					{/if}

					{#if data.isOwner}
						<div class="grid grid-cols-1 gap-8 lg:grid-cols-2">
							<!-- Pending Requests -->
							<div class="card bg-base-100 shadow-xl">
								<div class="card-body">
									<h3 class="card-title">
										<Users class="h-5 w-5" />
										Pending Participation Requests ({participationRequests.filter(
											(r: ParticipationRequest) =>
												r.status === 'SENT' || r.status === 'PENDING' || r.status === 'CREATED'
										).length})
									</h3>
									{#if participationRequests.some((r: ParticipationRequest) => r.status === 'SENT' || r.status === 'PENDING' || r.status === 'CREATED')}
										{#each participationRequests.filter((r: ParticipationRequest) => r.status === 'SENT' || r.status === 'PENDING' || r.status === 'CREATED') as request (request.id)}
											<div
												class="mb-4 border-b border-base-300 pb-4 last:mb-0 last:border-b-0 last:pb-0"
											>
												<div class="flex items-start justify-between">
													<div class="flex-1">
														<UserAvatar userId={request.userId} />
														{#if request.message}
															<p class="mt-1 text-sm opacity-70">{request.message}</p>
														{/if}
													</div>
													<div class="flex gap-2">
														<button
															class="btn btn-sm btn-success"
															onclick={() => acceptRequest(request.id)}
														>
															Accept
														</button>
														<button
															class="btn btn-sm btn-error"
															onclick={() => rejectRequest(request.id)}
														>
															Reject
														</button>
													</div>
												</div>
											</div>
										{/each}
									{:else}
										<p class="text-sm opacity-70">No pending participation requests.</p>
									{/if}
								</div>
							</div>

							<!-- Accepted Participants -->
							<div class="card bg-base-100 shadow-xl">
								<div class="card-body">
									<h3 class="card-title">
										<Users class="h-5 w-5" />
										Accepted Participants ({participationRequests.filter(
											(r: ParticipationRequest) =>
												r.status === 'ACCEPTED' ||
												r.status === 'NEGOTIATING' ||
												r.status === 'COMPLETED'
										).length})
									</h3>
									{#if participationRequests.some((r: ParticipationRequest) => r.status === 'ACCEPTED' || r.status === 'NEGOTIATING' || r.status === 'COMPLETED')}
										{#each participationRequests.filter((r: ParticipationRequest) => r.status === 'ACCEPTED' || r.status === 'NEGOTIATING' || r.status === 'COMPLETED') as request (request.id)}
											<div
												class="mb-4 border-b border-base-300 pb-4 last:mb-0 last:border-b-0 last:pb-0"
											>
												<div class="flex items-start justify-between">
													<div class="flex-1">
														<UserAvatar userId={request.userId} />
														{#if request.message}
															<p class="mt-1 text-sm opacity-70">{request.message}</p>
														{/if}
													</div>
													<div class="flex gap-2">
														<button
															class="btn btn-sm btn-info"
															onclick={() => startNegotiation(request.id)}
														>
															Start Negotiation
														</button>
														<button
															class="btn btn-sm btn-error"
															onclick={() => removeParticipant(request.id)}
														>
															Remove
														</button>
													</div>
												</div>
											</div>
										{/each}
									{:else}
										<p class="text-sm opacity-70">There are no accepted participants.</p>
									{/if}
								</div>
							</div>
						</div>
					{/if}
				</div>
				<!-- Main Content -->
				<div class="container mx-auto">
					<div class="grid grid-cols-1 gap-8 lg:grid-cols-2">
						<!-- Left Column: Event Details -->
						<div class="space-y-6"></div>

						<!-- Right Column: Chat -->
						<div class="card bg-base-100 shadow-xl">
							<div class="card-body">
								<!-- Tabs -->
								<div class="tabs-boxed mb-4 tabs">
									<button
										type="button"
										class="tab-lg tab {activeTab === 'public' ? 'tab-active' : ''}"
										onclick={() => (activeTab = 'public')}
									>
										<MessageSquare class="mr-2 h-5 w-5" />
										<span class="text-lg font-semibold">Public Chat</span>
										<span class="ml-2 badge badge-sm">{chatMessages.length}</span>
									</button>
									<button
										type="button"
										class="tab-lg tab {activeTab === 'negotiation'
											? 'tab-active'
											: ''} {userParticipationStatus !== 'accepted' && !data.isOwner
											? 'pointer-events-none opacity-50'
											: ''}"
										onclick={() => {
											if (userParticipationStatus === 'accepted' || data.isOwner)
												activeTab = 'negotiation';
										}}
									>
										<MessageSquare class="mr-2 h-5 w-5" />
										<span class="text-lg font-semibold">Negotiation</span>
										<span class="ml-2 badge badge-sm">{negotiationMessages.length}</span>
									</button>
								</div>

								{#if activeTab === 'public'}
									<!-- Chat Messages -->
									<div bind:this={chatContainer} class="mb-4 h-96 overflow-y-auto p-2">
										{#each chatMessages as message (message.id)}
											{#if message.author !== 'You'}
												<div class="chat-start chat">
													<UserAvatar userId={message.author === 'You' ? 0 : 2} />
													<div class="chat-header">
														{message.author}
														<time class="text-xs opacity-50">{message.timestamp}</time>
													</div>
													<div
														class="chat-bubble {message.author === 'You'
															? 'chat-bubble-primary'
															: ''}"
													>
														{message.content}
													</div>
												</div>
											{:else}
												<div class="chat-end chat">
													<div class="chat-header">
														{message.author}
														<time class="text-xs opacity-50">{message.timestamp}</time>
													</div>
													<div
														class="chat-bubble {message.author === 'You'
															? 'chat-bubble-primary'
															: ''}"
													>
														{message.content}
													</div>
												</div>
											{/if}
										{/each}
									</div>

									<!-- Message Input -->
									<div class="flex gap-2">
										<input
											type="text"
											placeholder="Type a message..."
											class="input-bordered input flex-1"
											bind:value={newMessage}
											onkeydown={(e) => {
												if (e.key === 'Enter') {
													sendMessage();
												}
											}}
										/>
										<button
											class="btn btn-primary"
											onclick={sendMessage}
											disabled={!newMessage.trim()}
										>
											<Send class="h-4 w-4" />
										</button>
									</div>
								{:else if activeTab === 'negotiation'}
									<!-- Negotiation Messages -->
									<div bind:this={negotiationContainer} class="mb-4 h-96 overflow-y-auto p-2">
										{#each negotiationMessages as message (message.id)}
											{#if message.author !== 'You'}
												<div class="chat-start chat">
													<UserAvatar userId={message.author === 'You' ? 0 : 2} />
													<div class="chat-header">
														{message.author}
														<time class="text-xs opacity-50">{message.timestamp}</time>
													</div>
													<div
														class="chat-bubble {message.author === 'You'
															? 'chat-bubble-primary'
															: ''}"
													>
														{message.content}
													</div>
												</div>
											{:else}
												<div class="chat-end chat">
													<div class="chat-header">
														{message.author}
														<time class="text-xs opacity-50">{message.timestamp}</time>
													</div>
													<div
														class="chat-bubble {message.author === 'You'
															? 'chat-bubble-primary'
															: ''}"
													>
														{message.content}
													</div>
												</div>
											{/if}
										{/each}
									</div>

									<!-- Negotiation Message Input -->
									<div class="flex gap-2">
										<input
											type="text"
											placeholder="Type a message..."
											class="input-bordered input flex-1"
											bind:value={newNegotiationMessage}
											onkeydown={(e) => {
												if (e.key === 'Enter') {
													sendNegotiationMessage();
												}
											}}
										/>
										<button
											class="btn btn-primary"
											onclick={sendNegotiationMessage}
											disabled={!newNegotiationMessage.trim()}
										>
											<Send class="h-4 w-4" />
										</button>
									</div>
								{/if}
							</div>
						</div>
					</div>
				</div>
			{/if}
		</div>
	</div>
</div>

<!-- Participation Modal -->
<div class="modal" class:modal-open={showParticipationModal}>
	<div class="modal-box">
		<h3 class="text-lg font-bold">Send Participation Request</h3>
		<p class="py-4">Provide additional information for your request (optional).</p>
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
					participationMessage = '';
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
			participationMessage = '';
		}}
		onkeydown={(e) => {
			if (e.key === 'Enter' || e.key === ' ') {
				showParticipationModal = false;
				participationMessage = '';
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
	currentDuration={data.event.duration ? parseInt(data.event.duration.split(' ')[0]) : 0}
	currentEventDate={data.event.targetDate}
	on:update={updateEvent}
/>
