import { browser } from '$app/environment';

declare global {
	interface Window {
		env: {
			CLIENT_BASE_URL: string;
		};
	}
}

export const CLIENT_BASE_URL = browser ? window.env.CLIENT_BASE_URL : 'http://localhost:8080';
