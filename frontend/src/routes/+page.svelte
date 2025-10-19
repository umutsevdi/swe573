<script lang="ts">
	import SearchResultCard from '$lib/components/SearchResultCard.svelte';
	import '../app.css';
	import { Apple, Skull, CircleAlert, Phone } from '@lucide/svelte';

	const cardsData: Array<{
		title: string;
		description: string;
		tags: string[];
		location: string;
		ownerUrl: string;
		ownerProfileUrl: string;
		participantCount: number;
		type?: 'Offer' | 'Need';
	}> = [
		{
			title: 'Free bike for anyone',
			description: 'Free bike for anyone',
			tags: ['bike', 'free', 'outdoor'],
			location: 'Berlin, DE',
			type: 'Offer',
			ownerUrl: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
			ownerProfileUrl: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
			participantCount: 4
		},
		{
			title: 'Need a tutor for calculus',
			description: 'Free bike for anyone',
			tags: ['tutoring', 'math', 'college'],
			location: 'Munich, DE',
			type: 'Need',
			ownerUrl: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
			ownerProfileUrl: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
			participantCount: 0
		},
		{
			title: 'Need a tutor for english',
			description: 'Free bike for anyone',
			tags: ['tutoring', 'english', 'college'],
			location: 'Turkey, TR',
			type: 'Need',
			ownerUrl: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
			ownerProfileUrl: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
			participantCount: 0
		}
	];
</script>

<div class="hero min-h-screen bg-base-200">
	<div class="hero-content flex-col lg:flex-row-reverse">
		<img
			src="https://img.daisyui.com/images/stock/photo-1635805737707-575885ab0820.webp"
			class="max-w-sm rounded-lg shadow-2xl"
		/>
		<div>
			<h1 class="text-5xl font-bold">Box Office News!</h1>
			<p class="py-6">
				Provident cupiditate voluptatem et in. Quaerat fugiat ut assumenda excepturi exercitationem
				quasi. In deleniti eaque aut repudiandae et a id nisi.
			</p>
			<h1>Hello</h1>
			<Apple />
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Laborum debitis dolores est
				voluptates? Excepturi quis architecto sequi exercitationem molestias ut praesentium facilis
				nemo illo? Harum incidunt porro fuga nam doloribus.
			</p>
			<CircleAlert color="#ff3e98" />
			<Phone fill="#333" />
			<Skull />
			<button class="btn btn-primary">Get Started</button>

			<ul class="list rounded-box bg-base-100 shadow-md">
				{#if cardsData.length}
					{#each cardsData as card (card.title)}
						<!-- Spread the object into the component props -->
						<SearchResultCard {...card} />
					{/each}
				{:else}
					<p>No cards to display.</p>
				{/if}
			</ul>
		</div>
	</div>
</div>
