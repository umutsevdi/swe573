<script lang="ts">
	import {
		Annoyed,
		Frown,
		HandHelping,
		HeartHandshake,
		Hourglass,
		Meh,
		Smile,
		Edit,
		Settings,
		Tag
	} from '@lucide/svelte';
	import type { PageProps } from './$types';
	import SearchResultCard from '$lib/components/SearchResultCard.svelte';
	import EditProfileModal from '$lib/components/EditProfileModal.svelte';
	let { data }: PageProps = $props();
	const { fullName, bio, interests, profileUrl, isSelf, offers, needs, remainingTime, userScore } =
		data;

	let editModalOpen = $state(false);

	const badgeColors = ['text-success', 'text-warning', 'text-error'];
	const bgColor = (i: number) =>
		i > 70 ? badgeColors[0] : i > 49 ? badgeColors[1] : badgeColors[2];

	let activeTab = $state('offers');
</script>

<div class="min-h-screen bg-base-200 py-8">
	<div class="container mx-auto max-w-6xl px-4">
		<!-- Profile Header -->
		<div class="card bg-base-100 shadow-xl">
			<div class="card-body">
				<div class="flex flex-col items-center gap-6 md:flex-row md:items-start">
					<!-- Avatar -->
					<div class="flex flex-col items-center">
						<div class="avatar">
							<div class="w-32 rounded-full ring ring-primary ring-offset-2 ring-offset-base-100">
								<img src={profileUrl} alt={`${fullName} avatar`} />
							</div>
						</div>
					</div>

					<!-- User Info -->
					<div class="flex-1 text-center md:text-left">
						<div class="flex items-center justify-center gap-2 md:justify-start">
							<h1 class="text-3xl font-bold">{fullName}</h1>
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
						<p class="mt-2 text-lg text-base-content/80">{bio}</p>
						{#if isSelf}
							<button class="btn mt-4 btn-primary" onclick={() => (editModalOpen = true)}>
								<Edit class="mr-2 h-4 w-4" />
								Edit Profile
							</button>
						{/if}
					</div>
				</div>

				<!-- Stats -->
				<div class="stats mt-8 w-full stats-vertical shadow lg:stats-horizontal">
					<div class="stat">
						<div class="stat-figure text-info">
							<Tag class="h-8 w-8" />
						</div>
						<div class="stat-title">Interests</div>
						<div class="stat-value text-info">
							<div class="flex flex-wrap gap-2">
								{#each interests as interest (interest)}
									<span class="badge badge-outline badge-lg">{interest}</span>
								{/each}
							</div>
						</div>
						<div class="stat-desc"></div>
					</div>
					<div class="stat">
						<div class="stat-figure text-accent">
							<Hourglass class="h-8 w-8" />
						</div>
						<div class="stat-title">Time Available</div>
						<div class="stat-value text-accent">{remainingTime}</div>
						<div class="stat-desc">Hours per week</div>
					</div>
				</div>
			</div>
		</div>

		<!-- Tabs for Offers/Needs -->
		<div class="mt-8">
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

<EditProfileModal
	bind:open={editModalOpen}
	currentBio={bio}
	currentInterests={interests}
	currentAvatar={profileUrl}
	on:update={(event) => {
		// Handle update, for now just log
		console.log('Profile update:', event.detail);
		// In real app, send to server
	}}
/>
