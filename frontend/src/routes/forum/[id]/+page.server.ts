import { SERVER_URL } from '$lib/serverConfig';
import { type Cookies } from '@sveltejs/kit';
import { loadWithAuth } from '$lib/utils/fetch';

type ThreadMessage = {
	message: string;
	ownerId: number;
	createdAt: string;
};

export const load = async ({ params, cookies }: { params: { id: string }; cookies: Cookies }) => {
	console.log('Fetching thread:', `${SERVER_URL}/thread/${params.id}`);
	const thread = await loadWithAuth(`${SERVER_URL}/thread/${params.id}`, cookies);

	// Transform the server response to match the expected format
	const parsedPosts = thread.messages.map((msg: ThreadMessage, index: number) => {
		let content = msg.message.trim();

		if (content.startsWith('>>')) {
			const parts = content.split('\n\n');
			if (parts.length >= 2) {
				const quoted = parts[0].replace(/^>>\s*/, '');
				const reply = parts.slice(1).join('\n\n');
				// Find the parent message
				for (let i = index - 1; i >= 0; i--) {
					if (thread.messages[i].message.trim() === quoted) {
						content = reply;
						break;
					}
				}
			}
		}

		return {
			userId: msg.ownerId,
			content,
			timestamp: new Date(msg.createdAt).toLocaleString()
		};
	});
	return {
		id: thread.id,
		title: thread.title,
		description: thread.description,
		createdAt: new Date(thread.createdAt).toLocaleDateString(),
		repliesCount: thread.messages.length,
		tags: thread.tags,
		posts: parsedPosts,
		userId: thread.ownerId,
		active: thread.active
	};
};
