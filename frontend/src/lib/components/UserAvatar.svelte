<script lang="ts">
	import { User } from '@lucide/svelte';
	import { resolve } from '$app/paths';
	import { SERVER_URL } from '$lib/config';
	import { auth } from '$lib/stores/auth';
	import { userCardCache, type UserCardData } from '$lib/stores/user';
	import UserTooltipCard from './UserTooltipCard.svelte';

	let { userId } = $props<{
		userId: number;
	}>();

	let loading = $state(false);
	let avatarError = $state(false);

	// Subscribe to cache
	let cache = $state<Map<number, UserCardData>>(new Map());
	userCardCache.subscribe((value) => {
		cache = value;
	});

	let userData = $derived(cache.has(userId) ? cache.get(userId)! : null);

	// Load user data
	$effect(() => {
		if (userId && !cache.has(userId)) {
			loadUserData(userId);
		}
	});

	async function loadUserData(id: number) {
		loading = true;

		try {
			const headers: any = {};
			if ($auth.token) {
				headers.Authorization = `Bearer ${$auth.token}`;
			}
			const response = await fetch(`${SERVER_URL}/user/${id}/card`, { headers });
			if (!response.ok) {
				throw new Error(`Failed to fetch user data (${response.status})`);
			}

			const data: any = await response.json();
			console.log('Fetched user data for', id, data);
			const userCardData: UserCardData = {
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

			userData = userCardData;
		} catch (err) {
			console.error('Error loading user data:', err);
		} finally {
			loading = false;
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
				{#if !avatarError}
					<img
						src={`${SERVER_URL}/assets/profile/${userId}`}
						alt={userData?.fullName || 'Unknown User'}
						onerror={() => (avatarError = true)}
					/>
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
