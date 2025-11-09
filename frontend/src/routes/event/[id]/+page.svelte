<script lang="ts">
	import {
		ArrowLeft,
		MapPin,
		Calendar,
		Clock,
		MessageSquare,
		Users,
		Send,
		HeartHandshake,
		HandHelping
	} from '@lucide/svelte';
	import { resolve } from '$app/paths';
	import MapComponent from '$lib/components/MapComponent.svelte';
	import CreatePostModal from '$lib/components/CreatePostModal.svelte';
	import UserAvatar from '$lib/components/UserAvatar.svelte';
	import { SERVER_URL } from '$lib/config';
	import { auth } from '$lib/stores/auth';
	import { toast } from '$lib/stores/toast';
	import { page } from '$app/stores';
	import { fetchAuthenticatedImage } from '$lib/utils/fetch';

	type ChatMessage = {
		id: string;
		author: string;
		avatar: string;
		content: string;
		timestamp: string;
	};

	type ParticipationRequest = {
		id: string;
		userId: string;
		userName: string;
		message: string;
		status: 'CREATED' | 'PENDING' | 'ACCEPTED' | 'REJECTED';
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
		participantCount?: number;
		userParticipationStatus?: 'none' | 'pending' | 'accepted' | 'rejected';
		participationRequests?: ParticipationRequest[];
		chatMessages?: ChatMessage[];
	};

	let { data } = $props<{ event: EventData; isOwner: boolean }>();
	let event = $derived(data.event);

	// Edit functionality
	let showEditModal = $state(false);
	let showParticipationModal = $state(false);

	async function updateEvent(data: any) {
		try {
			const eventId = $page.params.id;
			const originalEvent = event;

			// Map modal data to server format
			const serverData = {
				title: data.title,
				description: data.description,
				tags: data.tags,
				location: data.location
					? {
							lat: data.location.coordinates.lat,
							lon: data.location.coordinates.lng,
							name: data.location.name
						}
					: {
							lat: event.coordinates?.lat || 0,
							lon: event.coordinates?.lng || 0,
							name: event.location
						},
				maxParticipants: data.capacity,
				type: data.type === 'Offer' ? 'OFFER' : 'NEED',
				duration: parseDuration(data.duration),
				eventDate: data.eventDate
			};

			// Compare with original and send all fields, null for unchanged
			const updateData: any = {};

			// Helper to check if arrays are equal
			const arraysEqual = (a: string[] | undefined, b: string[] | undefined) => {
				const aArr = a || [];
				const bArr = b || [];
				if (aArr.length !== bArr.length) return false;
				return aArr.every((val, index) => val === bArr[index]);
			};

			// Helper to check if locations are equal
			const locationsEqual = (a: any, b: any) => {
				return a.lat === b.lat && a.lon === b.lon && a.name === b.name;
			};

			updateData.title = serverData.title !== originalEvent.title ? serverData.title : null;
			updateData.description =
				serverData.description !== originalEvent.description ? serverData.description : null;
			updateData.tags = !arraysEqual(serverData.tags, originalEvent.tags) ? serverData.tags : null;
			updateData.location = !locationsEqual(serverData.location, {
				lat: originalEvent.coordinates.lat,
				lon: originalEvent.coordinates.lng,
				name: originalEvent.location
			})
				? serverData.location
				: null;
			updateData.maxParticipants =
				serverData.maxParticipants !== originalEvent.maxParticipants
					? serverData.maxParticipants
					: null;
			updateData.type =
				serverData.type !== (originalEvent.type === 'Offer' ? 'OFFER' : 'NEED')
					? serverData.type
					: null;
			updateData.duration =
				serverData.duration !== parseDuration(originalEvent.duration) ? serverData.duration : null;
			updateData.eventDate =
				serverData.eventDate !== originalEvent.targetDate ? serverData.eventDate : null;

			// If no changes, do nothing
			const hasChanges = Object.values(updateData).some((value) => value !== null);
			if (!hasChanges) {
				showEditModal = false;
				return;
			}

			// Make API call
			const response = await fetch(`${SERVER_URL}/event/${eventId}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${$auth.token}`
				},
				body: JSON.stringify(updateData)
			});

			if (!response.ok) {
				throw new Error(`Failed to update event: ${response.statusText}`);
			}

			showEditModal = false;
			toast.success('Event updated successfully!');
			// Reload to see changes
			window.location.reload();
		} catch (error) {
			console.error('Error updating event:', error);
			toast.error('Failed to update event. Please try again.');
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
	let chatMessages = $state(event.chatMessages);
	let negotiationMessages = $state<ChatMessage[]>([]);
	let newNegotiationMessage = $state('');
	let backgroundLoaded = $state(false);
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
	let participationMessage = $state('');

	function sendParticipationRequest() {
		const message = participationMessage.trim();
		const request = {
			id: Date.now().toString(),
			userId: 'currentUser', // mock
			userName: 'You',
			message,
			status: 'PENDING' as const
		};
		participationRequests = [...participationRequests, request];
		userParticipationStatus = 'pending';
		participationMessage = '';
		// In real app, API call
		console.log('Sent participation request:', request);
	}

	function leaveEvent() {
		userParticipationStatus = 'none';
		// Remove user's request
		participationRequests = participationRequests.filter(
			(req: ParticipationRequest) => req.userId !== 'currentUser'
		);
		// In real app, API call
	}

	function acceptRequest(requestId: string) {
		participationRequests = participationRequests.map((req: ParticipationRequest) =>
			req.id === requestId ? { ...req, status: 'ACCEPTED' } : req
		);
		// Update user status if it's their request
		const request = participationRequests.find((r: ParticipationRequest) => r.id === requestId);
		if (request?.userId === 'currentUser') {
			userParticipationStatus = 'accepted';
		}
		// In real app, API call
	}

	function rejectRequest(requestId: string) {
		participationRequests = participationRequests.map((req: ParticipationRequest) =>
			req.id === requestId ? { ...req, status: 'REJECTED' } : req
		);
		const request = participationRequests.find((r: ParticipationRequest) => r.id === requestId);
		if (request?.userId === 'currentUser') {
			userParticipationStatus = 'rejected';
		}
		// In real app, API call
	}

	function removeParticipant(requestId: string) {
		participationRequests = participationRequests.map((req: ParticipationRequest) =>
			req.id === requestId ? { ...req, status: 'REJECTED' } : req
		);
		const request = participationRequests.find((r: ParticipationRequest) => r.id === requestId);
		if (request?.userId === 'currentUser') {
			userParticipationStatus = 'rejected';
		}
		// In real app, API call
	}

	function withdrawRequest() {
		participationRequests = participationRequests.filter(
			(req: ParticipationRequest) => req.userId !== 'currentUser'
		);
		userParticipationStatus = 'none';
		// In real app, API call
	}

	async function loadBackground() {
		backgroundBlobUrl = await fetchAuthenticatedImage(
			`${SERVER_URL}/assets/event/${$page.params.id}`,
			$auth.token!,
			'/assets/event.png'
		);
		backgroundLoaded = backgroundBlobUrl !== null;
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
						console.log('Could not get location name:', error);
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

	// Load background when component mounts
	$effect(() => {
		if ($auth.token && $page.params.id && data.event) {
			loadBackground();
		}

		// Cleanup blob URL on unmount (only if it's a blob URL)
		return () => {
			if (backgroundBlobUrl && backgroundBlobUrl.startsWith('blob:')) {
				URL.revokeObjectURL(backgroundBlobUrl);
			}
		};
	});

	// Scroll to bottom of chat when new messages arrive
	let chatContainer: HTMLElement;
	let negotiationContainer: HTMLElement;
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

{#if !data.event}
	<div class="flex min-h-screen items-center justify-center bg-base-200">
		<div class="loading loading-lg loading-spinner"></div>
	</div>
{:else}
	<div class="min-h-screen bg-base-200">
		<!-- Hero Section -->
		<div class="relative h-64 md:h-80 lg:h-96">
			<div class="absolute inset-0">
				<img src={backgroundImageSrc} alt={event.title} class="h-full w-full object-cover" />
				<div class="absolute inset-0 bg-black/40"></div>
			</div>
			<div class="relative flex h-full items-end p-6 text-white">
				<div class="container mx-auto flex items-end justify-between">
					<div>
						<a href={resolve('/')} class="btn mb-4 text-white btn-ghost hover:bg-white/20">
							<ArrowLeft class="h-4 w-4" />
							Back to Home
						</a>
						<div class="mb-2 flex items-center gap-3">
							<div
								class="badge badge-lg {event.type === 'OFFER' ? 'badge-primary' : 'badge-accent'}"
							>
								{#if event.type === 'OFFER'}
									<HeartHandshake class="h-4 w-4" />
								{:else}
									<HandHelping class="h-4 w-4" />
								{/if}
								{event.type}
							</div>
						</div>
						<h1 class="mb-2 text-3xl font-bold md:text-4xl lg:text-5xl">
							{event.title}
						</h1>
						<div class="flex flex-wrap gap-2">
							{#each event.tags as tag (tag)}
								<span class="badge badge-outline badge-lg badge-primary">{tag}</span>
							{/each}
						</div>
					</div>
					<div class="mt-4">
						{#if !data.isOwner}
							{#if userParticipationStatus === 'none'}
								<button
									class="btn btn-lg btn-primary"
									onclick={() => (showParticipationModal = true)}
								>
									{#if event.type === 'Offer'}
										<HeartHandshake class="h-5 w-5" />
									{:else}
										<HandHelping class="h-5 w-5" />
									{/if}
									Apply to {event.type.toLowerCase()}
								</button>
							{:else if userParticipationStatus === 'pending'}
								<div class="alert alert-info">
									<Clock class="h-5 w-5" />
									<span>Participation request pending approval.</span>
								</div>
								<button class="btn mt-2 btn-outline btn-warning" onclick={withdrawRequest}>
									Withdraw Request
								</button>
							{:else if userParticipationStatus === 'accepted'}
								<button class="btn btn-outline btn-lg btn-primary" onclick={leaveEvent}>
									{#if event.type === 'Offer'}
										<HeartHandshake class="h-5 w-5" />
									{:else}
										<HandHelping class="h-5 w-5" />
									{/if}
									Leave {event.type.toLowerCase()}
								</button>
								<div class="mt-4 alert alert-success">
									<HeartHandshake class="h-5 w-5" />
									<span>You are participating in this {event.type.toLowerCase()}!</span>
								</div>
							{:else if userParticipationStatus === 'rejected'}
								<div class="alert alert-error">
									<span>Your participation request was rejected.</span>
								</div>
								<button
									class="btn mt-2 btn-lg btn-primary"
									onclick={() => (showParticipationModal = true)}
								>
									Apply Again
								</button>
							{/if}
							{#if event.maxParticipants && event.participantCount >= event.maxParticipants}
								<p class="mt-2 text-sm text-white/70">
									This {event.type.toLowerCase()} is currently full
								</p>
							{/if}
						{/if}
						{#if data.isOwner}
							<button class="btn mt-2 btn-secondary" onclick={() => (showEditModal = true)}>
								Edit Event
							</button>
						{/if}
					</div>
				</div>
			</div>
		</div>

		<!-- Participation Management Section for Owner -->
		{#if data.isOwner}
			<div class="container mx-auto px-6 py-8">
				<div class="grid grid-cols-1 gap-8 lg:grid-cols-2">
					<!-- Pending Requests -->
					<div class="card bg-base-100 shadow-xl">
						<div class="card-body">
							<h3 class="card-title">
								<Users class="h-5 w-5" />
								Pending Participation Requests ({participationRequests.filter(
									(r: ParticipationRequest) => r.status === 'PENDING'
								).length})
							</h3>
							{#if participationRequests.some((r: ParticipationRequest) => r.status === 'PENDING')}
								{#each participationRequests.filter((r: ParticipationRequest) => r.status === 'PENDING') as request (request.id)}
									<div
										class="mb-4 border-b border-base-300 pb-4 last:mb-0 last:border-b-0 last:pb-0"
									>
										<div class="flex items-start justify-between">
											<div class="flex-1">
												<p class="font-semibold">{request.userName}</p>
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
									(r: ParticipationRequest) => r.status === 'ACCEPTED'
								).length})
							</h3>
							{#if participationRequests.some((r: ParticipationRequest) => r.status === 'ACCEPTED')}
								{#each participationRequests.filter((r: ParticipationRequest) => r.status === 'ACCEPTED') as request (request.id)}
									<div
										class="mb-4 border-b border-base-300 pb-4 last:mb-0 last:border-b-0 last:pb-0"
									>
										<div class="flex items-start justify-between">
											<div class="flex-1">
												<p class="font-semibold">{request.userName}</p>
												{#if request.message}
													<p class="mt-1 text-sm opacity-70">{request.message}</p>
												{/if}
											</div>
											<div class="flex gap-2">
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
			</div>
		{/if}

		<!-- Main Content -->
		<div class="container mx-auto px-6 py-8">
			<div class="grid grid-cols-1 gap-8 lg:grid-cols-2">
				<!-- Left Column: Event Details -->
				<div class="space-y-6">
					<!-- Event Description -->
					<div class="card bg-base-100 shadow-xl">
						<div class="card-body">
							<div class="mb-4 flex items-center gap-4">
								<UserAvatar userId={event.ownerId} />
								<div class="flex items-center gap-4 text-sm opacity-70">
									<div class="flex items-center gap-1">
										<Calendar class="h-4 w-4" />
										{event.createdAt}
										{#if event.updatedAt}
											<i class="text-xs italic opacity-60">Edited</i>
										{/if}
									</div>
									<div class="flex items-center gap-1">
										<Users class="h-4 w-4" />
										{event.participantCount} participants
									</div>
								</div>
							</div>
							<p class="text-base leading-relaxed whitespace-pre-wrap">{event.description}</p>
							<div class="divider"></div>
							<div class="mt-4 flex flex-wrap gap-4">
								<div class="flex items-center gap-4">
									{#if event.targetDate}
										<div class="flex items-center gap-2">
											<Calendar class="h-4 w-4 text-primary" />
											<span class="text-sm font-medium">Event Date: {event.targetDate}</span>
										</div>
									{/if}
									{#if event.duration}
										<div class="flex items-center gap-2">
											<Clock class="h-4 w-4 text-primary" />
											<span class="text-sm font-medium">Duration: {event.duration}</span>
										</div>
									{/if}
								</div>
								{#if event.maxParticipants}
									<div class="flex items-center gap-2">
										<Users class="h-4 w-4 text-primary" />
										<span class="text-sm font-medium">Capacity: {event.maxParticipants}</span>
									</div>
								{/if}
							</div>
						</div>
					</div>

					<!-- Map Section -->
					<div class="card bg-base-100 shadow-xl">
						<div class="card-body">
							<h3 class="card-title justify-between">
								<span class="flex items-center gap-2">
									<MapPin class="h-5 w-5" />
									Location
								</span>
								<span class="flex items-center gap-2">
									<span class="font-medium">{event.location}</span>
									{#if distance}
										<span class="badge badge-ghost">~{distance} away</span>
									{/if}
								</span>
							</h3>
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
					</div>
				</div>

				<!-- Right Column: Chat -->
				<div class="card bg-base-100 shadow-xl">
					<div class="card-body">
						<!-- Tabs -->
						<div class="tabs-boxed mb-4 tabs">
							<a
								class="tab-lg tab {activeTab === 'public' ? 'tab-active' : ''}"
								onclick={() => (activeTab = 'public')}
							>
								<MessageSquare class="mr-2 h-5 w-5" />
								<span class="text-lg font-semibold">Public Chat</span>
								<span class="ml-2 badge badge-sm">{chatMessages.length}</span>
							</a>
							<a
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
							</a>
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
												class="chat-bubble {message.author === 'You' ? 'chat-bubble-primary' : ''}"
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
												class="chat-bubble {message.author === 'You' ? 'chat-bubble-primary' : ''}"
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
								<button class="btn btn-primary" onclick={sendMessage} disabled={!newMessage.trim()}>
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
												class="chat-bubble {message.author === 'You' ? 'chat-bubble-primary' : ''}"
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
												class="chat-bubble {message.author === 'You' ? 'chat-bubble-primary' : ''}"
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
				onclick={() => {
					showParticipationModal = false;
					participationMessage = '';
				}}
			></div>
		</div>

		<CreatePostModal
			bind:open={showEditModal}
			editData={data.event}
			mode="edit"
			on:update={updateEvent}
		/>
	</div>
{/if}
