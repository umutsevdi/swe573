import { SERVER_URL } from '$lib/config';
import { redirect, type Cookies } from '@sveltejs/kit';
import { fetchWithAuth } from '$lib/utils/fetch';

type ForumThread = {
	id: number;
	title: string;
	active: boolean;
	description: string;
	createdAt: string;
	ownerId: number;
	tagsList: string[];
};

export const load = async ({ cookies }: { cookies: Cookies }) => {
	const token = cookies.get('token');

	const response = await fetchWithAuth(`${SERVER_URL}/thread`, token, cookies);

	const threads: ForumThread[] = await response.json();

	// Transform tagsList: split by comma and flatten
	const transformedThreads = threads.map((thread) => ({
		...thread,
		tags: thread.tagsList.flatMap((tag) => tag.split(',')).map((t) => t.trim())
	}));

	return { threads: transformedThreads };
};

export const actions = {
	create: async ({ request, cookies }: { request: Request; cookies: Cookies }) => {
		const token = cookies.get('token');
		const data = await request.formData();
		const title = data.get('title') as string;
		const description = data.get('description') as string;
		const tags = JSON.parse(data.get('tags') as string) as string[];

		if (!title?.trim() || !description?.trim()) {
			return { success: false, error: 'Title and description are required' };
		}

		try {
			const response = await fetch(`${SERVER_URL}/thread`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${token}`,
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({ title: title.trim(), description: description.trim(), tags })
			});

			if (!response.ok) {
				let errorMessage = `Failed to create thread (${response.status})`;
				try {
					const errorData = await response.json();
					if (errorData.message) {
						errorMessage = errorData.message;
					}
				} catch {
					// Use status text if can't parse
				}
				throw new Error(errorMessage);
			}

			const newThread = await response.json();

			// Redirect to the new thread
			throw redirect(302, `/forum/${newThread.id}`);
		} catch (err) {
			console.error('Error creating thread:', err);
			const errorMessage = err instanceof Error ? err.message : 'Failed to create thread';
			return { success: false, error: errorMessage };
		}
	}
};
