import { HIVE_API } from '$lib/serverConfig';

export async function fallback({
	request,
	params
}: {
	request: Request;
	params: { path: string[] };
}) {
	const path = params.path.join('/');
	const targetUrl = `${HIVE_API}/${path}`;

	// Clone the request to modify headers
	const requestInit: RequestInit = {
		method: request.method,
		headers: new Headers(request.headers)
	};

	// Remove host header to avoid issues
	(requestInit.headers as Headers).delete('host');

	// Add body for non-GET/HEAD requests
	if (request.method !== 'GET' && request.method !== 'HEAD') {
		requestInit.body = request.body;
	}

	const response = await fetch(targetUrl, requestInit);

	// Clone the response to return it
	const responseBody = response.body ? await response.arrayBuffer() : null;

	return new Response(responseBody, {
		status: response.status,
		statusText: response.statusText,
		headers: response.headers
	});
}
