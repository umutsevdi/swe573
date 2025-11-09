<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { X, Upload, User, Tag, Plus } from '@lucide/svelte';
	import TagInput from './TagInput.svelte';

	const dispatch = createEventDispatcher<{
		update: { biography: string; interests: string[]; avatar?: File; background?: File };
	}>();

	let {
		open = $bindable(false),
		currentBiography = '',
		currentInterests = [],
		currentAvatar,
		currentBackground
	} = $props<{
		open: boolean;
		currentBiography: string;
		currentInterests: string[];
		currentAvatar?: string | null;
		currentBackground?: string | null;
	}>();

	let modal: HTMLDialogElement | undefined;
	let biography = $state(currentBiography);
	let interests = $state(currentInterests);
	let avatarFile = $state<File | null>(null);
	let avatarPreview = $state<string | null>(currentAvatar || null);
	let backgroundFile = $state<File | null>(null);
	let backgroundPreview = $state<string | null>(currentBackground || null);

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

	function handleAvatarChange(event: Event) {
		const target = event.target as HTMLInputElement;
		const file = target.files?.[0];
		if (file) {
			avatarFile = file;
			const reader = new FileReader();
			reader.onload = (e) => {
				avatarPreview = e.target?.result as string;
			};
			reader.readAsDataURL(file);
		}
	}

	function handleBackgroundChange(event: Event) {
		const target = event.target as HTMLInputElement;
		const file = target.files?.[0];
		if (file) {
			backgroundFile = file;
			const reader = new FileReader();
			reader.onload = (e) => {
				backgroundPreview = e.target?.result as string;
			};
			reader.readAsDataURL(file);
		}
	}

	function submit() {
		dispatch('update', {
			biography,
			interests,
			avatar: avatarFile || undefined,
			background: backgroundFile || undefined
		});
		closeModal();
	}

	function reset() {
		biography = currentBiography;
		interests = currentInterests;
		avatarFile = null;
		avatarPreview = currentAvatar || null;
		backgroundFile = null;
		backgroundPreview = currentBackground || null;
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
		<h3 class="mb-4 text-lg font-bold">Edit Profile</h3>

		<div class="space-y-4">
			<!-- Avatar Upload -->
			<div class="form-control">
				<label class="label">
					<span class="label-text">Profile Picture</span>
				</label>
				<div class="flex items-center gap-4">
					<div class="avatar">
						<div class="w-16 rounded-full">
							{#if avatarPreview}
								<img src={avatarPreview} alt="Avatar preview" />
							{:else}
								<div class="bg-neutral text-neutral-content">
									<User class="h-8 w-8" />
								</div>
							{/if}
						</div>
					</div>
					<label class="btn btn-outline btn-sm">
						<Upload class="mr-2 h-4 w-4" />
						Upload Photo
						<input type="file" accept="image/*" class="hidden" onchange={handleAvatarChange} />
					</label>
				</div>
			</div>

			<!-- Background Upload -->
			<div class="form-control">
				<label class="label">
					<span class="label-text">Background Picture</span>
				</label>
				<div class="flex items-center gap-4">
					<div class="avatar">
						<div class="h-16 w-16 rounded">
							{#if backgroundPreview}
								<img src={backgroundPreview} alt="Background preview" class="object-cover" />
							{:else}
								<div
									class="flex h-full items-center justify-center bg-neutral text-neutral-content"
								>
									<User class="h-8 w-8" />
								</div>
							{/if}
						</div>
					</div>
					<label class="btn btn-outline btn-sm">
						<Upload class="mr-2 h-4 w-4" />
						Upload Background
						<input type="file" accept="image/*" class="hidden" onchange={handleBackgroundChange} />
					</label>
				</div>
			</div>

			<!-- Biography -->
			<div class="form-control">
				<label class="label">
					<span class="label-text">Biography</span>
				</label>
				<textarea
					class="textarea-bordered textarea h-24 w-full"
					placeholder="Tell us about yourself..."
					bind:value={biography}
					maxlength="1024"
				></textarea>
				<div class="mt-1 text-xs text-base-content/60">{biography.length} / 1024</div>
			</div>

			<!-- Interests -->
			<div class="form-control">
				<label class="label">
					<span class="label-text flex items-center gap-2">
						<Tag class="h-4 w-4" />
						Interests
					</span>
				</label>
				<TagInput bind:tags={interests} placeholder="Add an interest (press Enter)" />
			</div>
		</div>

		<div class="modal-action">
			<button class="btn btn-ghost" onclick={closeModal}>Cancel</button>
			<button class="btn btn-primary" onclick={submit}>Save Changes</button>
		</div>
	</div>
	<form method="dialog" class="modal-backdrop">
		<button onclick={closeModal}>close</button>
	</form>
</dialog>
