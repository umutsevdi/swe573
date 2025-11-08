<script lang="ts">
	import { MessageCircle } from '@lucide/svelte';
	import UserAvatar from './UserAvatar.svelte';

	type Post = {
		id: number;
		content: string;
		timestamp: string;
		userId: number;
	};

	let { content, timestamp, userId, onReply } = $props<{
		content: string;
		timestamp: string;
		userId: number;
		onReply?: (post: Post) => void;
	}>();
	console.log(userId);

	let showReplyButton = $state(false);

	let parsedLines = $derived(
		content.split('\n').map((line: string) => {
			let depth = 0;
			let text = line;
			while (text.startsWith('>> ')) {
				depth++;
				text = text.slice(3);
			}
			return { depth, text };
		})
	);
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<div
	class="card mb-4 bg-base-100 shadow-sm"
	onmouseenter={() => (showReplyButton = true)}
	onmouseleave={() => (showReplyButton = false)}
>
	<div class="card-body p-4">
		<div class="flex items-start gap-3">
			<div class="flex-1">
				<div class="mb-2 flex items-center gap-2">
					<UserAvatar {userId} />
					<span class="text-xs opacity-60">{timestamp}</span>
				</div>

				<div class="text-sm">
					{#each parsedLines as line (line)}
						{#if line.depth > 0}
							<div
								class="mb-0 border-l-2 border-accent bg-base-100 text-accent italic"
								style="padding-left: {32 + (line.depth - 1) * 16}px"
							>
								{line.text}
							</div>
						{:else}
							<p class="mb-0">{line.text}</p>
						{/if}
					{/each}
				</div>
			</div>
			{#if showReplyButton && onReply}
				<div class="flex-shrink-0">
					<button
						class="btn opacity-60 btn-ghost btn-sm hover:opacity-100"
						onclick={() => onReply({ content, timestamp, userId })}
					>
						<MessageCircle class="h-4 w-4" />
					</button>
				</div>
			{/if}
		</div>
	</div>
</div>
