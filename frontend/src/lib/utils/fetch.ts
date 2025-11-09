import { error, redirect, type Cookies } from '@sveltejs/kit';

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
		const body = await response.text();
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
		throw error(response.status, `Failed to fetch: ${response.statusText}`);
	}

	return response;
}

export async function fetchAuthenticatedImage(url: string, token: string): Promise<string | null> {
	try {
		const response = await fetch(url, {
			headers: {
				Authorization: `Bearer ${token}`
			}
		});

		if (response.ok) {
			const blob = await response.blob();
			return URL.createObjectURL(blob);
		} else {
			console.error(`Failed to load image: ${url} - ${response.status}`);
			return null;
		}
	} catch (error) {
		console.error('Failed to load image:', error);
		return null;
	}
}
