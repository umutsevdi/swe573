<script lang="ts">
	import { auth } from '$lib/stores/auth';
	import { avatarPromises, avatarUrls } from '$lib/stores/user';
	import { loadAvatar as loadAvatarUtil } from '$lib/utils/avatar';

	let { userId, size = 'w-6' } = $props<{
		userId: number;
		size?: string;
	}>();

	let avatarBlobUrl = $state<string | null>(null);

	let avatarUrl = $derived($avatarUrls.has(userId) ? $avatarUrls.get(userId)! : null);

	// Load avatar
	$effect(() => {
		if (userId && $auth.token && !avatarUrl && !$avatarPromises.has(userId)) {
			loadAvatar(userId);
		}
		if (avatarUrl && !avatarBlobUrl) {
			avatarBlobUrl = avatarUrl;
		}

		// Cleanup blob URL on unmount (only if it's a blob URL)
		return () => {
			if (avatarBlobUrl && avatarBlobUrl.startsWith('blob:')) {
				URL.revokeObjectURL(avatarBlobUrl);
			}
		};
	});

	async function loadAvatar(id: number) {
		try {
			if ($avatarPromises.has(id)) {
				await $avatarPromises.get(id)!;
				return;
			}

			const token = $auth.token;
			if (!token) return;

			const promise = (async (): Promise<string> => {
				const result = await loadAvatarUtil(id, token);
				const blobUrl = result || '/assets/profile.png';
				avatarUrls.update((m) => new Map(m).set(id, blobUrl));
				return blobUrl;
			})();

			avatarPromises.update((m) => new Map(m).set(id, promise));

			await promise;
		} catch (error) {
			console.error('Failed to load avatar:', error);
			// Set fallback on error to prevent continuous queries
			avatarUrls.update((m) => new Map(m).set(id, '/assets/profile.png'));
		} finally {
			avatarPromises.update((m) => {
				const newM = new Map(m);
				newM.delete(id);
				return newM;
			});
		}
	}
</script>

<div class="avatar">
	<div class="{size} rounded-full">
		{#if avatarBlobUrl}
			<img src={avatarBlobUrl} alt="Avatar" />
		{:else}
			<div class="flex items-center justify-center bg-neutral text-neutral-content">
				<!-- Placeholder, perhaps initials or icon -->
				<span class="text-xs">?</span>
			</div>
		{/if}
	</div>
</div>
