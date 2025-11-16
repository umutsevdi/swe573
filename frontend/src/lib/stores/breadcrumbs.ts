import { writable } from 'svelte/store';

export const breadcrumbs = writable<Array<{ text: string; href?: string }>>([{ text: 'Hive' }]);
