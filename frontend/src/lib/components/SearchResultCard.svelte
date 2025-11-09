<script lang="ts">
	import { HandHelping, HeartHandshake, MapPin, Users } from '@lucide/svelte';
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';

	export let title: string;
	export let description: string;
	export let tags: string[] = [];
	export let location: string;
	export let participantCount: number;
	export let ownerProfileUrl: string = '';
	export let type: 'Offer' | 'Need' = 'Offer';
	export let eventId: string;

	let badgeColor = (cond: boolean) => (cond ? 'badge-primary' : 'badge-accent');

	function handleCardClick() {
		goto(resolve(`/event/${eventId}`));
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter' || event.key === ' ') {
			event.preventDefault();
			handleCardClick();
		}
	}
</script>

<div
	class="card cursor-pointer bg-base-100 shadow-md hover:shadow-lg"
	onclick={handleCardClick}
	onkeydown={handleKeydown}
	role="button"
	tabindex="0"
	aria-label="View event details for {title}"
>
	<div class="card-body p-4">
		<!-- Title with type badge -->
		<div class="flex items-center">
			<h2 class="card-title flex-1">{title}</h2>
			<div class={`badge font-semibold uppercase ${badgeColor(type === 'Offer')} shrink-0 `}>
				{#if type === 'Offer'}
					<HeartHandshake class="h-3 w-3" />
				{:else}
					<HandHelping class="h-3 w-3" />
				{/if}
				{type}
			</div>
		</div>

		<!-- Description row with participants/owner on right -->
		<div class="flex items-start gap-3">
			<div class="flex-1">
				<p class="line-clamp-2 text-sm whitespace-pre-wrap text-base-content/70">{description}</p>
			</div>
			<!-- Header with location -->
			<div class="mb-3 flex items-center justify-between">
				<div class="flex items-center gap-1 text-sm opacity-70">
					<MapPin class="h-4 w-4" />
					<span>{location}</span>
				</div>
			</div>
		</div>

		<div class="flex">
			<!-- Tags -->
			{#if tags.length > 0}
				<div class="flex flex-1 flex-wrap gap-1">
					{#each tags as tag (tag)}
						<span class="badge badge-outline badge-secondary">{tag}</span>
					{/each}
				</div>
			{/if}
			<div class="flex shrink-0 items-center gap-2">
				<div class="flex items-center gap-1">
					<Users class="h-4 w-4 opacity-60" />
					<span class="text-sm opacity-70">{participantCount}</span>
				</div>
				{#if ownerProfileUrl}
					<div class="avatar">
						<div class="w-6 rounded-full">
							<img src={ownerProfileUrl} alt="Owner" />
						</div>
					</div>
				{/if}
			</div>
		</div>
	</div>
</div>

<style>
	.card {
		transition: all 0.2s ease-in-out;
	}

	.card:hover {
		transform: translateY(-2px);
	}

	.line-clamp-2 {
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
		line-clamp: 2;
	}
</style>
