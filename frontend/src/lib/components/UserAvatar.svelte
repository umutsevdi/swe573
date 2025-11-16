<script lang="ts">
	import { User, Annoyed, Frown, Meh, Smile } from '@lucide/svelte';
	import { resolve } from '$app/paths';
	import { HIVE_API } from '$lib/config';
	import { auth } from '$lib/stores/auth';
	import { loadAvatar as loadAvatarUtil } from '$lib/utils/avatar';
	import {
		userCardCache,
		userDataPromises,
		avatarPromises,
		avatarUrls,
		type UserCard
	} from '$lib/stores/user';

	let { userId, hideText = false } = $props<{
		userId: number;
		hideText?: boolean;
	}>();

	const badgeColors = ['text-success', 'text-warning', 'text-error'];
	const bgColor = (i: number) =>
		i > 70 ? badgeColors[0] : i > 49 ? badgeColors[1] : badgeColors[2];

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
				const response = await fetch(`${HIVE_API}/user/${id}/card`, { headers });
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
		{#if !hideText}
			<span class="font-semibold">Loading...</span>
		{/if}
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
		{#if !hideText}
			<span class="font-semibold">{userData?.fullName || 'Unknown User'}</span>
		{/if}
		{#if showTooltip}
			<div class="absolute top-full left-0 z-[999] mt-2">
				<div class="card w-80 border border-base-300 bg-base-100 shadow-lg">
					<div class="card-body p-4">
						<div class="mb-2 flex items-center gap-2">
							<h3 class="card-title text-lg">{userData.fullName}</h3>
							<div
								class="tooltip avatar tooltip-bottom avatar-placeholder h-8 w-8"
								data-tip={`User behavior score: ${userData.userScore}%`}
							>
								<div class={`w-8 rounded-full ${bgColor(userData.userScore)}`}>
									{#if userData.userScore > 70}
										<Smile class="h-4 w-4" />
									{:else if userData.userScore > 49}
										<Meh class="h-4 w-4" />
									{:else if userData.userScore > 20}
										<Frown class="h-4 w-4" />
									{:else}
										<Annoyed class="h-4 w-4" />
									{/if}
								</div>
							</div>
						</div>
						<p class="mb-3 text-sm whitespace-pre-wrap text-base-content/80">
							{userData.biography}
						</p>
						{#if userData.interests.length}
							<div>
								<h4 class="mb-1 text-sm font-semibold text-base-content/60">Interests</h4>
								<div class="flex flex-wrap gap-1">
									{#each userData.interests as interest (interest)}
										<span class="badge badge-outline badge-accent">{interest}</span>
									{/each}
								</div>
							</div>
						{/if}
					</div>
				</div>
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
		{#if !hideText}
			<span class="font-semibold">Unknown User</span>
		{/if}
	</div>
{/if}
