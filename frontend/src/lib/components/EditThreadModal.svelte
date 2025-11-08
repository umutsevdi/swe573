<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { X, Tag, Plus } from '@lucide/svelte';

	const dispatch = createEventDispatcher<{
		update: { title: string; description: string; tags: string[] };
	}>();

	let {
		open = $bindable(false),
		currentTitle = '',
		currentDescription = '',
		currentTags = []
	} = $props<{
		open: boolean;
		currentTitle: string;
		currentDescription: string;
		currentTags: string[];
	}>();

	let modal: HTMLDialogElement | undefined;
	let title = $state(currentTitle);
	let description = $state(currentDescription);
	let tags = $state(currentTags);
	let tagInput = $state('');

	function openModal() {
		modal?.showModal();
		open = true;
	}

	function closeModal() {
		modal?.close();
		open = false;
		reset();
	}

	// Sync open prop with modal
	$effect(() => {
		if (open && modal) {
			modal.showModal();
		}
	});

	function addTag() {
		const trimmedTag = tagInput.trim().toLowerCase();
		if (trimmedTag && !tags.includes(trimmedTag)) {
			tags = [...tags, trimmedTag];
			tagInput = '';
		}
	}

	function removeTag(tagToRemove: string) {
		tags = tags.filter((tag: string) => tag !== tagToRemove);
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			event.preventDefault();
			addTag();
		}
	}

	function submit(event: Event) {
		event.preventDefault();
		if (!title.trim() || !description.trim()) {
			alert('Title and description are required');
			return;
		}
		const updates: { title: string; description: string; tags: string[] } = {
			title: title.trim(),
			description: description.trim(),
			tags
		};
		dispatch('update', updates);
		closeModal();
	}

	function reset() {
		title = currentTitle;
		description = currentDescription;
		tags = currentTags;
		tagInput = '';
	}

	$effect(() => {
		if (open) {
			reset();
		}
	});

	// Export function for parent component access
	export { openModal };
</script>

<dialog bind:this={modal} class="modal" onclose={() => (open = false)}>
	<div class="modal-box max-w-2xl">
		<h3 class="mb-4 text-lg font-bold">Edit Thread</h3>

		<form onsubmit={submit} class="space-y-4">
			<!-- Title -->
			<div class="form-control">
				<label class="label">
					<span class="label-text">Title</span>
				</label>
				<input
					type="text"
					class="input-bordered input w-full"
					placeholder="Thread title..."
					bind:value={title}
					required
				/>
			</div>

			<!-- Description -->
			<div class="form-control">
				<label class="label">
					<span class="label-text">Description</span>
				</label>
				<textarea
					class="textarea-bordered textarea h-24 w-full"
					placeholder="Thread description..."
					bind:value={description}
					required
				></textarea>
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
			<div class="modal-action">
				<button class="btn btn-ghost" onclick={closeModal}>Cancel</button>
				<button type="submit" class="btn btn-primary">Update</button>
			</div>
		</form>
		<form method="dialog" class="modal-backdrop">
			<button onclick={closeModal}>close</button>
		</form>
	</div>
</dialog>
