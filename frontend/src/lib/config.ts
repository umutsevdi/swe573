import { browser } from '$app/environment';

declare global {
	interface Window {
		env: {
			CLIENT_BASE_URL: string;
		};
	}
}

export const CLIENT_BASE_URL = browser ? window.env.CLIENT_BASE_URL : 'http://localhost:8080';

console.log('CLIENT_BASE_URL:', CLIENT_BASE_URL);
