<script lang="ts">
	import { page } from '$app/stores';
	import { resolve } from '$app/paths';
	import { AlertTriangle, Home, RefreshCw } from '@lucide/svelte';

	let status = $derived($page.status);
	let error = $derived($page.error);

	let isClientError = $derived(status >= 400 && status < 500);
	let isServerError = $derived(status >= 500);
</script>

<div class="hero min-h-screen bg-base-200">
	<div class="hero-content text-center">
		<div class="max-w-md">
			{#if isClientError}
				<div class="mb-5 flex justify-center">
					<AlertTriangle class="h-32 w-32 text-warning" />
				</div>
				<h1 class="mb-5 text-5xl font-bold text-warning">Oops!</h1>
				<p class="mb-5 text-lg">The page you're looking for doesn't exist or has been moved.</p>
				<p class="mb-5 text-sm opacity-75">
					Error {status}: {error?.message || 'Page not found'}
				</p>
			{:else if isServerError}
				<div class="mb-5 flex justify-center">
					<AlertTriangle class="h-32 w-32 text-error" />
				</div>
				<h1 class="mb-5 text-5xl font-bold text-error">Server Error</h1>
				<p class="mb-5 text-lg">Something went wrong on our end. Please try again later.</p>
				<p class="mb-5 text-sm opacity-75">
					Error {status}: {error?.message || 'Internal server error'}
				</p>
			{:else}
				<div class="mb-5 flex justify-center">
					<AlertTriangle class="h-32 w-32 text-base-content" />
				</div>
				<h1 class="mb-5 text-5xl font-bold">Something went wrong</h1>
				<p class="mb-5 text-lg">An unexpected error occurred.</p>
				<p class="mb-5 text-sm opacity-75">
					Error {status}: {error?.message || 'Unknown error'}
				</p>
			{/if}

			<div class="flex justify-center gap-4">
				<a href={resolve('/')} class="btn cursor-pointer btn-primary">
					<Home class="mr-2 h-4 w-4" />
					Go Home
				</a>
				<button class="btn btn-outline" onclick={() => window.location.reload()}>
					<RefreshCw class="mr-2 h-4 w-4" />
					Refresh
				</button>
			</div>
		</div>
	</div>
</div>
