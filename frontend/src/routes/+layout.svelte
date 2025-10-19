<script lang="ts">
	import '../app.css';
	import Example from '$lib/components/Example.svelte';
	import favicon from '$lib/assets/favicon.svg';
	import {
		BookOpen,
		Github,
		HandHelping,
		HeartHandshake,
		House,
		PanelLeftOpen,
		Plus,
		Search,
		Settings,
		Menu
	} from '@lucide/svelte';

	let { children } = $props();
</script>

<div class="fab">
	<!-- a focusable div with tabindex is necessary to work on all browsers. role="button" is necessary for accessibility -->
	<div
		tabindex="0"
		role="button"
		class="tooltip btn btn-circle btn-lg btn-primary"
		data-tip="New Post"
	>
		<Plus />
	</div>
	<!-- buttons that show up when FAB is open -->
	<button class="tooltip btn btn-circle btn-lg" data-tip="Share Offer">
		<HeartHandshake />
	</button>
	<button class="tooltip btn btn-circle btn-lg" data-tip="Share Need">
		<HandHelping />
	</button>
</div>
<dialog id="search-modal" class="modal">
	<div class="modal-box">
		<input type="text" placeholder="Search" class=" input text-lg md:w-full" />
		<ul
			tabindex="-1"
			class="dropdown-content menu z-1 mt-3 w-52 menu-sm rounded-box bg-base-100 p-2 shadow md:w-full"
		>
			<!-- Search List -->
			<li><a> Umut Sevdi <span class="badge badge-accent">User</span> </a></li>
			<li>
				<a> I need doctor <span class="badge badge-primary">Need</span> </a>
			</li>
			<li>
				<a>
					I can teach painting <span class="badge badge-secondary">Give</span>
				</a>
			</li>
		</ul>
		<p class="py-4">Press ESC key or click outside to close</p>
	</div>
	<form method="dialog" class="modal-backdrop">
		<button>close</button>
	</form>
</dialog>

<div class="drawer">
	<input id="my-drawer-2" type="checkbox" class="drawer-toggle" />
	<div class="drawer-content flex flex-col">
		<!-- Navbar -->
		<div class="navbar w-full bg-base-300">
			<div class="flex-none lg:hidden">
				<label for="my-drawer-2" aria-label="open sidebar" class="btn btn-square btn-ghost">
					<Menu />
				</label>
			</div>
			<div class="flex-1"><a class="btn text-xl btn-ghost" href="/#">Hive</a></div>
			<div class="hidden flex-none lg:block">
				<ul class="menu menu-horizontal">
					<!-- Navbar menu content here -->
					<li><a class="btn btn-ghost">Community</a></li>
					<li><a class="btn btn-ghost">Navbar Item 2</a></li>
				</ul>
			</div>
			<div class="flex">
				<ul class="menu menu-horizontal">
					<li>
						<button
							id="search-button"
							class="btn min-h-full btn-ghost"
							onclick={document.getElementById('search-modal').showModal()}
						>
							<Search />
						</button>
					</li>

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
									<a> Profile </a>
								</li>
								<li><a>Settings</a></li>
								<li><a>Logout</a></li>
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
	</div>
	<div class="drawer-side">
		<label for="my-drawer-2" aria-label="close sidebar" class="drawer-overlay"></label>
		<ul class="menu min-h-full w-80 bg-base-200 p-4">
			<!-- Sidebar content here -->
			<li><a>Sidebar Item 1</a></li>
			<li><a>Sidebar Item 2</a></li>
		</ul>
	</div>
</div>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>
