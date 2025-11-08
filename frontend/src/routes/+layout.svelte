<script lang="ts">
	import '../app.css';
	import { resolve } from '$app/paths';
	import CreatePostModal from '$lib/components/CreatePostModal.svelte';
	import Toast from '$lib/components/Toast.svelte';
	import favicon from '$lib/assets/favicon.svg';
	import { BookOpen, Github, Plus, MessageSquare, Inbox, User } from '@lucide/svelte';
	import { auth } from '$lib/stores/auth';
	import { goto } from '$app/navigation';
	import { SERVER_URL } from '$lib/config';

	let { children } = $props();

	let createPostModal: unknown;
	let modalOpen = $state(false);
	let navbarAvatarUrl = $state<string | null>(null);

	function openCreatePostModal() {
		createPostModal?.openModal();
	}

	function handlePostSubmit(event: CustomEvent) {
		console.log('New post submitted:', event.detail);
		// TODO: Handle the post submission (send to API, etc.)
	}

	async function loadNavbarAvatar() {
		if (!$auth.userCard?.id || !$auth.token) return;

		try {
			const response = await fetch(`${SERVER_URL}/assets/profile/${$auth.userCard.id}`, {
				headers: {
					Authorization: `Bearer ${$auth.token}`
				}
			});

			if (response.ok) {
				const blob = await response.blob();
				navbarAvatarUrl = URL.createObjectURL(blob);
			} else if (response.status === 401) {
				// Token invalid, logout
				auth.logout();
				goto(resolve('/auth'));
			}
		} catch (error) {
			console.error('Failed to load navbar avatar:', error);
		}
	}

	function handleLogout() {
		auth.logout();
		goto(resolve('/auth'));
	}

	// Load navbar avatar when user logs in
	$effect(() => {
		if ($auth.isLoggedIn && $auth.userCard?.id && $auth.token) {
			loadNavbarAvatar();
		}

		// Cleanup blob URL on unmount
		return () => {
			if (navbarAvatarUrl) {
				URL.revokeObjectURL(navbarAvatarUrl);
			}
		};
	});
</script>

<CreatePostModal bind:this={createPostModal} bind:open={modalOpen} on:submit={handlePostSubmit} />

<div class="flex min-h-screen flex-col">
	<!-- Navbar -->
	<div class="navbar w-full bg-base-300">
		<div class="flex-1"><a href={resolve('/')} class="btn text-xl btn-ghost">Hive</a></div>
		<div class="flex-none">
			<ul class="menu menu-horizontal">
				<!-- Navbar menu content here -->
				{#if $auth.isLoggedIn}
					<li>
						<a href={resolve('/forum')} class="btn cursor-pointer btn-ghost"
							><MessageSquare class="h-4 w-4" /> Forum</a
						>
					</li>
					<li>
						<a href={resolve('/inbox')} class="btn cursor-pointer btn-ghost"
							><Inbox class="h-4 w-4" /> Inbox</a
						>
					</li>
				{/if}
			</ul>
		</div>
		<div class="flex">
			<ul class="menu menu-horizontal">
				{#if $auth.isLoggedIn}
					<li>
						<details>
							<summary>
								<div class="btn avatar btn-circle btn-ghost">
									<div class="w-10 rounded-full">
										{#if navbarAvatarUrl}
											<img alt="User avatar" src={navbarAvatarUrl} />
										{:else}
											<div
												class="flex h-full items-center justify-center bg-neutral text-neutral-content"
											>
												<User class="h-6 w-6" />
											</div>
										{/if}
									</div>
								</div>
							</summary>
							<ul class="rounded-t-none bg-base-100 p-2">
								<li>
									<a
										href={resolve('/user/' + $auth.userCard?.id)}
										class="btn w-full text-left btn-ghost"
									>
										Profile
									</a>
								</li>
								<li><button class="btn w-full text-left btn-ghost">Settings</button></li>
								<li>
									<button class="btn w-full text-left btn-ghost" onclick={handleLogout}
										>Logout</button
									>
								</li>
							</ul>
						</details>
					</li>
				{:else}
					<li>
						<a href={resolve('/auth')} class="btn btn-ghost">Login</a>
					</li>
				{/if}
			</ul>
		</div>
	</div>
	<!-- Page content here -->
	{@render children?.()}

	<Toast />

	<footer class="footer items-center bg-neutral p-4 text-neutral-content sm:footer-horizontal">
		<aside class="grid-flow-col items-center">
			<p>Copyright © {new Date().getFullYear()} - All right reserved</p>
		</aside>

		<nav class="grid-flow-col justify-center gap-4 md:place-self-center md:justify-self-end">
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
	{#if $auth.isLoggedIn}
		<button
			class="btn fixed right-4 bottom-4 z-10 btn-circle btn-primary"
			onclick={openCreatePostModal}
		>
			<Plus class="h-6 w-6" />
		</button>
	{/if}
</div>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>
