<script lang="ts">
	import { toast } from '$lib/stores/toast';
	import { X } from '@lucide/svelte';
</script>

{#if $toast.length > 0}
	<div class="toast toast-end toast-top z-50">
		{#each $toast as t (t.id)}
			<div class="alert alert-{t.type} shadow-lg">
				<div>
					<span>{t.message}</span>
				</div>
				<div class="flex-none">
					<button class="btn btn-circle btn-ghost btn-sm" onclick={() => toast.remove(t.id)}>
						<X class="h-4 w-4" />
					</button>
				</div>
			</div>
		{/each}
	</div>
{/if}
