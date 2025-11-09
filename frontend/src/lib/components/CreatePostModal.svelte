<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import {
		MapPin,
		Calendar,
		Clock,
		Users,
		Tag,
		HeartHandshake,
		HandHelping,
		Upload
	} from '@lucide/svelte';
	import LocationPicker from './LocationPicker.svelte';
	import TagInput from './TagInput.svelte';
	import BaseModal from './BaseModal.svelte';
	import { createFilePreview } from '$lib/utils/file';

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

	let type = $state<'Offer' | 'Need'>('Offer');
	let title = $state('');
	let description = $state('');

	let backgroundImagePreview = $state<string | null>(null);
	let location = $state<{ lat: number; lng: number; name: string } | null>(null);
	let userLat = $state<number>(41.0082); // Default to Istanbul
	let userLng = $state<number>(28.9784);
	let eventDate = $state('');
	let duration = $state('');
	let capacity = $state(1);
	let tags = $state<string[]>([]);

	function openModal() {
		open = true;
	}

	function closeModal() {
		open = false;
		resetForm();
	}

	function resetForm() {
		type = 'Offer';
		title = '';
		description = '';
		backgroundImagePreview = null;
		location = null;
		eventDate = '';
		duration = '';
		capacity = 1;
		tags = [];
	}

	async function handleImageUpload(event: Event) {
		const target = event.target as HTMLInputElement;
		const file = target.files?.[0];
		if (file) {
			backgroundImagePreview = await createFilePreview(file);
		}
	}

	function convertDurationForEdit(duration: string | number | undefined): string {
		if (!duration) return '';
		const durationStr = typeof duration === 'number' ? `${duration} hour(s)` : duration;
		// Convert from "2 hour(s)" to "2 hours"
		const match = durationStr.match(/^(\d+)\s*hour\(s\)$/);
		if (match) {
			const num = parseInt(match[1]);
			return num === 1 ? '1 hour' : `${num} hours`;
		}
		return durationStr; // fallback
	}

	// Prefill form for edit mode
	$effect(() => {
		if (mode === 'edit' && editData) {
			type = editData.type || 'Offer';
			title = editData.title || '';
			description = editData.description || '';
			tags = editData.tags || [];
			backgroundImagePreview = editData.backgroundImage || null;
			if (editData.coordinates) {
				location = {
					lat: editData.coordinates.lat,
					lng: editData.coordinates.lng,
					name: editData.location
				};
			}
			capacity = editData.maxParticipants || 1;
			eventDate = editData.targetDate || '';
			duration = convertDurationForEdit(editData.duration);
		}
	});

	// Reset form on close
	$effect(() => {
		if (!open) {
			resetForm();
		}
	});

	function submitPost() {
		if (!location) {
			alert('Please select a location on the map');
			return;
		}

		// Parse duration string to hours
		function parseDurationToHours(durationStr: string): number {
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

		let postData: {
			title: string;
			description: string;
			tags: string[];
			location: { lat: number; lng: number; name: string };
			maxParticipants: number;
			type: string;
			duration: number;
			eventDate?: string;
		} = {
			title,
			description,
			tags,
			location: {
				lat: location.lat,
				lng: location.lng,
				name: location.name
			},
			maxParticipants: capacity,
			type: type === 'Offer' ? 'OFFER' : 'NEED',
			duration: parseDurationToHours(duration)
		};

		// Only include eventDate if it's not empty
		if (eventDate && eventDate.trim()) {
			postData.eventDate = eventDate;
		}

		dispatch(
			mode === 'edit' ? 'update' : 'submit',
			mode === 'edit' ? { id: editData.id, ...postData } : postData
		);
		closeModal();
	}

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

<BaseModal bind:open size="max-w-5xl">
	<h3 class="mb-4 flex items-center gap-2 text-lg font-bold">
		{mode === 'edit' ? 'Edit' : 'Create New'}
		<div class="join">
			<button
				class="btn join-item btn-sm btn-success {type === 'Offer' ? 'btn-active' : ''}"
				onclick={() => (type = 'Offer')}
			>
				<HeartHandshake class="h-4 w-4" />
				Offer
			</button>
			<button
				class="btn join-item btn-sm btn-accent {type === 'Need' ? 'btn-active' : ''}"
				onclick={() => (type = 'Need')}
			>
				<HandHelping class="h-4 w-4" />
				Need
			</button>
		</div>
	</h3>

	<form onsubmit={submitPost} class="grid grid-cols-1 gap-6 lg:grid-cols-2">
		<!-- Left Column: All Text Inputs -->
		<div class="space-y-4">
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
					maxlength="256"
					required
				/>
				<div class="mt-1 text-xs text-base-content/60">{title.length} / 256</div>
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
					maxlength="1024"
					required
				></textarea>
				<div class="mt-1 text-xs text-base-content/60">{description.length} / 1024</div>
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
						bind:value={eventDate}
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
				<label class="label">
					<span class="label-text flex items-center gap-2">
						<Tag class="h-4 w-4" />
						Tags
					</span>
				</label>
				<TagInput bind:tags />
			</div>
		</div>

		<!-- Right Column: Location Map -->
		<div class="space-y-4">
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
						initialLat={mode === 'edit' && editData?.coordinates
							? editData.coordinates.lat
							: userLat}
						initialLng={mode === 'edit' && editData?.coordinates
							? editData.coordinates.lng
							: userLng}
						height="h-96"
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
		</div>

		<!-- Action Buttons (spans both columns) -->
		<div class="modal-action lg:col-span-2">
			<button type="button" class="btn" onclick={closeModal}>Cancel</button>
			<button type="submit" class="btn btn-primary">
				{mode === 'edit' ? 'Save' : 'Post'}
				{type}
			</button>
		</div>
	</form>
</BaseModal>
