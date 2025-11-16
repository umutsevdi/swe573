<script lang="ts">
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { page } from '$app/stores';
	import { Funnel, Search } from '@lucide/svelte';
	import MapOverview from '$lib/components/MapOverview.svelte';
	import SearchResultCard from '$lib/components/SearchResultCard.svelte';

	let { data } = $props();

	let q = $state('');
	let tags = $state<string[]>([]);
	let tagInput = $state('');
	let start = $state('');
	let end = $state('');
	let proximity = $state(0);
	let type = $state<'OFFER' | 'NEED' | ''>('');
	let userLat = $state<number | null>(null);
	let userLng = $state<number | null>(null);

	// Get user location
	$effect(() => {
		if (typeof navigator !== 'undefined' && navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(
				(position) => {
					userLat = position.coords.latitude;
					userLng = position.coords.longitude;
				},
				(error) => {
					console.log('Geolocation error:', error);
				}
			);
		}
	});

	// Prefill from URL
	$effect(() => {
		const params = $page.url.searchParams;
		q = params.get('q') || '';
		const tagsParam = params.get('tags');
		if (tagsParam) {
			tags = tagsParam.split(',').filter(Boolean);
		}
		start = params.get('start') || '';
		end = params.get('end') || '';
		const prox = params.get('proximity');
		if (prox) proximity = parseInt(prox) || 0;
		const typeParam = params.get('type');
		if (typeParam && (typeParam === 'OFFER' || typeParam === 'NEED')) {
			type = typeParam as 'OFFER' | 'NEED';
		} else {
			type = '';
		}
	});

	function addTag() {
		const trimmed = tagInput.trim().toLowerCase();
		if (trimmed && !tags.includes(trimmed)) {
			tags = [...tags, trimmed];
			tagInput = '';
		}
	}

	function removeTag(tag: string) {
		tags = tags.filter((t) => t !== tag);
	}

	function handleSubmit(event: Event) {
		event.preventDefault();
		const params = new URLSearchParams();
		if (q.trim()) params.set('q', q.trim());
		if (tags.length) params.set('tags', tags.join(','));
		if (start) params.set('start', start);
		if (end) params.set('end', end);
		if (type) params.set('type', type);
		if (proximity > 0 && userLat && userLng) {
			params.set('user_lat', userLat.toString());
			params.set('user_lng', userLng.toString());
			params.set('proximity', proximity.toString());
		}
		goto(`?${params.toString()}`);
	}
</script>

<div class="hero min-h-screen bg-base-200">
	<div class="hero-content w-full max-w-4xl flex-col">
		<div class="w-full">
			<!-- Map and Filters section -->
			<div class="mb-4 bg-base-100 p-4">
				<div class="mb-4">
					<MapOverview events={data.events} {userLat} {userLng} {proximity} />
				</div>
				<form onsubmit={handleSubmit} class="space-y-4">
					<!-- Search Row -->
					<div class="flex flex-col gap-4 lg:flex-row lg:items-center">
						<div class="flex flex-1 items-center gap-2">
							<span class="label-text">Search</span>
							<input
								type="text"
								placeholder="Search events..."
								class="input-bordered input flex-1"
								bind:value={q}
							/>
						</div>
						<div class="flex flex-1 items-center gap-2">
							<span class="label-text">Add Tag</span>
							<div class="flex flex-1 gap-2">
								<input
									type="text"
									placeholder="Enter tag..."
									class="input-bordered input flex-1"
									bind:value={tagInput}
									onkeydown={(e) => {
										if (e.key === 'Enter') {
											e.preventDefault();
											addTag();
										}
									}}
								/>
								<button type="button" class="btn btn-outline" onclick={addTag}>+</button>
							</div>
						</div>
						<div class="flex items-center gap-2">
							<span class="label-text">Type</span>
							<div class="join">
								<input
									class="btn join-item {type === 'OFFER' ? 'btn-success' : 'btn-outline'}"
									type="radio"
									value="OFFER"
									bind:group={type}
									aria-label="OFFER"
									onclick={() => {
										if (type === 'OFFER') type = '';
									}}
								/>
								<input
									class="btn join-item {type === 'NEED' ? 'btn-accent' : 'btn-outline'}"
									type="radio"
									value="NEED"
									bind:group={type}
									aria-label="NEED"
									onclick={() => {
										if (type === 'NEED') type = '';
									}}
								/>
							</div>
						</div>
					</div>

					<!-- Filters Row -->
					<div class="flex flex-col gap-4 lg:flex-row lg:items-center">
						<div class="flex flex-1 items-center gap-2">
							<span class="label-text">Date Range</span>
							<div class="flex flex-1 gap-2">
								<input type="date" class="input-bordered input" bind:value={start} />
								<span class="self-center">to</span>
								<input type="date" class="input-bordered input" bind:value={end} />
							</div>
						</div>
						<div class="flex flex-1 items-center gap-2">
							<span class="label-text">Proximity</span>
							<div class="flex flex-1 items-center gap-4">
								<input
									type="range"
									min="0"
									max="1000"
									class="range flex-1"
									bind:value={proximity}
								/>
								<span class="label-text">{proximity != 0 ? `${proximity}km` : 'No Filter'}</span>
							</div>
						</div>
						<button type="submit" class="btn btn-primary">
							<Search class="h-4 w-4" />
							Search
						</button>
					</div>

					<!-- Active filters display -->
					<div class="mt-4">
						<div class="flex flex-wrap gap-2">
							{#if q}
								<span class="badge gap-2 badge-primary">
									Search: {q}
									<button type="button" class="btn btn-ghost btn-xs" onclick={() => (q = '')}
										>×</button
									>
								</span>
							{/if}
							{#each tags as tag (tag)}
								<span class="badge gap-2 badge-outline badge-accent">
									Tag: {tag}
									<button type="button" class="btn btn-ghost btn-xs" onclick={() => removeTag(tag)}
										>×</button
									>
								</span>
							{/each}
							{#if start}
								<span class="badge gap-2 badge-accent">
									Start: {start}
									<button type="button" class="btn btn-ghost btn-xs" onclick={() => (start = '')}
										>×</button
									>
								</span>
							{/if}
							{#if end}
								<span class="badge gap-2 badge-accent">
									End: {end}
									<button type="button" class="btn btn-ghost btn-xs" onclick={() => (end = '')}
										>×</button
									>
								</span>
							{/if}
							{#if proximity > 0}
								<span class="badge gap-2 badge-info">
									Proximity: {proximity} km
									<button type="button" class="btn btn-ghost btn-xs" onclick={() => (proximity = 0)}
										>×</button
									>
								</span>
							{/if}
							{#if type}
								<span class="badge gap-2 badge-secondary">
									Type: {type}
									<button type="button" class="btn btn-ghost btn-xs" onclick={() => (type = '')}
										>×</button
									>
								</span>
							{/if}
						</div>
					</div>
				</form>
			</div>
			<!-- List of cards -->
			<div class="h-[75vh] space-y-4 overflow-y-auto">
				{#each data.events as event (event.id)}
					<SearchResultCard
						title={event.title}
						description={event.description}
						tags={event.tags}
						location={event.location}
						participantCount={event.participantCount}
						ownerId={event.ownerId}
						type={event.type}
						eventId={event.id}
					/>
				{/each}
				{#if data.events.length === 0 && (q || tags.length > 0 || start || end || proximity > 0)}
					<div class="alert alert-info">No events found matching your criteria.</div>
				{/if}
			</div>
		</div>
	</div>
</div>
