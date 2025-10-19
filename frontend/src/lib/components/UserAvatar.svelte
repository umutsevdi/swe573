<script lang="ts">
	import { User } from '@lucide/svelte';
	import UserTooltipCard from './UserTooltipCard.svelte';

	let {
		avatar,
		name,
		userData,
		showName = true,
		clazz,
		avatarClass,
		size = 'w-10',
		userId
	} = $props<{
		avatar?: string;
		name: string;
		userData?: {
			fullName: string;
			bio: string;
			interests: string[];
			userScore: number;
		};
		showName?: boolean;
		clazz?: string;
		avatarClass?: string;
		size?: string;
		userId?: string;
	}>();

	let showTooltip = $state(false);
	let tooltipTimeout: number | undefined;

	function handleMouseEnter() {
		if (userData) {
			tooltipTimeout = window.setTimeout(() => {
				showTooltip = true;
			}, 750);
		}
	}

	function handleMouseLeave() {
		if (tooltipTimeout) {
			clearTimeout(tooltipTimeout);
			tooltipTimeout = undefined;
		}
		showTooltip = false;
	}
</script>

{#if userId}
	<a href="/user/{userId}" class="cursor-pointer">
		{@render avatarContent()}
	</a>
{:else}
	{@render avatarContent()}
{/if}

{#snippet avatarContent()}
	<div
		class="relative flex items-center gap-2 {clazz || ''}"
		role="presentation"
		onmouseenter={handleMouseEnter}
		onmouseleave={handleMouseLeave}
	>
		<div class="avatar {avatarClass || ''}">
			<div class="{size} rounded-full">
				{#if avatar}
					<img src={avatar} alt={name} />
				{:else}
					<div class="bg-neutral text-neutral-content">
						<User />
					</div>
				{/if}
			</div>
		</div>
		{#if showName}
			<span class="font-semibold">{name}</span>
		{/if}

		{#if showTooltip && userData}
			<div class="absolute top-full left-0 z-50 mt-2">
				<UserTooltipCard
					fullName={userData.fullName}
					bio={userData.bio}
					interests={userData.interests}
					userScore={userData.userScore}
				/>
			</div>
		{/if}
	</div>
{/snippet}

<style>
	/* Ensure tooltip appears above other elements */
</style>
