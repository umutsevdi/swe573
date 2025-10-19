<script lang="ts">
	import { MessageCircle, User } from '@lucide/svelte';
	import UserAvatar from './UserAvatar.svelte';

	type ForumPostData = {
		id: string;
		author: string;
		avatar: string;
		content: string;
		timestamp: string;
		replies: ForumPostData[];
	};

	let {
		author,
		avatar,
		content,
		timestamp,
		replies = [],
		userData
	} = $props<{
		userData?: {
			fullName: string;
			bio: string;
			interests: string[];
			userScore: number;
		};
	}>();

	let showReplyForm = $state(false);
	let replyContent = $state('');

	function handleReply() {
		showReplyForm = !showReplyForm;
	}

	function submitReply() {
		if (replyContent.trim()) {
			// In a real app, send to server
			console.log('Reply submitted:', replyContent);
			replyContent = '';
			showReplyForm = false;
		}
	}
</script>

<div class="card mb-4 bg-base-100 shadow-sm">
	<div class="card-body p-4">
		<div class="flex items-start gap-3">
			<div class="flex-1">
				<div class="mb-2 flex items-center gap-2">
					<UserAvatar {avatar} name={author} {userData} />
					<span class="text-xs opacity-60">{timestamp}</span>
				</div>
				<p class="text-sm">{content}</p>
				<div class="mt-3 flex items-center gap-4">
					<button class="btn btn-ghost btn-sm" onclick={handleReply}>
						<MessageCircle />
						Reply
					</button>
				</div>
				{#if showReplyForm}
					<div class="mt-4">
						<textarea
							class="textarea-bordered textarea w-full"
							placeholder="Write your reply..."
							bind:value={replyContent}
						></textarea>
						<div class="mt-2 flex gap-2">
							<button class="btn btn-sm btn-primary" onclick={submitReply}>Submit</button>
							<button class="btn btn-ghost btn-sm" onclick={() => (showReplyForm = false)}
								>Cancel</button
							>
						</div>
					</div>
				{/if}
				{#if replies.length > 0}
					<div class="mt-4 ml-4 space-y-4 border-l-2 border-base-300 pl-4">
						{#each replies as reply (reply.id)}
							<svelte:self {...reply} {userData} />
						{/each}
					</div>
				{/if}
			</div>
		</div>
	</div>
</div>
