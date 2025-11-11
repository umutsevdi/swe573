import { redirect, type Handle } from '@sveltejs/kit';

export const handle: Handle = async ({ event, resolve }) => {
	const token = event.cookies.get('token');

	// If no token and not on auth page, redirect to auth
	if (!token && !event.url.pathname.startsWith('/auth')) {
		throw redirect(302, '/auth');
	}

	const response = await resolve(event);

	if (response.headers.get('content-type')?.includes('text/html')) {
		const body = await response.text();
		const clientBaseUrl = process.env.CLIENT_BASE_URL || 'http://localhost:8080';
		const modifiedBody = body.replace('%CLIENT_BASE_URL%', clientBaseUrl);
		return new Response(modifiedBody, {
			status: response.status,
			headers: response.headers
		});
	}

	return response;
};
