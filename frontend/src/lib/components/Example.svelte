<!-- src/lib/components/Example.svelte -->
<script>
	export let label = 'Click me';
</script>

<button>{label}</button>

<style>
	button {
		padding: 0.5rem 1rem;
		background: #0066ff;
		color: white;
		border: none;
		border-radius: 4px;
	}
</style>
