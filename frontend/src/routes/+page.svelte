<script lang="ts">
	import SearchResultCard from '$lib/components/SearchResultCard.svelte';
	import MapOverview from '$lib/components/MapOverview.svelte';
	import FilterControls from '$lib/components/FilterControls.svelte';
	import { ArrowUpDown, Users, Calendar } from '@lucide/svelte';
	import { goto } from '$app/navigation';
	import { browser } from '$app/environment';
	import '../app.css';

	interface Event {
		id: string;
		title: string;
		description: string;
		tags: string[];
		location: string;
		latitude: number;
		longitude: number;
		type: 'Offer' | 'Need';
		ownerUrl: string;
		ownerProfileUrl: string;
		participantCount: number;
		date: string;
	}

	interface PageData {
		events: Event[];
	}

	let { data }: { data: PageData } = $props();

	let sortBy = $state<'date' | 'participants'>('date');
	let sortOrder = $state<'asc' | 'desc'>('desc');

	// Events are already filtered and sorted by server
	let searchedEvents = data.events;

	// Initialize sort from URL parameters
	$effect(() => {
		if (browser) {
			const url = new URL(window.location.href);
			const searchParams = url.searchParams;

			sortBy = (searchParams.get('sort') as 'date' | 'participants') || 'date';
			sortOrder = (searchParams.get('order') as 'asc' | 'desc') || 'desc';
		}
	});

	function handleSearch(newFilters: {
		searchQuery: string;
		selectedTags: string[];
		dateRange: { start: string; end: string };
		proximityKm: number;
		type: 'all' | 'Offer' | 'Need';
	}) {
		const url = new URL(window.location.href);
		url.searchParams.delete('q');
		url.searchParams.delete('tags');
		url.searchParams.delete('start');
		url.searchParams.delete('end');
		url.searchParams.delete('proximity');
		url.searchParams.delete('type');
		url.searchParams.delete('sort');
		url.searchParams.delete('order');

		if (newFilters.searchQuery) url.searchParams.set('q', newFilters.searchQuery);
		if (newFilters.selectedTags.length)
			url.searchParams.set('tags', newFilters.selectedTags.join(','));
		if (newFilters.dateRange.start) url.searchParams.set('start', newFilters.dateRange.start);
		if (newFilters.dateRange.end) url.searchParams.set('end', newFilters.dateRange.end);
		if (newFilters.proximityKm !== 50)
			url.searchParams.set('proximity', newFilters.proximityKm.toString());
		if (newFilters.type !== 'all') url.searchParams.set('type', newFilters.type);
		if (sortBy !== 'date') url.searchParams.set('sort', sortBy);
		if (sortOrder !== 'desc') url.searchParams.set('order', sortOrder);

		goto(url.toString());
	}

	function toggleSort(field: 'date' | 'participants') {
		if (sortBy === field) {
			sortOrder = sortOrder === 'asc' ? 'desc' : 'asc';
		} else {
			sortBy = field;
			sortOrder = 'desc';
		}

		const url = new URL(window.location.href);
		if (sortBy !== 'date') url.searchParams.set('sort', sortBy);
		else url.searchParams.delete('sort');
		if (sortOrder !== 'desc') url.searchParams.set('order', sortOrder);
		else url.searchParams.delete('order');

		goto(url.toString());
	}
</script>

<div class="min-h-screen bg-base-200">
	<div class="container mx-auto px-4 py-8">
		<!-- Header -->
		<div class="mb-8 text-center">
			<h1 class="mb-4 text-5xl font-bold">Welcome to Hive!</h1>
		</div>

		<!-- Subtitle and Filters Section -->
		<div class="mb-8">
			<p class="mb-6 text-center text-lg text-base-content/70">
				Discover offers and needs in your community
			</p>
			<FilterControls
				searchQuery=""
				selectedTags={[]}
				dateRange={{ start: '', end: '' }}
				proximityKm={50}
				type="all"
				onSearch={handleSearch}
			/>
		</div>

		<!-- Main Content Grid -->
		<div class="grid min-h-[600px] grid-cols-1 gap-8 lg:grid-cols-2">
			<!-- Left Column: Map -->
			<div class="space-y-4">
				<div class="card bg-base-100 shadow-lg">
					<div class="card-body p-4">
						<h2 class="card-title text-lg">Explore on Map</h2>
						<MapOverview events={searchedEvents} />
					</div>
				</div>
			</div>

			<!-- Right Column: Search & Results -->
			<div class="space-y-6">
				<!-- Sort Controls -->
				<div class="flex items-center justify-between">
					<h2 class="text-xl font-bold">
						{searchedEvents.length} Result{searchedEvents.length !== 1 ? 's' : ''}
					</h2>
					<div class="flex gap-2">
						<button
							class="btn gap-2 btn-outline btn-sm"
							onclick={() => toggleSort('date')}
							class:active={sortBy === 'date'}
						>
							<Calendar class="h-4 w-4" />
							Date
							{#if sortBy === 'date'}
								<ArrowUpDown class="h-4 w-4" />
							{/if}
						</button>
						<button
							class="btn gap-2 btn-outline btn-sm"
							onclick={() => toggleSort('participants')}
							class:active={sortBy === 'participants'}
						>
							<Users class="h-4 w-4" />
							Participants
							{#if sortBy === 'participants'}
								<ArrowUpDown class="h-4 w-4" />
							{/if}
						</button>
					</div>
				</div>

				<!-- Results List -->
				<div class="max-h-[500px] space-y-4 overflow-y-auto">
					{#if searchedEvents.length}
						{#each searchedEvents as event (event.id)}
							<SearchResultCard
								title={event.title}
								description={event.description}
								tags={event.tags}
								location={event.location}
								participantCount={event.participantCount}
								ownerProfileUrl={event.ownerProfileUrl}
								type={event.type}
								eventId={event.id}
							/>
						{/each}
					{:else}
						<div class="rounded-lg bg-base-100 py-12 text-center">
							<p class="text-lg text-base-content/60">No events match your filters.</p>
							<p class="mt-2 text-sm text-base-content/40">Try adjusting your search criteria.</p>
						</div>
					{/if}
				</div>
			</div>
		</div>
	</div>
</div>
