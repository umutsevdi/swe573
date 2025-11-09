import { SERVER_URL } from '$lib/config';

/**
 * Loads an avatar image for a user.
 * @param id - The user ID.
 * @param token - The authentication token.
 * @returns The blob URL of the avatar or null if not found.
 */
export async function loadAvatar(id: number, token: string): Promise<string | null> {
	const response = await fetch(`${SERVER_URL}/assets/profile/${id}`, {
		headers: {
			Authorization: `Bearer ${token}`
		}
	});

	if (!response.ok) {
		if (response.status === 404) {
			return null; // No avatar
		}
		throw new Error('Failed to load avatar');
	}

	const blob = await response.blob();
	const blobUrl = URL.createObjectURL(blob);
	return blobUrl;
}
