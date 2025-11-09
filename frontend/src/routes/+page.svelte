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
	let proximity = $state(50);
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
		if (proximity > 0 && userLat && userLng) {
			params.set('user_lat', userLat.toString());
			params.set('user_lng', userLng.toString());
			params.set('proximity', proximity.toString());
		}
		goto(resolve(`?${params.toString()}`));
	}
</script>

<div class="hero min-h-screen bg-base-200">
	<div class="hero-content w-full max-w-4xl flex-col">
		<div class="w-full">
			<div class="mb-8 flex items-center justify-between">
				<h1 class="text-4xl font-bold">Discover</h1>
			</div>
			<!-- Filter section -->
			<div class="mb-4 bg-base-100 p-4">
				<h2 class="mb-4 flex items-center gap-2 text-lg font-bold"><Funnel /> Filters</h2>
				<form onsubmit={handleSubmit}>
					<div class="flex flex-wrap items-center gap-4">
						<!-- Search input -->
						<div class="form-control flex-1">
							<input
								type="text"
								placeholder="Search events..."
								class="input-bordered input"
								bind:value={q}
							/>
						</div>

						<!-- Tags -->
						<div class="form-control flex-1">
							<div class="flex gap-2">
								<input
									type="text"
									placeholder="Add tag..."
									class="input-bordered input"
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
						<!-- Start and End dates in one row -->
						<div class="flex gap-4">
							<div class="form-control">
								<label class="label">
									Start Date
									<input type="date" class="input-bordered input" bind:value={start} />
								</label>
							</div>

							<div class="form-control">
								<label class="label">
									End Date
									<input type="date" class="input-bordered input" bind:value={end} />
								</label>
							</div>
						</div>
						<!-- Proximity -->
						<div class="form-control">
							<div class="gap-4 lg:flex">
								Proximity
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
						<!-- Search button -->
						<button type="submit" class="btn btn-primary"><Search /></button>
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
						</div>
					</div>
				</form>
			</div>
			<div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
				<!-- Left: Map -->
				<div>
					<MapOverview events={data.events} {userLat} {userLng} {proximity} />
				</div>

				<!-- Right: List of cards -->
				<div class="space-y-4">
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
</div>
