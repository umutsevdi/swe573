import { SERVER_URL } from '$lib/serverConfig';
import { type Cookies } from '@sveltejs/kit';
import { loadWithAuth } from '$lib/utils/fetch';
import { validateRequired, submitWithAuth } from '$lib/utils/actions';

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
	const threads: ForumThread[] = await loadWithAuth(`${SERVER_URL}/thread`, cookies);

	// Transform tagsList: split by comma and flatten
	const transformedThreads = threads.map((thread) => ({
		...thread,
		tags: thread.tagsList.flatMap((tag) => tag.split(',')).map((t) => t.trim())
	}));

	return { threads: transformedThreads };
};

export const actions = {
	create: async ({ request, cookies }: { request: Request; cookies: Cookies }) => {
		const data = await request.formData();
		const title = data.get('title') as string;
		const description = data.get('description') as string;
		const tags = JSON.parse(data.get('tags') as string) as string[];

		const validationError = validateRequired({ title, description });
		if (validationError) {
			return { success: false, error: validationError };
		}

		try {
			const result = await submitWithAuth(
				`${SERVER_URL}/thread`,
				{ title: title.trim(), description: description.trim(), tags },
				cookies
			);
			return { success: true, thread: result };
		} catch (error) {
			console.error('Error creating thread:', error);
			return { success: false, error: 'Failed to create thread' };
		}
	}
};
