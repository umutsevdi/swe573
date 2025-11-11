<script lang="ts">
	import '../app.css';
	import { resolve } from '$app/paths';
	import { enhance } from '$app/forms';
	import CreatePostModal from '$lib/components/CreatePostModal.svelte';
	import ThreadModal from '$lib/components/ThreadModal.svelte';
	import Toast from '$lib/components/Toast.svelte';
	import favicon from '$lib/assets/favicon.svg';
	import {
		BookOpen,
		Github,
		Plus,
		LogOut,
		Settings,
		PanelRightOpen,
		MapPinned,
		Mails,
		Bell,
		MapPinPlus,
		MailPlus
	} from '@lucide/svelte';
	import { auth } from '$lib/stores/auth';
	import { toast } from '$lib/stores/toast';
	import { goto, invalidateAll } from '$app/navigation';
	import { CLIENT_BASE_URL } from '$lib/config';

	let { children } = $props();

	let createPostModal: unknown;
	let modalOpen = $state(false);
	let threadModalOpen = $state(false);
	let navbarAvatarUrl = $state<string | null>(null);

	function openCreatePostModal() {
		(createPostModal as any)?.openModal();
	}

	async function handleCreateThreadSubmit(
		event: CustomEvent<{ title: string; description: string; tags: string[] }>
	) {
		const { title, description, tags } = event.detail;

		try {
			const response = await fetch(`${CLIENT_BASE_URL}/thread`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${$auth.token}`
				},
				body: JSON.stringify({ title: title.trim(), description: description.trim(), tags })
			});

			if (!response.ok) {
				throw new Error(`Failed to create thread: ${response.statusText}`);
			}

			const result = await response.json();
			toast.success('Thread created successfully!');

			// Navigate to the new thread page
			goto(resolve(`/forum/${result.id}`));
			threadModalOpen = false;
		} catch (error) {
			console.error('Error creating thread:', error);
			toast.error(
				error instanceof Error ? error.message : 'Failed to create thread. Please try again.'
			);
		}
	}

	async function handlePostSubmit(event: CustomEvent) {
		try {
			const response = await fetch(`${CLIENT_BASE_URL}/event`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${$auth.token}`
				},
				body: JSON.stringify(event.detail)
			});

			if (!response.ok) {
				throw new Error(`Failed to create event: ${response.statusText}`);
			}

			const result = await response.json();
			toast.success('Event created successfully!');

			// Navigate to the new event page
			goto(resolve(`/event/${result.id}`));
		} catch (error) {
			console.error('Error creating event:', error);
			toast.error(
				error instanceof Error ? error.message : 'Failed to create event. Please try again.'
			);
		}
	}

	async function loadNavbarAvatar() {
		if (!$auth.userCard?.id || !$auth.token) return;

		try {
			const response = await fetch(`${CLIENT_BASE_URL}/assets/profile/${$auth.userCard.id}`, {
				headers: {
					Authorization: `Bearer ${$auth.token}`
				}
			});

			if (response.ok) {
				const blob = await response.blob();
				navbarAvatarUrl = URL.createObjectURL(blob);
			} else if (response.status === 404) {
				navbarAvatarUrl = '/assets/profile.png';
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

		// Cleanup blob URL on unmount (only if it's a blob URL)
		return () => {
			if (navbarAvatarUrl && navbarAvatarUrl.startsWith('blob:')) {
				URL.revokeObjectURL(navbarAvatarUrl);
			}
		};
	});
</script>

<CreatePostModal bind:this={createPostModal} bind:open={modalOpen} on:submit={handlePostSubmit} />

<ThreadModal mode="create" bind:open={threadModalOpen} on:submit={handleCreateThreadSubmit} />

<div class="drawer-open drawer">
	<input id="my-drawer-4" type="checkbox" class="drawer-toggle" />
	<div class="drawer-content">
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

		<!-- Speed Dial FAB for Events and Threads -->
		{#if $auth.isLoggedIn}
			<div class="fab fixed right-8 bottom-8 z-20 fab-flower">
				<!-- Trigger button -->
				<div tabindex="0" role="button" class="btn btn-circle btn-lg btn-info"><Plus /></div>
				<div class="tooltip tooltip-left" data-tip="Close">
					<div class="fab-close">
						<span class="btn btn-circle btn-lg btn-error">✕</span>
					</div>
				</div>

				<div class="tooltip tooltip-left" data-tip="Create a new Offer or Need">
					<button
						class="fab-main-action btn btn-circle btn-lg btn-primary"
						onclick={openCreatePostModal}
					>
						<MapPinPlus />
					</button>
				</div>
				<div></div>
				<div class="tooltip tooltip-left" data-tip="Create new thread">
					<button
						class="btn btn-circle btn-lg btn-secondary"
						onclick={() => (threadModalOpen = true)}
					>
						<MailPlus />
					</button>
				</div>
			</div>
		{/if}
	</div>

	<div class="drawer-side drawer-end is-drawer-close:overflow-visible">
		<label for="my-drawer-4" aria-label="close sidebar" class="drawer-overlay"></label>
		{#if $auth.isLoggedIn}
			<div
				class="bg-base-400 flex min-h-full flex-col items-start is-drawer-close:w-14 is-drawer-open:w-32"
			>
				<!-- Sidebar content here -->
				<ul class="menu w-full grow gap-4">
					<li>
						<a
							href={resolve('/')}
							class="is-drawer-close:tooltip is-drawer-close:tooltip-right"
							data-tip="Discover"
							><MapPinned class="h-6 w-6" />
							<span class="is-drawer-close:hidden">Discover</span>
						</a>
					</li>
					<li>
						<a
							href={resolve('/forum')}
							class="is-drawer-close:tooltip is-drawer-close:tooltip-right"
							data-tip="Forum"
							><Mails class="h-6 w-6" />
							<span class="is-drawer-close:hidden">Forum</span>
						</a>
					</li>
					<li>
						<a
							href={resolve('/inbox')}
							class="is-drawer-close:tooltip is-drawer-close:tooltip-right"
							data-tip="Inbox"
							><Bell class="h-6 w-6" />
							<span class="is-drawer-close:hidden">Inbox</span>
						</a>
					</li>
					<li>
						<a
							href={resolve(`/user/${$auth.userCard?.id}`)}
							class="is-drawer-close:tooltip is-drawer-close:tooltip-right"
							data-tip="Profile"
						>
							<div class="avatar">
								<div
									class="w-6 rounded-full ring-2 ring-primary ring-offset-2 ring-offset-base-100"
								>
									<img alt="Profile" src={navbarAvatarUrl} />
								</div>
							</div>
							<span class="is-drawer-close:hidden">Profile</span>
						</a>
					</li>
					<li>
						<a
							href={resolve('/inbox')}
							class="is-drawer-close:tooltip is-drawer-close:tooltip-right"
							data-tip="Settings"
							><Settings class="h-6 w-6" />
							<span class="is-drawer-close:hidden">Settings</span>
						</a>
					</li>
					<li>
						<button
							class="is-drawer-close:tooltip is-drawer-close:tooltip-right"
							data-tip="Logout"
							onclick={handleLogout}
						>
							<LogOut class="h-5 w-6" />
							<span class="is-drawer-close:hidden">Logout</span>
						</button>
					</li>
				</ul>

				<ul>
					<!-- button to open/close drawer -->
					<div class="m-2 is-drawer-close:tooltip is-drawer-close:tooltip-right" data-tip="Open">
						<label
							for="my-drawer-4"
							class="drawer-button btn btn-circle btn-ghost is-drawer-open:rotate-y-180"
						>
							<PanelRightOpen />
						</label>
					</div>
				</ul>
			</div>
		{/if}
	</div>
</div>
<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>
