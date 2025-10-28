<script lang="ts">
	import { MessageSquare, Calendar } from '@lucide/svelte';
	import { resolve } from '$app/paths';
	import UserAvatar from './UserAvatar.svelte';

	type ForumPostData = {
		id: string;
		author: string;
		avatar: string;
		content: string;
		timestamp: string;
		replies: number;
	};

	type ForumTopicData = {
		id: string;
		title: string;
		author: string;
		avatar: string;
		createdAt: string;
		repliesCount: number;
		tags: string[];
		posts: ForumPostData[];
	};

	export let topic: ForumTopicData;
</script>

<a
	href={resolve(`/forum/${topic.id}`)}
	class="mb-4 block cursor-pointer rounded-lg border border-base-300 bg-base-100 p-4 transition-colors hover:bg-base-200"
>
	<div class="flex items-center gap-3 text-xl font-medium">
		<MessageSquare />
		<span>{topic.title}</span>
		<div class="badge badge-primary">{topic.repliesCount} replies</div>
		<div class="flex gap-1">
			{#each topic.tags as tag (tag)}
				<span class="badge badge-primary">{tag}</span>
			{/each}
		</div>
	</div>
	<div class="mt-2 flex items-center gap-2 text-sm opacity-70">
		<UserAvatar
			avatar={topic.avatar}
			name={topic.author}
			showName={false}
			userData={{
				fullName: topic.author,
				bio: 'Topic author bio',
				interests: ['Forum'],
				userScore: 70
			}}
		/>
		<span>Started by {topic.author}</span>
		<Calendar />
		<span>{topic.createdAt}</span>
	</div>
</a>
