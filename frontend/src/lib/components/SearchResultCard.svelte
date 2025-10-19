<script lang="ts">
	import { HandHelping, HeartHandshake, MapPin } from '@lucide/svelte';
	export let title: string;
	export let description: string;
	export let tags: string[] = [];
	export let location: string;
	export let participantCount: number;
	export let ownerUrl: string;
	export let ownerProfileUrl: string;
	export let type: 'Offer' | 'Need' = 'Offer';

	const badgeColors = [
		'badge-primary',
		'badge-secondary',
		'badge-accent',
		'badge-info',
		'badge-success',
		'badge-warning',
		'badge-error'
	];
	let badgeColor = (i: number) => badgeColors[i];
</script>

<li>
	<div
		role="button"
		class="list-row m-5 cursor-pointer bg-base-200 shadow-sm"
		onclick={document.getElementById('search-modal').showModal()}
	>
		<div class="space-y-3">
			<div class={`badge  font-semibold uppercase ${badgeColor(type == 'Offer' ? 1 : 0)}`}>
				{#if type == 'Offer'}
					<HeartHandshake />
				{:else}
					<HandHelping />
				{/if}
				{type}
			</div>
			<div class="flex items-center justify-center text-xs opacity-60">
				<MapPin />
				<p>{location}</p>
			</div>
		</div>
		<div>
			<h2 class="text-xl font-bold">
				{title}
			</h2>
			<div class="gap-4">
				<div class="mb-2 flex flex-wrap gap-2">
					{#each tags as tag, i}
						<span class={`badge badge-outline badge-sm ${badgeColor(i)}`}>{tag}</span>
					{/each}
				</div>
			</div>
			<p class="list-col-wrap min-h-10 text-xs">
				{description}
			</p>
		</div>
		<div class="avatar-group -space-x-5">
			{#if participantCount}
				<div class="avatar avatar-placeholder h-15 w-15">
					<div class="bg-neutral text-neutral-content">
						<span>+{participantCount}</span>
					</div>
				</div>
			{/if}
			<a class="avatar h-15 w-15" href={ownerUrl}>
				<img alt="Owner" src={ownerProfileUrl} />
			</a>
		</div>
	</div>
</li>
