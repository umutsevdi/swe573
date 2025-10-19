<script lang="ts">
	import { Search, Plus } from '@lucide/svelte';
	import ForumTopic from '$lib/components/ForumTopic.svelte';

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

	let { data } = $props<{ topics: ForumTopicData[] }>();
	let search = $state('');

	let topics = $derived(data.topics);
</script>

<div class="hero min-h-screen bg-base-200">
	<div class="hero-content w-full max-w-4xl flex-col">
		<h1 class="mb-6 text-4xl font-bold">Forum</h1>
		<div class="mb-6 flex w-full gap-4">
			<label class="input-bordered input flex flex-1 items-center gap-2">
				<Search />
				<input type="text" class="grow" placeholder="Search forum threads..." bind:value={search} />
			</label>
			<button class="btn btn-primary">
				<Plus />
				Create New Thread
			</button>
		</div>
		<div class="w-full">
			{#each topics.filter((topic: ForumTopicData) => topic.title
						.toLowerCase()
						.includes(search.toLowerCase()) || topic.tags.some((tag: string) => tag
							.toLowerCase()
							.includes(search.toLowerCase()))) as topic (topic.id)}
				<ForumTopic {topic} />
			{/each}
		</div>
	</div>
</div>
