import { type Cookies } from '@sveltejs/kit';
import { fetchWithAuth } from './fetch';

/**
 * Validates that required fields are present and not empty.
 * @param fields - Object with field names and values.
 * @returns Error message or null if valid.
 */
export function validateRequired(fields: Record<string, any>): string | null {
	for (const [key, value] of Object.entries(fields)) {
		if (!value || (typeof value === 'string' && !value.trim())) {
			return `${key} is required`;
		}
	}
	return null;
}

/**
 * Submits data to a URL with authentication.
 * @param url - The URL to post to.
 * @param data - The data to send.
 * @param cookies - The cookies for auth.
 * @returns The response JSON.
 */
export async function submitWithAuth(url: string, data: any, cookies: Cookies): Promise<any> {
	const response = await fetchWithAuth(url, cookies.get('token')!, cookies, {
		method: 'POST',
		body: JSON.stringify(data)
	});
	return response.json();
}
