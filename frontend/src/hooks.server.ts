import { redirect, type Handle } from '@sveltejs/kit';

export const handle: Handle = async ({ event, resolve }) => {
	const token = event.cookies.get('token');

	// If no token and not on auth page, redirect to auth
	if (!token && !event.url.pathname.startsWith('/auth')) {
		throw redirect(302, '/auth');
	}

	return resolve(event);
};
