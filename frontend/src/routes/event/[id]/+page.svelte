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
	import MapComponent from '$lib/components/MapComponent.svelte';
	import CreatePostModal from '$lib/components/CreatePostModal.svelte';
	import UserAvatar from '$lib/components/UserAvatar.svelte';

	type ChatMessage = {
		id: string;
		author: string;
		avatar: string;
		content: string;
		timestamp: string;
	};

	type EventData = {
		id: string;
		title: string;
		description: string;
		author: string;
		authorAvatar: string;
		tags: string[];
		backgroundImage: string;
		location: string;
		coordinates: { lat: number; lng: number };
		type: 'Offer' | 'Need';
		participantCount: number;
		maxParticipants?: number;
		createdAt: string;
		isParticipating: boolean;
		chatMessages: ChatMessage[];
	};

	let { data } = $props<{ event: EventData; isOwner: boolean }>();
	let event = $derived(data.event);

	// Edit functionality
	let showEditModal = $state(false);

	function updateEvent(data: any) {
		// In real app, API call to update
		console.log('Update event:', data);
		showEditModal = false;
		// For demo, reload to see changes if any
		window.location.reload();
	}

	// Chat functionality
	let newMessage = $state('');
	let chatMessages = $state(event.chatMessages);

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

	// Participation functionality
	let isParticipating = $state(event.isParticipating);

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

	function toggleParticipation() {
		isParticipating = !isParticipating;
		// In a real app, this would make an API call
	}

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
						data.event.coordinates.lat,
						data.event.coordinates.lng
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

	// Scroll to bottom of chat when new messages arrive
	let chatContainer: HTMLElement;
	$effect(() => {
		if (chatContainer) {
			chatContainer.scrollTop = chatContainer.scrollHeight;
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
				<img src={event.backgroundImage} alt={event.title} class="h-full w-full object-cover" />
				<div class="absolute inset-0 bg-black/40"></div>
			</div>
			<div class="relative flex h-full items-end p-6 text-white">
				<div class="container mx-auto flex items-end justify-between">
					<div>
						<a href="/" class="btn mb-4 text-white btn-ghost hover:bg-white/20">
							<ArrowLeft class="h-4 w-4" />
							Back to Home
						</a>
						<div class="mb-2 flex items-center gap-3">
							<div
								class="badge badge-lg {event.type === 'Offer' ? 'badge-secondary' : 'badge-accent'}"
							>
								{#if event.type === 'Offer'}
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
								<span class="badge badge-outline badge-lg">{tag}</span>
							{/each}
						</div>
					</div>
					<div class="mt-4">
						{#if !data.isOwner}
							<button
								class="btn btn-lg btn-primary {isParticipating ? 'btn-outline' : ''}"
								onclick={toggleParticipation}
							>
								{#if event.type === 'Offer'}
									<HeartHandshake class="h-5 w-5" />
								{:else}
									<HandHelping class="h-5 w-5" />
								{/if}
								{isParticipating ? 'Leave' : 'Apply to'}
								{event.type.toLowerCase()}
							</button>
							{#if isParticipating}
								<div class="mt-4 alert alert-success">
									<HeartHandshake class="h-5 w-5" />
									<span>You are participating in this {event.type.toLowerCase()}!</span>
								</div>
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

		<!-- Main Content -->
		<div class="container mx-auto px-6 py-8">
			<div class="grid grid-cols-1 gap-8 lg:grid-cols-2">
				<!-- Left Column: Event Details -->
				<div class="space-y-6">
					<!-- Event Description -->
					<div class="card bg-base-100 shadow-xl">
						<div class="card-body">
							<div class="mb-4 flex items-start gap-4">
								<UserAvatar
									avatar={event.authorAvatar}
									name={event.author}
									showName={false}
									size="w-12"
									userId="1"
									userData={{
										fullName: event.author,
										bio: 'Event organizer and community builder',
										interests: ['Community Events', 'Networking'],
										userScore: 85
									}}
								/>
								<div class="flex-1">
									<h3 class="text-lg font-semibold">{event.author}</h3>
									<div class="flex items-center gap-4 text-sm opacity-70">
										<div class="flex items-center gap-1">
											<Calendar class="h-4 w-4" />
											{event.createdAt}
										</div>
										<div class="flex items-center gap-1">
											<Users class="h-4 w-4" />
											{event.participantCount} participants
										</div>
									</div>
								</div>
							</div>
							<p class="text-base leading-relaxed">{event.description}</p>
							<div class="mt-4 grid grid-cols-1 gap-2 md:grid-cols-3">
								{#if event.targetDate}
									<div class="flex items-center gap-2">
										<Calendar class="h-4 w-4 text-primary" />
										<span class="text-sm font-medium">Date: {event.targetDate}</span>
									</div>
								{/if}
								{#if event.duration}
									<div class="flex items-center gap-2">
										<Clock class="h-4 w-4 text-primary" />
										<span class="text-sm font-medium">Duration: {event.duration}</span>
									</div>
								{/if}
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
								latitude={event.coordinates.lat}
								longitude={event.coordinates.lng}
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
				<div>
					<div class="card bg-base-100 shadow-xl">
						<div class="card-body">
							<h3 class="card-title">
								<MessageSquare class="h-5 w-5" />
								Public Chat
								<span class="badge badge-sm">{chatMessages.length}</span>
							</h3>

							<!-- Chat Messages -->
							<div
								bind:this={chatContainer}
								class="mb-4 h-96 overflow-y-auto rounded-lg bg-base-200 p-2"
							>
								{#each chatMessages as message (message.id)}
									<div class="chat {message.author === 'You' ? 'chat-end' : 'chat-start'}">
										<UserAvatar
											avatarClass="chat-image"
											avatar={message.avatar}
											name={message.author}
											showName={false}
											userId={message.author === 'You' ? '0' : '2'}
											userData={{
												fullName: message.author,
												bio: message.author === 'You' ? 'Event participant' : 'Community member',
												interests: ['Events', 'Networking'],
												userScore: message.author === 'You' ? 90 : 75
											}}
										/>
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
						</div>
					</div>
				</div>
			</div>
		</div>
		<CreatePostModal
			bind:open={showEditModal}
			editData={data.event}
			mode="edit"
			on:update={updateEvent}
		/>
	</div>
{/if}
