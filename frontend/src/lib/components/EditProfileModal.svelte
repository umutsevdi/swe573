<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { X, Upload, User, Tag, Plus } from '@lucide/svelte';

	const dispatch = createEventDispatcher<{
		update: { bio: string; interests: string[]; avatar?: File };
	}>();

	let {
		open = $bindable(false),
		currentBio = '',
		currentInterests = [],
		currentAvatar
	} = $props<{
		open: boolean;
		currentBio: string;
		currentInterests: string[];
		currentAvatar?: string;
	}>();

	let modal: HTMLDialogElement | undefined;
	let bio = $state(currentBio);
	let interests = $state(currentInterests);
	let interestInput = $state('');
	let avatarFile = $state<File | null>(null);
	let avatarPreview = $state<string | null>(currentAvatar || null);

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

	function addInterest() {
		const trimmedInterest = interestInput.trim().toLowerCase();
		if (trimmedInterest && !interests.includes(trimmedInterest)) {
			interests = [...interests, trimmedInterest];
			interestInput = '';
		}
	}

	function removeInterest(interestToRemove: string) {
		interests = interests.filter((interest: string) => interest !== interestToRemove);
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			event.preventDefault();
			addInterest();
		}
	}

	function submit() {
		dispatch('update', {
			bio,
			interests,
			avatar: avatarFile || undefined
		});
		closeModal();
	}

	function reset() {
		bio = currentBio;
		interests = currentInterests;
		interestInput = '';
		avatarFile = null;
		avatarPreview = currentAvatar || null;
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

			<!-- Bio -->
			<div class="form-control">
				<label class="label">
					<span class="label-text">Bio</span>
				</label>
				<textarea
					class="textarea-bordered textarea h-24 w-full"
					placeholder="Tell us about yourself..."
					bind:value={bio}
				></textarea>
			</div>

			<!-- Interests -->
			<div class="form-control">
				<label class="label" for="interest-input">
					<span class="label-text flex items-center gap-2">
						<Tag class="h-4 w-4" />
						Interests
					</span>
				</label>
				<div class="mb-2 flex gap-2">
					<input
						id="interest-input"
						type="text"
						placeholder="Add an interest (press Enter)"
						class="input-bordered input flex-1"
						bind:value={interestInput}
						onkeydown={handleKeydown}
					/>
					<button type="button" class="btn btn-outline" onclick={addInterest}>
						<Plus class="h-4 w-4" />
					</button>
				</div>
				{#if interests.length > 0}
					<div class="flex flex-wrap gap-2">
						{#each interests as interest (interest)}
							<div class="badge gap-2 badge-primary">
								{interest}
								<button
									type="button"
									class="btn btn-ghost btn-xs"
									onclick={() => removeInterest(interest)}
								>
									<X class="h-3 w-3" />
								</button>
							</div>
						{/each}
					</div>
				{/if}
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
