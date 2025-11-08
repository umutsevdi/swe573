<script lang="ts">
	import { Annoyed, Frown, Meh, Smile } from '@lucide/svelte';
	import { type UserCardData } from '$lib/stores/user';

	let { userData } = $props<{
		userData: UserCardData;
	}>();

	const badgeColors = ['text-success', 'text-warning', 'text-error'];
	const bgColor = (i: number) =>
		i > 70 ? badgeColors[0] : i > 49 ? badgeColors[1] : badgeColors[2];
</script>

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
		<p class="mb-3 text-sm text-base-content/80">{userData.biography}</p>
		{#if userData.interests.length}
			<div>
				<h4 class="mb-1 text-xs font-semibold text-base-content/60">Interests</h4>
				<div class="flex flex-wrap gap-1">
					{#each userData.interests as interest (interest)}
						<span class="badge badge-xs badge-primary">{interest}</span>
					{/each}
				</div>
			</div>
		{/if}
	</div>
</div>
