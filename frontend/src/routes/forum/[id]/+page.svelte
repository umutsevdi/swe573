<script lang="ts">
	import { ArrowLeft, MessageSquare, Calendar } from '@lucide/svelte';
	import ForumPost from '$lib/components/ForumPost.svelte';
	import UserAvatar from '$lib/components/UserAvatar.svelte';

	type ForumPostData = {
		id: string;
		author: string;
		avatar: string;
		content: string;
		timestamp: string;
		replies: ForumPostData[];
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

	let { data } = $props();
	let topic = $derived(data.topic as ForumTopicData);
</script>

{#if !data.topic}
	<div class="hero flex min-h-screen items-center justify-center bg-base-200">
		<div class="loading loading-lg loading-spinner"></div>
	</div>
{:else}
	<div class="hero min-h-screen bg-base-200">
		<div class="hero-content w-full max-w-4xl flex-col">
			<div class="w-full">
				<a href="/forum" class="btn mb-4 btn-ghost">
					<ArrowLeft />
					Back to Forum
				</a>
				<div class="card mb-6 bg-base-100 shadow-xl">
					<div class="card-body">
						<h1 class="mb-4 card-title text-3xl">{topic.title}</h1>
						<div class="mb-4 flex items-center gap-3">
							<UserAvatar
								avatar={topic.avatar}
								name={topic.author}
								showName={false}
								userId="3"
								userData={{
									fullName: topic.author,
									bio: 'Forum contributor',
									interests: ['Discussion', 'Community'],
									userScore: 80
								}}
							/>
							<div>
								<span class="font-semibold">{topic.author}</span>
								<div class="flex items-center gap-2 text-sm opacity-70">
									<Calendar />
									<span>{topic.createdAt}</span>
									<MessageSquare />
									<span>{topic.repliesCount} replies</span>
								</div>
							</div>
						</div>
						<div class="mb-4 flex gap-1">
							{#each topic.tags as tag (tag)}
								<span class="badge badge-outline">{tag}</span>
							{/each}
						</div>
					</div>
				</div>
				<div class="space-y-4">
					{#each topic.posts as post (post.id)}
						<ForumPost
							{...post}
							userData={{
								fullName: post.author,
								bio: 'Sample bio',
								interests: ['Tech'],
								userScore: 75
							}}
						/>
					{/each}
				</div>
			</div>
		</div>
	</div>
{/if}
