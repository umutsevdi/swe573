<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import {
		MapPin,
		Calendar,
		Clock,
		Users,
		Tag,
		X,
		Plus,
		HeartHandshake,
		HandHelping,
		Upload
	} from '@lucide/svelte';
	import LocationPicker from './LocationPicker.svelte';

	const dispatch = createEventDispatcher<{ submit: any; update: any }>();

	let {
		open = $bindable(false),
		editData,
		mode = 'create'
	} = $props<{
		open: boolean;
		editData?: any;
		mode?: 'create' | 'edit';
	}>();

	let modal: HTMLDialogElement;
	let type = $state<'Offer' | 'Need'>('Offer');
	let title = $state('');
	let description = $state('');
	let backgroundImage: File | null = $state(null);
	let backgroundImagePreview = $state<string | null>(null);
	let location = $state<{ lat: number; lng: number; name: string } | null>(null);
	let userLat = $state<number>(41.0082); // Default to Istanbul
	let userLng = $state<number>(28.9784);
	let targetDate = $state('');
	let duration = $state('');
	let capacity = $state(1);
	let tagInput = $state('');
	let tags = $state<string[]>([]);

	function openModal() {
		modal?.showModal();
		open = true;
	}

	function closeModal() {
		modal?.close();
		open = false;
		resetForm();
	}

	// Sync open prop with modal
	$effect(() => {
		if (open && modal) {
			modal.showModal();
		}
	});

	function resetForm() {
		type = 'Offer';
		title = '';
		description = '';
		backgroundImage = null;
		backgroundImagePreview = null;
		location = null;
		targetDate = '';
		duration = '';
		capacity = 1;
		tagInput = '';
		tags = [];
	}

	function handleImageUpload(event: Event) {
		const target = event.target as HTMLInputElement;
		const file = target.files?.[0];
		if (file) {
			backgroundImage = file;
			const reader = new FileReader();
			reader.onload = (e) => {
				backgroundImagePreview = e.target?.result as string;
			};
			reader.readAsDataURL(file);
		}
	}

	function addTag() {
		const trimmedTag = tagInput.trim().toLowerCase();
		if (trimmedTag && !tags.includes(trimmedTag)) {
			tags = [...tags, trimmedTag];
			tagInput = '';
		}
	}

	function removeTag(tagToRemove: string) {
		tags = tags.filter((tag) => tag !== tagToRemove);
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			event.preventDefault();
			addTag();
		}
	}

	function submitPost() {
		if (!location) {
			alert('Please select a location on the map');
			return;
		}

		const postData = {
			type,
			title,
			description,
			backgroundImage,
			location: {
				name: location.name,
				coordinates: { lat: location.lat, lng: location.lng }
			},
			targetDate,
			duration,
			capacity,
			tags
		};

		dispatch(
			mode === 'edit' ? 'update' : 'submit',
			mode === 'edit' ? { id: editData.id, ...postData } : postData
		);
		closeModal();
	}

	// Prefill form for edit mode
	$effect(() => {
		if (mode === 'edit' && editData) {
			type = editData.type || 'Offer';
			title = editData.title || '';
			description = editData.description || '';
			tags = editData.tags || [];
			backgroundImagePreview = editData.backgroundImage || null;
			if (editData.coordinates && editData.location) {
				location = {
					lat: editData.coordinates.lat,
					lng: editData.coordinates.lng,
					name: editData.location
				};
			}
			capacity = editData.maxParticipants || 1;
			// Note: targetDate and duration not in EventData, leave empty
		}
	});

	// Get user's current location when component mounts
	$effect(() => {
		if (typeof navigator !== 'undefined' && navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(
				(position) => {
					userLat = position.coords.latitude;
					userLng = position.coords.longitude;
				},
				(error) => {
					console.log('Geolocation error:', error);
					// Keep default Istanbul coordinates
				},
				{
					enableHighAccuracy: true,
					timeout: 10000,
					maximumAge: 300000 // 5 minutes
				}
			);
		}
		// If geolocation not supported, keep default Istanbul coordinates
	});

	// Export function for parent component access
	export { openModal };
</script>

<dialog bind:this={modal} class="modal" onclose={() => (open = false)}>
	<div class="modal-box max-w-2xl">
		<h3 class="mb-4 flex items-center gap-2 text-lg font-bold">
			{mode === 'edit' ? 'Edit' : 'Create New'}
			<div class="join">
				<button
					class="btn join-item btn-sm {type === 'Offer' ? 'btn-active' : ''}"
					onclick={() => (type = 'Offer')}
				>
					<HeartHandshake class="h-4 w-4" />
					Offer
				</button>
				<button
					class="btn join-item btn-sm {type === 'Need' ? 'btn-active' : ''}"
					onclick={() => (type = 'Need')}
				>
					<HandHelping class="h-4 w-4" />
					Need
				</button>
			</div>
		</h3>

		<form onsubmit={submitPost} class="space-y-4">
			<!-- Title and Description Row -->
			<div class="grid grid-cols-1 gap-4">
				<!-- Title -->
				<div class="form-control">
					<label class="label" for="title-input">
						<span class="label-text">Title</span>
					</label>
					<input
						id="title-input"
						type="text"
						placeholder="Enter a descriptive title"
						class="input-bordered input w-full"
						bind:value={title}
						required
					/>
				</div>

				<!-- Description -->
				<div class="form-control">
					<label class="label" for="description-input">
						<span class="label-text">Description</span>
					</label>
					<textarea
						id="description-input"
						placeholder="Describe your offer or need in detail"
						class="textarea-bordered textarea h-24 w-full"
						bind:value={description}
						required
					></textarea>
				</div>
			</div>

			<!-- Background Image -->
			<div class="form-control">
				<label class="label">
					<span class="label-text">Background Image</span>
				</label>
				<div class="flex items-center gap-4">
					<div class="avatar">
						<div class="w-16 rounded-full">
							{#if backgroundImagePreview}
								<img src={backgroundImagePreview} alt="Preview" />
							{:else}
								<div class="bg-neutral text-neutral-content">
									<Upload class="h-8 w-8" />
								</div>
							{/if}
						</div>
					</div>
					<label class="btn btn-outline btn-sm">
						<Upload class="mr-2 h-4 w-4" />
						Upload Image
						<input type="file" accept="image/*" class="hidden" onchange={handleImageUpload} />
					</label>
				</div>
			</div>

			<!-- Location -->
			<div class="form-control">
				<label class="label">
					<span class="label-text flex items-center gap-2">
						<MapPin class="h-4 w-4" />
						Location
					</span>
				</label>
				<div class="rounded-lg border border-base-300 p-2">
					<LocationPicker
						initialLat={userLat}
						initialLng={userLng}
						height="h-48"
						onLocationChange={(newLocation) => {
							location = newLocation;
						}}
					/>
				</div>
				{#if location}
					<p class="mt-2 text-sm text-base-content/70">
						Selected location: <strong>{location.name}</strong>
					</p>
				{/if}
			</div>

			<!-- Date and Duration Row -->
			<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
				<!-- Target Date -->
				<div class="form-control">
					<label class="label" for="date-input">
						<span class="label-text flex items-center gap-2">
							<Calendar class="h-4 w-4" />
							Target Date
						</span>
					</label>
					<input
						id="date-input"
						type="date"
						class="input-bordered input"
						bind:value={targetDate}
						required
					/>
				</div>

				<!-- Duration -->
				<div class="form-control">
					<label class="label" for="duration-select">
						<span class="label-text flex items-center gap-2">
							<Clock class="h-4 w-4" />
							Duration
						</span>
					</label>
					<select
						id="duration-select"
						class="select-bordered select"
						bind:value={duration}
						required
					>
						<option value="" disabled selected>Select duration</option>
						<option value="1 hour">1 hour</option>
						<option value="2 hours">2 hours</option>
						<option value="3 hours">3 hours</option>
						<option value="4 hours">4 hours</option>
						<option value="1 day">1 day</option>
						<option value="2 days">2 days</option>
						<option value="3 days">3 days</option>
						<option value="1 week">1 week</option>
						<option value="2 weeks">2 weeks</option>
						<option value="1 month">1 month</option>
						<option value="Ongoing">Ongoing</option>
					</select>
				</div>
			</div>

			<!-- Capacity -->
			<div class="form-control">
				<label class="label" for="capacity-input">
					<span class="label-text flex items-center gap-2">
						<Users class="h-4 w-4" />
						Capacity
					</span>
				</label>
				<input
					id="capacity-input"
					type="number"
					min="1"
					max="100"
					class="input-bordered input"
					bind:value={capacity}
					required
				/>
			</div>

			<!-- Tags -->
			<div class="form-control">
				<label class="label" for="tag-input">
					<span class="label-text flex items-center gap-2">
						<Tag class="h-4 w-4" />
						Tags
					</span>
				</label>
				<div class="mb-2 flex gap-2">
					<input
						id="tag-input"
						type="text"
						placeholder="Add a tag (press Enter)"
						class="input-bordered input flex-1"
						bind:value={tagInput}
						onkeydown={handleKeydown}
					/>
					<button type="button" class="btn btn-outline" onclick={addTag}>
						<Plus class="h-4 w-4" />
					</button>
				</div>
				{#if tags.length > 0}
					<div class="flex flex-wrap gap-2">
						{#each tags as tag (tag)}
							<div class="badge gap-2 badge-primary">
								{tag}
								<button type="button" class="btn btn-ghost btn-xs" onclick={() => removeTag(tag)}>
									<X class="h-3 w-3" />
								</button>
							</div>
						{/each}
					</div>
				{/if}
			</div>

			<!-- Action Buttons -->
			<div class="modal-action">
				<button type="button" class="btn" onclick={closeModal}>Cancel</button>
				<button type="submit" class="btn btn-primary">
					{mode === 'edit' ? 'Save' : 'Post'}
					{type}
				</button>
			</div>
		</form>
	</div>
	<form method="dialog" class="modal-backdrop">
		<button onclick={closeModal}>close</button>
	</form>
</dialog>
