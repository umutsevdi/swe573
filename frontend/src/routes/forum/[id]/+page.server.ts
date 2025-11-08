import { SERVER_URL } from '$lib/config';
import { redirect, error, type Cookies } from '@sveltejs/kit';

type ThreadMessage = {
	message: string;
	ownerId: number;
	createdAt: string;
};

export const load = async ({ params, cookies }: { params: { id: string }; cookies: Cookies }) => {
	const token = cookies.get('token');

	try {
		console.log('Fetching thread:', `${SERVER_URL}/thread/${params.id}`);
		const response = await fetch(`${SERVER_URL}/thread/${params.id}`, {
			headers: {
				Authorization: `Bearer ${token}`
			}
		});

		console.log('API response status:', response.status);
		if (!response.ok) {
			if (response.status === 404) {
				throw error(404, 'Thread not found');
			}
			// Try to parse error response body
			let errorMessage = `Failed to fetch thread (${response.status})`;
			try {
				const errorData = await response.json();
				if (errorData.message) {
					errorMessage = errorData.message;
				} else if (errorData.error) {
					errorMessage = errorData.error;
				}
			} catch {
				// If we can't parse the error response, use the status text
				if (response.statusText) {
					errorMessage = `${response.status} ${response.statusText}`;
				}
			}
			throw new Error(errorMessage);
		}

		const thread = await response.json();

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
	} catch (err) {
		console.error('Error fetching thread:', err);
		const errorMessage = err instanceof Error ? err.message : 'Failed to load thread';

		// Check if it's a 404 (thread not found) and handle appropriately
		if (
			errorMessage.includes('404') ||
			errorMessage.toLowerCase().includes('not found') ||
			errorMessage.toLowerCase().includes('content not found')
		) {
			throw error(404, 'Thread not found');
		}

		// For other errors, throw a 500
		throw error(500, errorMessage);
	}
};

export const actions = {
	reply: async ({
		request,
		params,
		cookies
	}: {
		request: Request;
		params: { id: string };
		cookies: Cookies;
	}) => {
		const token = cookies.get('token');
		const data = await request.formData();
		const message = data.get('message') as string;

		if (!message?.trim()) {
			return { success: false, error: 'Message cannot be empty' };
		}

		const replyContent = message;

		try {
			const response = await fetch(`${SERVER_URL}/thread/${params.id}/reply`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${token}`,
					'Content-Type': 'text/plain'
				},
				body: replyContent
			});

			if (!response.ok) {
				if (response.status === 404) {
					return { success: false, error: 'Thread not found' };
				}
				// Try to parse error response body
				let errorMessage = `Failed to post reply (${response.status})`;
				try {
					const errorData = await response.json();
					if (errorData.message) {
						errorMessage = errorData.message;
					} else if (errorData.error) {
						errorMessage = errorData.error;
					}
				} catch {
					// If we can't parse the error response, use the status text
					if (response.statusText) {
						errorMessage = `${response.status} ${response.statusText}`;
					}
				}
				throw new Error(errorMessage);
			}

			const newMessage = await response.json();

			return {
				success: true,
				newMessage: {
					userId: newMessage.ownerId,
					content: newMessage.message,
					timestamp: new Date(newMessage.createdAt).toLocaleString()
				}
			};
		} catch (err) {
			console.error('Error posting reply:', err);
			const errorMessage = err instanceof Error ? err.message : 'Failed to post reply';
			return { success: false, error: errorMessage };
		}
	}
};
