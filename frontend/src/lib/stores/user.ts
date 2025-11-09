import { writable } from 'svelte/store';

export type UserCard = {
	id: number;
	fullName: string;
	biography: string;
	interests: string[];
	userScore: number;
};

// Global cache for user card data
export const userCardCache = writable<Map<number, UserCard>>(new Map());

// Pending promises to prevent duplicate fetches
export const userDataPromises = writable<Map<number, Promise<UserCard>>>(new Map());
export const avatarPromises = writable<Map<number, Promise<string>>>(new Map());

// Cached avatar URLs (blob URLs or static paths)
export const avatarUrls = writable<Map<number, string>>(new Map());
