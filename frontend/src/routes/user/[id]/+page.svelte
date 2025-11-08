<script lang="ts">
	import {
		Annoyed,
		Frown,
		HandHelping,
		HeartHandshake,
		Meh,
		Smile,
		Edit,
		Hourglass
	} from '@lucide/svelte';
	import type { PageProps } from './$types';
	import SearchResultCard from '$lib/components/SearchResultCard.svelte';
	import EditProfileModal from '$lib/components/EditProfileModal.svelte';
	import { SERVER_URL } from '$lib/config';
	import { auth } from '$lib/stores/auth';
	import { page } from '$app/stores';
	import { toast } from '$lib';

	let { data }: PageProps = $props();
	const dataObj = data || {};
	let {
		fullName,
		username,
		bio,
		interests = [],
		profileUrl,
		backgroundUrl,
		isSelf,
		offers = [],
		needs = [],
		timeBank,
		userScore
	} = $state(dataObj);
	let editModalOpen = $state(false);

	const badgeColors = ['text-success', 'text-warning', 'text-error'];
	const bgColor = (i: number) =>
		i > 70 ? badgeColors[0] : i > 49 ? badgeColors[1] : badgeColors[2];

	let activeTab = $state('offers');
	let backgroundLoaded = $state(false);
	let avatarBlobUrl = $state<string | null>(null);
	let backgroundBlobUrl = $state<string | null>(null);

	async function uploadAsset(
		file: File | undefined,
		type: 'profile' | 'bg',
		reload: () => Promise<void>
	) {
		if (!file) return;

		const formData = new FormData();
		formData.append('file', file);

		const uploadResponse = await fetch(`${SERVER_URL}/assets/${type}/${$page.params.id}`, {
			method: 'POST',
			headers: {
				Authorization: `Bearer ${$auth.token}`
			},
			body: formData
		});

		if (!uploadResponse.ok) {
			throw new Error(`Failed to upload ${type}`);
		}

		await reload();
	}

	async function handleProfileUpdate(
		event: CustomEvent<{ bio: string; interests: string[]; avatar?: File; background?: File }>
	) {
		const {
			bio: newBio,
			interests: newInterests,
			avatar: newAvatar,
			background: newBackground
		} = event.detail;

		try {
			// Prepare update data - only send changed fields
			const updateData: { bio?: string; interests?: string[] } = {};
			if (newBio !== bio) updateData.bio = newBio;
			if (JSON.stringify(newInterests) !== JSON.stringify(interests))
				updateData.interests = newInterests;

			// If there are changes to bio or interests, send PUT request
			if (Object.keys(updateData).length > 0) {
				const response = await fetch(`${SERVER_URL}/user`, {
					method: 'PUT',
					headers: {
						'Content-Type': 'application/json',
						Authorization: `Bearer ${$auth.token}`
					},
					body: JSON.stringify(updateData)
				});

				if (!response.ok) {
					throw new Error('Failed to update profile');
				}

				const updatedData = await response.json();
				// Update local state with server response
				bio = updatedData.biography || updatedData.bio || bio;
				interests = updatedData.interests || interests;
			}

			// Upload assets if changed
			await uploadAsset(newAvatar, 'profile', loadAvatar);
			await uploadAsset(newBackground, 'bg', loadBackground);

			// Success
			toast.success('Profile updated successfully!');
		} catch (error) {
			console.error('Failed to update profile:', error);
			toast.error('Failed to update profile. Please try again.');
		}
	}

	async function loadAvatar() {
		try {
			const response = await fetch(`${SERVER_URL}/assets/profile/${$page.params.id}`, {
				headers: {
					Authorization: `Bearer ${$auth.token}`
				}
			});

			if (response.ok) {
				const blob = await response.blob();
				avatarBlobUrl = URL.createObjectURL(blob);
			}
		} catch (error) {
			console.error('Failed to load avatar:', error);
		}
	}

	async function loadBackground() {
		try {
			const response = await fetch(`${SERVER_URL}/assets/bg/${$page.params.id}`, {
				headers: {
					Authorization: `Bearer ${$auth.token}`
				}
			});

			if (response.ok) {
				const blob = await response.blob();
				backgroundBlobUrl = URL.createObjectURL(blob);
				backgroundLoaded = true;
			} else {
				backgroundLoaded = false;
			}
		} catch (error) {
			console.error('Failed to load background:', error);
			backgroundLoaded = false;
		}
	}

	// Load avatar and background when component mounts
	$effect(() => {
		if ($auth.token && $page.params.id) {
			loadAvatar();
			loadBackground();
		}

		// Cleanup blob URLs on unmount
		return () => {
			if (avatarBlobUrl) {
				URL.revokeObjectURL(avatarBlobUrl);
			}
			if (backgroundBlobUrl) {
				URL.revokeObjectURL(backgroundBlobUrl);
			}
		};
	});
</script>

<div class="min-h-screen bg-base-200">
	<!-- Hero Section -->
	<div class="relative h-64 md:h-80 lg:h-96">
		<div class="absolute inset-0">
			<img
				src={backgroundBlobUrl || backgroundUrl}
				alt="background"
				class="h-full w-full object-cover"
			/>
			<div class="absolute inset-0 bg-black/40"></div>
		</div>
		<div class="relative flex h-full items-end p-6 text-white">
			<div class="container mx-auto flex items-end justify-between">
				<div class="flex flex-col items-center gap-6 md:flex-row md:items-start">
					<!-- Avatar -->
					<div class="flex flex-col items-center">
						<div class="avatar">
							<div class="w-32 rounded-full ring ring-primary ring-offset-2 ring-offset-base-100">
								<img src={avatarBlobUrl || profileUrl} alt={`${fullName} avatar`} />
							</div>
						</div>
					</div>

					<!-- User Info -->
					<div class="flex-1 text-center md:text-left">
						<div class="flex items-center justify-center gap-2 md:justify-start">
							<h1 class="text-3xl font-bold">
								{fullName}
							</h1>
							{#if isSelf}
								<span class="badge badge-primary">You</span>
							{/if}
							<div
								class="tooltip avatar tooltip-bottom avatar-placeholder h-12 w-12"
								data-tip={`User behavior score: ${userScore}%`}
							>
								<div class={`w-12 rounded-full ${bgColor(userScore)}`}>
									{#if userScore > 70}
										<Smile class="h-6 w-6" />
									{:else if userScore > 49}
										<Meh class="h-6 w-6" />
									{:else if userScore > 20}
										<Frown class="h-6 w-6" />
									{:else}
										<Annoyed class="h-6 w-6" />
									{/if}
								</div>
							</div>
						</div>
						<p class="text-sm italic opacity-70">
							@{username}
						</p>
						<p class="mt-2 text-lg opacity-80">
							{bio}
						</p>
						<div class="mt-4 flex items-center gap-4">
							<div class="flex flex-wrap gap-2">
								{#each interests as interest (interest)}
									<span class="badge badge-outline badge-lg badge-primary">{interest}</span>
								{/each}
							</div>
							<div class="flex items-center gap-2">
								<Hourglass class="h-4 w-4" />
								<span class="text-sm font-medium">{timeBank} Hours</span>
							</div>
						</div>
					</div>
				</div>
				{#if isSelf}
					<div class="mt-4">
						<button class="btn btn-primary" onclick={() => (editModalOpen = true)}>
							<Edit class="mr-2 h-4 w-4" />
							Edit Profile
						</button>
					</div>
				{/if}
			</div>
		</div>
	</div>

	<!-- Tabs for Offers/Needs -->
	<div class="container mx-auto max-w-6xl px-4 py-8">
		<div class="card bg-base-100 shadow-xl">
			<div class="card-body">
				<div class="tabs-bordered tabs">
					<button
						class="tab-active tab"
						class:tab-active={activeTab === 'offers'}
						onclick={() => (activeTab = 'offers')}
					>
						<HeartHandshake class="h-5 w-5" />
						Offers ({offers.length})
					</button>
					<button
						class="tab"
						class:tab-active={activeTab === 'needs'}
						onclick={() => (activeTab = 'needs')}
					>
						<HandHelping class="h-5 w-5" />
						Needs ({needs.length})
					</button>
				</div>

				<div class="mt-6">
					{#if activeTab === 'offers'}
						<div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
							{#if offers.length}
								{#each offers as card (card.id)}
									<SearchResultCard
										title={card.title}
										description={card.description}
										tags={card.tags}
										location={card.location}
										participantCount={card.participantCount}
										type={card.type}
										eventId={card.id}
										ownerProfileUrl={profileUrl}
									/>
								{/each}
							{:else}
								<div class="col-span-full py-12 text-center">
									<HeartHandshake class="mx-auto h-16 w-16 text-base-content/30" />
									<p class="mt-4 text-lg text-base-content/60">No offers yet</p>
								</div>
							{/if}
						</div>
					{:else if activeTab === 'needs'}
						<div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
							{#if needs.length}
								{#each needs as card (card.id)}
									<SearchResultCard
										title={card.title}
										description={card.description}
										tags={card.tags}
										location={card.location}
										participantCount={card.participantCount}
										type={card.type}
										eventId={card.id}
										ownerProfileUrl={profileUrl}
									/>
								{/each}
							{:else}
								<div class="col-span-full py-12 text-center">
									<HandHelping class="mx-auto h-16 w-16 text-base-content/30" />
									<p class="mt-4 text-lg text-base-content/60">No needs yet</p>
								</div>
							{/if}
						</div>
					{/if}
				</div>
			</div>
		</div>
	</div>
</div>

<EditProfileModal
	bind:open={editModalOpen}
	currentBio={bio}
	currentInterests={interests}
	currentAvatar={avatarBlobUrl || profileUrl}
	currentBackground={backgroundBlobUrl || backgroundUrl}
	on:update={handleProfileUpdate}
/>
