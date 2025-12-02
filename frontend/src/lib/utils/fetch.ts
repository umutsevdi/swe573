import { error, redirect, type Cookies } from '@sveltejs/kit';

/**
 * Fetches a URL with authentication, handling common errors.
 * @param url - The URL to fetch.
 * @param token - The auth token.
 * @param cookies - The cookies for redirects.
 * @param options - Additional fetch options.
 * @returns The response.
 */
export async function fetchWithAuth(
	url: string,
	token: string,
	cookies: Cookies,
	options: RequestInit = {}
) {
	const response = await fetch(url, {
		...options,
		headers: {
			Authorization: `Bearer ${token}`,
			'Content-Type': 'application/json',
			...options.headers
		}
	});

	if (!response.ok) {
		const errorData = await response.json().catch(() => ({}));
		const body = JSON.stringify(errorData);
		console.error(
			`API Error: ${url} - ${response.status} ${response.statusText} - Payload: ${body}`
		);
		if (response.status === 401) {
			// Invalid token, clear it and redirect to login
			cookies.delete('token', { path: '/' });
			throw redirect(302, '/auth');
		}
		if (response.status === 404) {
			throw error(404, 'Not found');
		}
		throw error(response.status, errorData.message || `Failed to fetch: ${response.statusText}`);
	}

	return response;
}

/**
 * Loads JSON data from a URL with authentication.
 * @param url - The URL to fetch.
 * @param cookies - The cookies for auth and redirects.
 * @returns The JSON data.
 */
export async function loadWithAuth(url: string, cookies: Cookies): Promise<any> {
	const token = cookies.get('token');
	if (!token) {
		throw redirect(302, '/auth');
	}
	const response = await fetchWithAuth(url, token, cookies);
	return response.json();
}

/**
 * Client-side fetch with authentication, handling common errors.
 * @param url - The URL to fetch.
 * @param token - The auth token.
 * @param options - Additional fetch options.
 * @returns The response.
 */
export async function fetchWithAuthClient(url: string, token: string, options: RequestInit = {}) {
	const isFormData = options.body instanceof FormData;
	const response = await fetch(url, {
		...options,
		headers: {
			Authorization: `Bearer ${token}`,
			...(isFormData ? {} : { 'Content-Type': 'application/json' }),
			...options.headers
		}
	});

	if (!response.ok) {
		const errorData = await response.json().catch(() => ({}));
		const body = JSON.stringify(errorData);
		console.error(
			`API Error: ${url} - ${response.status} ${response.statusText} - Payload: ${body}`
		);
		throw new Error(errorData.message || `Failed to fetch: ${response.statusText}`);
	}

	return response;
}

export async function fetchAuthenticatedImage(
	url: string,
	token: string,
	fallback?: string
): Promise<string | null> {
	try {
		const response = await fetch(url, {
			headers: {
				Authorization: `Bearer ${token}`
			}
		});

		if (response.ok) {
			const blob = await response.blob();
			return URL.createObjectURL(blob);
		} else if (response.status === 404 && fallback) {
			return fallback;
		} else {
			console.error(`Failed to load image: ${url} - ${response.status}`);
			return null;
		}
	} catch (error) {
		console.error('Failed to load image:', error);
		return null;
	}
}
