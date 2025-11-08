import { writable } from 'svelte/store';

export type UserCardData = {
	id: number;
	fullName: string;
	biography: string;
	interests: string[];
	userScore: number;
};

// Global cache for user card data
export const userCardCache = writable<Map<number, UserCardData>>(new Map());
