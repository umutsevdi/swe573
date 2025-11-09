<script lang="ts">
	import { User } from '@lucide/svelte';
	import { resolve } from '$app/paths';
	import { SERVER_URL } from '$lib/config';
	import { auth } from '$lib/stores/auth';
	import {
		userCardCache,
		userDataPromises,
		avatarPromises,
		avatarUrls,
		type UserCard
	} from '$lib/stores/user';
	import UserTooltipCard from './UserTooltipCard.svelte';

	let { userId } = $props<{
		userId: number;
	}>();

	let loading = $state(false);
	let avatarBlobUrl = $state<string | null>(null);

	let userData = $derived($userCardCache.has(userId) ? $userCardCache.get(userId)! : null);
	let avatarUrl = $derived($avatarUrls.has(userId) ? $avatarUrls.get(userId)! : null);

	// Load user data and avatar
	$effect(() => {
		if (userId && !userData && !$userDataPromises.has(userId)) {
			loadUserData(userId);
		}
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

	async function loadUserData(id: number) {
		loading = true;

		try {
			if ($userDataPromises.has(id)) {
				await $userDataPromises.get(id)!;
				return;
			}

			const promise = (async () => {
				const headers: any = {};
				if ($auth.token) {
					headers.Authorization = `Bearer ${$auth.token}`;
				}
				const response = await fetch(`${SERVER_URL}/user/${id}/card`, { headers });
				if (!response.ok) {
					throw new Error(`Failed to fetch user data (${response.status})`);
				}

				const data: any = await response.json();
				const userCardData: UserCard = {
					id,
					fullName: data.fullName || 'Unknown User',
					biography: data.biography || '',
					interests: data.interests || [],
					userScore: data.userScore || 0
				};

				// Update cache
				userCardCache.update((currentCache) => {
					return new Map(currentCache).set(id, userCardData);
				});
				return userCardData;
			})();

			userDataPromises.update((m) => new Map(m).set(id, promise));

			await promise;
		} catch (err) {
			console.error('Error loading user data:', err);
		} finally {
			userDataPromises.update((m) => {
				const newM = new Map(m);
				newM.delete(id);
				return newM;
			});
			loading = false;
		}
	}

	async function loadAvatar(id: number) {
		try {
			if ($avatarPromises.has(id)) {
				await $avatarPromises.get(id)!;
				return;
			}

			const promise = (async (): Promise<string> => {
				const response = await fetch(`${SERVER_URL}/assets/profile/${id}`, {
					headers: {
						Authorization: `Bearer ${$auth.token}`
					}
				});

				if (response.ok) {
					const blob = await response.blob();
					const blobUrl = URL.createObjectURL(blob);
					avatarUrls.update((m) => new Map(m).set(id, blobUrl));
					return blobUrl;
				} else if (response.status === 404) {
					const fallbackUrl = '/assets/profile.png';
					avatarUrls.update((m) => new Map(m).set(id, fallbackUrl));
					return fallbackUrl;
				} else {
					throw new Error('Failed to load avatar');
				}
			})();

			avatarPromises.update((m) => new Map(m).set(id, promise));

			await promise;
		} catch (error) {
			console.error('Failed to load avatar:', error);
		} finally {
			avatarPromises.update((m) => {
				const newM = new Map(m);
				newM.delete(id);
				return newM;
			});
		}
	}

	let showTooltip = $state(false);
	let tooltipTimeout: number | undefined;

	function handleMouseEnter() {
		tooltipTimeout = window.setTimeout(() => {
			showTooltip = true;
		}, 750);
	}

	function handleMouseLeave() {
		if (tooltipTimeout) {
			clearTimeout(tooltipTimeout);
			tooltipTimeout = undefined;
		}
		showTooltip = false;
	}
</script>

{#if loading}
	<div class="flex items-center gap-2">
		<div class="avatar">
			<div class="w-10 rounded-full">
				<div class="bg-neutral text-neutral-content">
					<div class="loading loading-sm loading-spinner"></div>
				</div>
			</div>
		</div>
		<span class="font-semibold">Loading...</span>
	</div>
{:else if userData}
	<a
		href={resolve(`/user/${userId}`)}
		class="relative flex cursor-pointer items-center gap-2"
		onmouseenter={handleMouseEnter}
		onmouseleave={handleMouseLeave}
	>
		<div class="avatar">
			<div class="w-10 rounded-full">
				{#if avatarBlobUrl}
					<img src={avatarBlobUrl} alt={userData?.fullName || 'Unknown User'} />
				{:else}
					<div class="avatar avatar-placeholder">
						<div class="w-24 rounded-full bg-neutral text-neutral-content">
							<span class="text-3xl"
								>{(userData?.fullName || 'Unknown User')
									.split(' ')
									.map((a: string) => a.charAt(0))
									.join(' ')}</span
							>
						</div>
					</div>
				{/if}
			</div>
		</div>
		<span class="font-semibold">{userData?.fullName || 'Unknown User'}</span>
		{#if showTooltip}
			<div class="absolute top-full left-0 z-50 mt-2">
				<UserTooltipCard {userData} />
			</div>
		{/if}
	</a>
{:else}
	<div class="flex items-center gap-2">
		<div class="avatar">
			<div class="w-10 rounded-full">
				<div class="bg-neutral text-neutral-content">
					<User />
				</div>
			</div>
		</div>
		<span class="font-semibold">Unknown User</span>
	</div>
{/if}
