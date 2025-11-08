<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { Tag, X, Plus } from '@lucide/svelte';

	const dispatch = createEventDispatcher<{
		submit: { title: string; description: string; tags: string[] };
	}>();

	let { open = $bindable(false) } = $props<{
		open: boolean;
	}>();

	let modal: HTMLDialogElement;
	let title = $state('');
	let description = $state('');
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
		title = '';
		description = '';
		tagInput = '';
		tags = [];
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

	function submitThread() {
		if (!title.trim() || !description.trim()) {
			alert('Title and description are required');
			return;
		}

		dispatch('submit', { title: title.trim(), description: description.trim(), tags });
		closeModal();
	}

	// Export function for parent component access
	export { openModal };
</script>

<dialog bind:this={modal} class="modal" onclose={() => (open = false)}>
	<div class="modal-box max-w-2xl">
		<h3 class="mb-4 text-lg font-bold">Create New Thread</h3>

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
					placeholder="Describe your thread"
					class="textarea-bordered textarea h-24 w-full"
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

			<!-- Action Buttons -->
			<div class="modal-action">
				<button type="button" class="btn" onclick={closeModal}>Cancel</button>
				<button type="submit" class="btn btn-primary">Create Thread</button>
			</div>
		</form>
	</div>
	<form method="dialog" class="modal-backdrop">
		<button onclick={closeModal}>close</button>
	</form>
</dialog>
