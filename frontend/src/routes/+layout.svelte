<script lang="ts">
	import '../app.css';
	import CreatePostModal from '$lib/components/CreatePostModal.svelte';
	import UserAvatar from '$lib/components/UserAvatar.svelte';
	import favicon from '$lib/assets/favicon.svg';
	import { BookOpen, Github, Plus, Search, Menu, MessageSquare, Inbox } from '@lucide/svelte';

	let { children } = $props();

	let createPostModal: any;
	let modalOpen = $state(false);

	function openCreatePostModal() {
		createPostModal?.openModal();
	}

	function handlePostSubmit(event: CustomEvent) {
		console.log('New post submitted:', event.detail);
		// TODO: Handle the post submission (send to API, etc.)
	}
</script>

<CreatePostModal bind:this={createPostModal} bind:open={modalOpen} on:submit={handlePostSubmit} />

<div class="flex min-h-screen flex-col">
	<!-- Navbar -->
	<div class="navbar w-full bg-base-300">
		<div class="flex-1"><a class="btn text-xl btn-ghost" href="/#">Hive</a></div>
		<div class="flex-none">
			<ul class="menu menu-horizontal">
				<!-- Navbar menu content here -->
				<li><a href="/forum" class="btn btn-ghost"><MessageSquare class="h-4 w-4" /> Forum</a></li>
				<li><a href="/inbox" class="btn btn-ghost"><Inbox class="h-4 w-4" /> Inbox</a></li>
			</ul>
		</div>
		<div class="flex">
			<ul class="menu menu-horizontal">
				<li>
					<details>
						<summary>
							<div class="btn avatar btn-circle btn-ghost">
								<div class="w-10 rounded-full">
									<img
										alt="Tailwind CSS Navbar component"
										src="https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp"
									/>
								</div>
							</div>
						</summary>
						<ul class="rounded-t-none bg-base-100 p-2">
							<li>
								<button class="btn w-full text-left btn-ghost"> Profile </button>
							</li>
							<li><button class="btn w-full text-left btn-ghost">Settings</button></li>
							<li><button class="btn w-full text-left btn-ghost">Logout</button></li>
						</ul>
					</details>
				</li>
			</ul>
		</div>
	</div>
	<!-- Page content here -->
	{@render children?.()}

	<footer class="footer items-center bg-neutral p-4 text-neutral-content sm:footer-horizontal">
		<aside class="grid-flow-col items-center">
			<p>Copyright © {new Date().getFullYear()} - All right reserved</p>
		</aside>

		<nav class="grid-flow-col gap-4 md:place-self-center md:justify-self-end">
			<ul tabindex="-1" class="menu menu-vertical lg:menu-horizontal">
				<li>
					<a
						class="tooltip tooltip-left"
						href="https://github.com/umutsevdi/swe573/wiki"
						target="_blank"
						data-tip="Application documentation."
					>
						<BookOpen />
					</a>
				</li>
				<li>
					<a
						class="tooltip tooltip-left"
						href="https://github.com/umutsevdi/swe573"
						target="_blank"
						data-tip="Source code is available at GitHub."
					>
						<Github />
					</a>
				</li>
			</ul>
		</nav>
	</footer>

	<!-- Floating Action Button -->
	<button
		class="btn fixed right-4 bottom-4 z-10 btn-circle btn-primary"
		onclick={openCreatePostModal}
	>
		<Plus class="h-6 w-6" />
	</button>
</div>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>
