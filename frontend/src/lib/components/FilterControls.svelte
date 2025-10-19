<script lang="ts">
	import {
		Search,
		SlidersHorizontal,
		Calendar,
		MapPin,
		Plus,
		X,
		HeartHandshake,
		HandHelping
	} from '@lucide/svelte';

	let {
		searchQuery: initialSearchQuery = $bindable(''),
		selectedTags: initialSelectedTags = $bindable([]),
		dateRange: initialDateRange = $bindable({ start: '', end: '' }),
		proximityKm: initialProximityKm = $bindable(50),
		type: initialType = $bindable('all'),
		onSearch
	} = $props();

	// Local state for the form
	let searchQuery = $state(initialSearchQuery);
	let selectedTags = $state([...initialSelectedTags]);
	let dateRange = $state({ ...initialDateRange });
	let proximityKm = $state(initialProximityKm);
	let type = $state(initialType);

	// Tag input state
	let tagInput = $state('');

	// Update local state when props change
	$effect(() => {
		if (searchQuery !== initialSearchQuery) searchQuery = initialSearchQuery;
		if (JSON.stringify(selectedTags) !== JSON.stringify(initialSelectedTags))
			selectedTags = [...initialSelectedTags];
		if (JSON.stringify(dateRange) !== JSON.stringify(initialDateRange))
			dateRange = { ...initialDateRange };
		if (proximityKm !== initialProximityKm) proximityKm = initialProximityKm;
		if (type !== initialType) type = initialType;
	});

	function addTag() {
		const trimmedTag = tagInput.trim().toLowerCase();
		if (trimmedTag && !selectedTags.includes(trimmedTag)) {
			selectedTags.push(trimmedTag);
			selectedTags = selectedTags;
			tagInput = '';
		}
	}

	function removeTag(tagToRemove: string) {
		selectedTags = selectedTags.filter((tag) => tag !== tagToRemove);
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			event.preventDefault();
			addTag();
		}
	}

	function performSearch() {
		onSearch({ searchQuery, selectedTags, dateRange, proximityKm, type });
	}

	function clearFilters() {
		searchQuery = '';
		selectedTags = [];
		dateRange = { start: '', end: '' };
		proximityKm = 50;
		type = 'all';
		tagInput = '';
	}
</script>

<div class="card bg-base-100 shadow-lg">
	<div class="card-body">
		<div class="mb-4 flex items-center gap-2">
			<SlidersHorizontal class="h-5 w-5" />
			<h3 class="card-title">Filters</h3>
			<button class="btn ml-auto btn-ghost btn-sm" onclick={clearFilters}> Clear All </button>
		</div>

		<!-- First Row: Search and Tags -->
		<div class="mb-6 grid grid-cols-1 gap-4 md:grid-cols-2">
			<!-- Search Input -->
			<div class="form-control">
				<label class="label">
					<span class="label-text flex items-center gap-1">
						<Search class="h-4 w-4" />
						Search
					</span>
				</label>
				<input
					type="text"
					placeholder="Search offers and needs..."
					class="input-bordered input input-sm"
					value={searchQuery}
					oninput={(e) => (searchQuery = (e.target as HTMLInputElement).value)}
				/>
			</div>

			<!-- Type Filter -->
			<div class="form-control">
				<label class="label" for="type-group">
					<span class="label-text">Type</span>
				</label>
				<div id="type-group" class="join" role="group" aria-label="Filter by type">
					<button
						class="btn join-item btn-sm {type === 'all' ? 'btn-active' : ''}"
						onclick={() => (type = 'all')}
					>
						All
					</button>
					<button
						class="btn join-item btn-sm {type === 'Offer' ? 'btn-active' : ''}"
						onclick={() => (type = 'Offer')}
					>
						<HeartHandshake class="h-4 w-4" />
						Offers
					</button>
					<button
						class="btn join-item btn-sm {type === 'Need' ? 'btn-active' : ''}"
						onclick={() => (type = 'Need')}
					>
						<HandHelping class="h-4 w-4" />
						Needs
					</button>
				</div>
			</div>

			<!-- Tags Input -->
			<div class="form-control">
				<label class="label" for="tag-input">
					<span class="label-text">Tags</span>
				</label>
				<div class="flex gap-2">
					<input
						id="tag-input"
						type="text"
						placeholder="Add a tag (press Enter)"
						class="input-bordered input input-sm flex-1"
						value={tagInput}
						oninput={(e) => (tagInput = (e.target as HTMLInputElement).value)}
						onkeydown={handleKeydown}
					/>
					<button type="button" class="btn btn-outline btn-sm" onclick={addTag}>
						<Plus class="h-4 w-4" />
					</button>
				</div>
			</div>
		</div>

		<!-- Second Row: Date Range and Proximity -->
		<div class="mb-6 grid grid-cols-1 gap-4 md:grid-cols-2">
			<!-- Date Range -->
			<div class="form-control">
				<label class="label">
					<span class="label-text flex items-center gap-1">
						<Calendar class="h-4 w-4" />
						Date Range
					</span>
				</label>
				<div class="flex gap-2">
					<input
						type="date"
						class="input-bordered input input-sm flex-1"
						value={dateRange.start}
						oninput={(e) => (dateRange.start = (e.target as HTMLInputElement).value)}
					/>
					<input
						type="date"
						class="input-bordered input input-sm flex-1"
						value={dateRange.end}
						oninput={(e) => (dateRange.end = (e.target as HTMLInputElement).value)}
					/>
				</div>
			</div>

			<!-- Proximity Slider -->
			<div class="form-control">
				<label class="label">
					<span class="label-text flex items-center gap-1">
						<MapPin class="h-4 w-4" />
						Proximity: {proximityKm}km
					</span>
				</label>
				<input
					type="range"
					min="1"
					max="100"
					class="range range-sm"
					value={proximityKm}
					oninput={(e) => (proximityKm = parseInt((e.target as HTMLInputElement).value))}
				/>
				<div class="flex justify-between px-2 text-xs">
					<span>1km</span>
					<span>100km</span>
				</div>
			</div>
		</div>

		<!-- Selected Tags Display -->
		{#if selectedTags.length > 0}
			<div class="mt-4 flex flex-wrap gap-2">
				<span class="text-sm font-medium">Selected tags:</span>
				{#each selectedTags as tag (tag)}
					<div class="badge gap-2 badge-primary">
						{tag}
						<button type="button" class="btn btn-ghost btn-xs" onclick={() => removeTag(tag)}>
							<X class="h-3 w-3" />
						</button>
					</div>
				{/each}
			</div>
		{/if}

		<!-- Search Button -->
		<div class="flex justify-center">
			<button class="btn btn-lg btn-primary" onclick={performSearch}>
				<Search class="h-5 w-5" />
			</button>
		</div>
	</div>
</div>
