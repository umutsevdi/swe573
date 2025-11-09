<script lang="ts">
	import { Calendar } from '@lucide/svelte';
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import UserAvatar from '$lib/components/UserAvatar.svelte';

	type ForumThread = {
		id: number;
		title: string;
		active: boolean;
		description: string;
		createdAt: string;
		ownerId: number;
		tags: string[];
	};

	let { data } = $props();
	let threads = $derived(data.threads as ForumThread[]);
</script>

<div class="hero min-h-screen bg-base-200">
	<div class="hero-content w-full max-w-4xl flex-col">
		<div class="w-full">
			<div class="mb-8">
				<h1 class="text-4xl font-bold">Forum</h1>
			</div>

			<div class="space-y-4">
				{#each threads as thread (thread.id)}
					<!-- svelte-ignore a11y_click_events_have_key_events -->
					<div
						class="card cursor-pointer shadow-md hover:shadow-lg {thread.active
							? 'bg-base-100'
							: 'bg-base-200'}"
						onclick={() => goto(resolve(`/forum/${thread.id}`))}
						role="button"
						tabindex="0"
						aria-label="View thread {thread.title}"
					>
						<div class="card-body p-4">
							<!-- Title with closed badge -->
							<div class="flex items-center">
								<h2 class="card-title flex-1">{thread.title}</h2>
								{#if !thread.active}
									<div class="badge shrink-0 font-semibold uppercase badge-neutral">Closed</div>
								{/if}
							</div>

							<!-- Description row with date on right -->
							<div class="flex items-start gap-3">
								<div class="flex-1">
									<p
										class="line-clamp-2 text-sm text-base-content/70"
										style="white-space: pre-wrap;"
									>
										{thread.description}
									</p>
								</div>
								<div class="flex shrink-0 items-center gap-1 text-sm opacity-70">
									<Calendar class="h-4 w-4" />
									<span>{new Date(thread.createdAt).toLocaleDateString()}</span>
								</div>
							</div>

							<div class="flex">
								<!-- Tags -->
								{#if thread.tags.length > 0}
									<div class="flex flex-1 flex-wrap gap-2">
										{#each thread.tags as tag (tag)}
											<div class="badge gap-2 badge-outline badge-accent">{tag}</div>
										{/each}
									</div>
								{/if}
								<div class="flex shrink-0 items-center gap-2">
									<UserAvatar userId={thread.ownerId} />
								</div>
							</div>
						</div>
					</div>
				{/each}
			</div>

			{#if threads.length === 0}
				<div class="text-center text-base-content/50">
					<p>No forum threads found.</p>
				</div>
			{/if}
		</div>
	</div>
</div>

<style>
	.card {
		transition: all 0.2s ease-in-out;
	}

	.card:hover {
		transform: translateY(-2px);
	}

	.line-clamp-2 {
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
		line-clamp: 2;
	}
</style>
