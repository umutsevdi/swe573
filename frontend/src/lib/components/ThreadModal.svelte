<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { Tag } from '@lucide/svelte';
	import TagInput from './TagInput.svelte';
	import BaseModal from './BaseModal.svelte';
	import { toast } from '$lib/stores/toast';

	const dispatch = createEventDispatcher<{
		submit: { title: string; description: string; tags: string[] };
		update: { title: string; description: string; tags: string[] };
	}>();

	let {
		mode,
		open = $bindable(false),
		currentTitle = '',
		currentDescription = '',
		currentTags = []
	} = $props<{
		mode: 'create' | 'edit';
		open: boolean;
		currentTitle?: string;
		currentDescription?: string;
		currentTags?: string[];
	}>();

	let title = $state('');
	let description = $state('');
	let tags = $state<string[]>([]);

	function openModal() {
		open = true;
	}

	function closeModal() {
		open = false;
	}

	function resetForm() {
		if (mode === 'edit') {
			title = currentTitle;
			description = currentDescription;
			tags = [...currentTags];
		} else {
			title = '';
			description = '';
			tags = [];
		}
	}

	function submitThread() {
		if (!title.trim() || !description.trim()) {
			toast.error('Title and description are required');
			return;
		}

		const data = { title: title.trim(), description: description, tags };
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
		{mode === 'edit' ? 'Edit Thread' : 'Create New Thread'}
	</h3>

	<form onsubmit={submitThread} class="space-y-4">
		<!-- Title -->
		<div class="form-control">
			<label class="label" for="title-input">
				<span class="label-text">Title</span>
			</label>
			<input
				id="title-input"
				type="text"
				placeholder="Enter thread title"
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
				placeholder="Describe your thread"
				class="textarea-bordered textarea h-24 w-full"
				bind:value={description}
				maxlength="1024"
				required
			/>
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

		<!-- Action Buttons -->
		<div class="modal-action">
			<button type="button" class="btn" onclick={closeModal}>Cancel</button>
			<button type="submit" class="btn btn-primary">
				{mode === 'edit' ? 'Update' : 'Create Thread'}
			</button>
		</div>
	</form>
</BaseModal>
