<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { Tag, MapPin } from '@lucide/svelte';
	import TagInput from './TagInput.svelte';
	import BaseModal from './BaseModal.svelte';
	import LocationPicker from './LocationPicker.svelte';
	import { toast } from '$lib/stores/toast';

	const dispatch = createEventDispatcher<{
		submit: {
			title: string;
			description: string;
			tags: string[];
			location: { lat: number; lon: number; name: string };
			maxParticipants: number;
			type: 'OFFER' | 'NEED';
			duration: number;
			eventDate: string;
		};
		update: {
			title: string;
			description: string;
			tags: string[];
			location: { lat: number; lon: number; name: string };
			maxParticipants: number;
			type: 'OFFER' | 'NEED';
			duration: number;
			eventDate: string;
		};
	}>();

	let {
		mode,
		open = $bindable(false),
		currentTitle = '',
		currentDescription = '',
		currentTags = [],
		currentLocation,
		currentMaxParticipants = 0,
		currentType = 'OFFER',
		currentDuration = 0,
		currentEventDate = ''
	} = $props<{
		mode: 'create' | 'edit';
		open: boolean;
		currentTitle?: string;
		currentDescription?: string;
		currentTags?: string[];
		currentLocation?: { lat: number; lon: number; name: string };
		currentMaxParticipants?: number;
		currentType?: 'OFFER' | 'NEED';
		currentDuration?: number;
		currentEventDate?: string;
	}>();

	let title = $state('');
	let description = $state('');
	let tags = $state<string[]>([]);
	let location = $state({ lat: 41.0082, lon: 28.9784, name: 'Istanbul' });
	let maxParticipants = $state(0);
	let type = $state<'OFFER' | 'NEED'>('OFFER');
	let duration = $state(1);
	let eventDate = $state('');

	function openModal() {
		open = true;
	}

	function closeModal() {
		open = false;
	}

	function resetForm() {
		if (mode === 'edit') {
			title = currentTitle || '';
			description = currentDescription || '';
			tags = currentTags ? [...currentTags] : [];
			location = currentLocation
				? { ...currentLocation }
				: { lat: 41.0082, lon: 28.9784, name: 'Istanbul' };
			maxParticipants = currentMaxParticipants || 0;
			type = currentType || 'OFFER';
			duration = currentDuration ? Math.floor(currentDuration / 60) : 1;
			eventDate = currentEventDate || '';
		} else {
			title = '';
			description = '';
			tags = [];
			location = { lat: 41.0082, lon: 28.9784, name: 'Istanbul' };
			maxParticipants = 0;
			type = 'OFFER';
			duration = 1;
			eventDate = new Date().toISOString().split('T')[0];
		}
	}

	function submitEvent() {
		if (!title.trim() || !description.trim()) {
			toast.error('Title and description are required');
			return;
		}

		if (!location || !location.name) {
			location = { lat: 41.0082, lon: 28.9784, name: 'Istanbul' };
		}

		const data = {
			title: title.trim(),
			description: description,
			tags,
			location,
			maxParticipants,
			type,
			duration: duration,
			eventDate
		};
		console.log(data);
		dispatch(mode === 'edit' ? 'update' : 'submit', data);
		closeModal();
	}

	// Initialize form on open
	$effect(() => {
		if (open) {
			resetForm();
		}
	});

	// Reset form on close
	$effect(() => {
		if (!open) {
			resetForm();
		}
	});

	// Export function for parent component access
	export { openModal };
</script>

<BaseModal bind:open size="max-w-2xl">
	<h3 class="mb-4 text-lg font-bold">
		{mode === 'edit' ? 'Edit Event' : 'Create New Event'}
	</h3>

	<form onsubmit={submitEvent} class="space-y-4">
		<!-- Title -->
		<div class="form-control">
			<label class="label" for="title-input">
				<span class="label-text">Title</span>
			</label>
			<input
				id="title-input"
				type="text"
				placeholder="Enter event title"
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
				placeholder="Describe your event"
				class="textarea-bordered textarea h-24 w-full"
				bind:value={description}
				maxlength="1024"
				required
			></textarea>
			<div class="mt-1 text-xs text-base-content/60">{description.length} / 1024</div>
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

		<!-- Location -->
		<div class="form-control">
			<label class="label">
				<span class="label-text flex items-center gap-2">
					<MapPin class="h-4 w-4" />
					Location
				</span>
			</label>
			<LocationPicker
				initialLat={mode === 'edit' && currentLocation ? currentLocation.lat : undefined}
				initialLon={mode === 'edit' && currentLocation ? currentLocation.lon : undefined}
				initialName={mode === 'edit' && currentLocation ? currentLocation.name : undefined}
				onLocationChange={(loc) => (location = loc)}
			/>
		</div>

		<!-- Max Participants -->
		<div class="form-control">
			<label class="label" for="max-participants">
				<span class="label-text">Max Participants</span>
			</label>
			<input
				id="max-participants"
				type="number"
				class="input-bordered input w-full"
				bind:value={maxParticipants}
				min="0"
			/>
		</div>

		<!-- Type -->
		<div class="form-control">
			<label class="label" for="type">
				<span class="label-text">Type</span>
			</label>
			<select id="type" class="select-bordered select w-full" bind:value={type}>
				<option value="OFFER">OFFER</option>
				<option value="NEED">NEED</option>
			</select>
		</div>

		<!-- Duration -->
		<div class="form-control">
			<label class="label" for="duration">
				<span class="label-text">Duration (hours)</span>
			</label>
			<input
				id="duration"
				type="number"
				class="input-bordered input w-full"
				bind:value={duration}
				min="1"
				max="12"
			/>
		</div>

		<!-- Event Date -->
		<div class="form-control">
			<label class="label" for="event-date">
				<span class="label-text">Event Date</span>
			</label>
			<input
				id="event-date"
				type="date"
				class="input-bordered input w-full"
				bind:value={eventDate}
			/>
		</div>

		<!-- Action Buttons -->
		<div class="modal-action">
			<button type="button" class="btn" onclick={closeModal}>Cancel</button>
			<button type="submit" class="btn btn-primary">
				{mode === 'edit' ? 'Update' : 'Create Event'}
			</button>
		</div>
	</form>
</BaseModal>
