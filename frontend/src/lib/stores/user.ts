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

// Pending promises to prevent duplicate fetches
export const userDataPromises = writable<Map<number, Promise<UserCardData>>>(new Map());
export const avatarPromises = writable<Map<number, Promise<Blob>>>(new Map());

// Cached avatar blobs
export const avatarBlobs = writable<Map<number, Blob>>(new Map());
