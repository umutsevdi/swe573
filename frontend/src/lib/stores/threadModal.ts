import { writable } from 'svelte/store';

export const threadModal = writable({
	open: false
});
