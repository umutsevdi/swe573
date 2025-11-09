import { writable } from 'svelte/store';
import { browser } from '$app/environment';
import { type UserCard } from './user';

export interface AuthState {
	token: string | null;
	userCard: UserCard | null;
	isLoggedIn: boolean;
}

function createAuthStore() {
	const initialState: AuthState = {
		token: null,
		userCard: null,
		isLoggedIn: false
	};

	const { subscribe, set, update } = writable<AuthState>(initialState);

	// Initialize from localStorage if in browser
	if (browser) {
		const token = localStorage.getItem('token');
		const userCardStr = localStorage.getItem('userCard');
		let userCard: UserCard | null = null;
		if (userCardStr) {
			try {
				userCard = JSON.parse(userCardStr);
			} catch (e) {
				console.error('Failed to parse userCard from localStorage', e);
			}
		}

		// Also set the cookie for server-side access
		if (token) {
			const expires = new Date();
			expires.setDate(expires.getDate() + 7);
			document.cookie = `token=${encodeURIComponent(token)}; expires=${expires.toUTCString()}; path=/; SameSite=Lax`;
		}

		set({
			token,
			userCard,
			isLoggedIn: !!token
		});
	}

	return {
		subscribe,
		login: (token: string, userCard: UserCard) => {
			localStorage.setItem('token', token);
			localStorage.setItem('userCard', JSON.stringify(userCard));

			// Set cookie for server-side access (expires in 7 days)
			if (browser) {
				const expires = new Date();
				expires.setDate(expires.getDate() + 7);
				const cookieValue = `token=${encodeURIComponent(token)}; expires=${expires.toUTCString()}; path=/; SameSite=Lax`;
				document.cookie = cookieValue;
			}

			set({ token, userCard, isLoggedIn: true });
		},
		logout: () => {
			localStorage.removeItem('token');
			localStorage.removeItem('userCard');

			// Clear cookie
			if (browser) {
				document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
			}

			set({ token: null, userCard: null, isLoggedIn: false });
		},
		update
	};
}

export const auth = createAuthStore();
