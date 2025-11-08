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
				<h1 class="mb-5 text-5xl font-bold text-warning">
					{status} - {status === 404
						? 'Page Not Found'
						: status === 403
							? 'Forbidden'
							: status === 401
								? 'Unauthorized'
								: 'Client Error'}
				</h1>
				<p class="mb-5 text-lg">
					{#if status === 404}
						The page you're looking for doesn't exist or has been moved.
					{:else if status === 403}
						You don't have permission to access this resource.
					{:else if status === 401}
						You need to be logged in to access this resource.
					{:else}
						A client error occurred.
					{/if}
				</p>
				<p class="mb-5 text-sm opacity-75">
					{error?.message || 'An error occurred'}
				</p>
			{:else if isServerError}
				<div class="mb-5 flex justify-center">
					<AlertTriangle class="h-32 w-32 text-error" />
				</div>
				<h1 class="mb-5 text-5xl font-bold text-error">{status} - Server Error</h1>
				<p class="mb-5 text-lg">Something went wrong on our end. Please try again later.</p>
				<p class="mb-5 text-sm opacity-75">
					{error?.message || 'Internal server error'}
				</p>
			{:else}
				<div class="mb-5 flex justify-center">
					<AlertTriangle class="h-32 w-32 text-base-content" />
				</div>
				<h1 class="mb-5 text-5xl font-bold">{status} - Error</h1>
				<p class="mb-5 text-lg">An unexpected error occurred.</p>
				<p class="mb-5 text-sm opacity-75">
					{error?.message || 'Unknown error'}
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
