import type { PageServerLoad } from './$types';
import { SERVER_URL } from '$lib/serverConfig';
import { fetchWithAuth } from '$lib/utils/fetch';
import type { Cookies } from '@sveltejs/kit';

interface EventFilterRequest {
	textSearch?: string;
	tags?: string[];
	userLocation?: {
		lat: number;
		lon: number;
	};
	proximity?: number;
	startDate?: string;
	endDate?: string;
}

interface ApiEvent {
	id: number;
	title: string;
	description: string;
	tags: string[];
	location: {
		lat: number;
		lon: number;
		name: string;
	};
	maxParticipants: number;
	type: 'OFFER' | 'NEED';
	duration: number;
	eventDate: string;
	createdAt: string;
	updatedAt: string;
	ownerId: number;
}

interface MappedEvent {
	id: string;
	title: string;
	description: string;
	tags: string[];
	location: string;
	latitude: number;
	longitude: number;
	participantCount: number;
	type: 'Offer' | 'Need';
	date: string;
	ownerId: number;
}

export const load: PageServerLoad = async ({ url, cookies }: { url: URL; cookies: Cookies }) => {
	const searchParams = url.searchParams;
	const requestBody: EventFilterRequest = {};

	if (searchParams.has('q')) {
		requestBody.textSearch = searchParams.get('q')!.trim();
	}

	if (searchParams.has('tags')) {
		requestBody.tags = searchParams.get('tags')!.split(',').filter(Boolean);
	}

	if (searchParams.has('start')) {
		requestBody.startDate = searchParams.get('start')!;
	}

	if (searchParams.has('end')) {
		requestBody.endDate = searchParams.get('end')!;
	}

	if (searchParams.has('user_lat') && searchParams.has('user_lng')) {
		const userLat = parseFloat(searchParams.get('user_lat') || '');
		const userLng = parseFloat(searchParams.get('user_lng') || '');
		if (!isNaN(userLat) && !isNaN(userLng)) {
			requestBody.userLocation = { lat: userLat, lon: userLng };
			if (searchParams.has('proximity')) {
				const proximity = parseInt(searchParams.get('proximity') || '0');
				if (!isNaN(proximity) && proximity != 0) {
					requestBody.proximity = proximity;
				}
			}
		}
	}

	try {
		console.log('Fetching events from:', `${SERVER_URL}/event/search`, 'with body:', requestBody);
		const response = await fetchWithAuth(
			`${SERVER_URL}/event/search`,
			cookies.get('token')!,
			cookies,
			{
				method: 'POST',
				body: JSON.stringify(requestBody)
			}
		);

		const apiEvents: ApiEvent[] = await response.json();
		console.log('Fetched events:', apiEvents.length);

		let events: MappedEvent[] = apiEvents.map((event) => ({
			id: event.id.toString(),
			title: event.title,
			description: event.description,
			tags: event.tags,
			location: event.location.name,
			latitude: event.location.lat,
			longitude: event.location.lon,
			participantCount: event.maxParticipants,
			type: event.type === 'OFFER' ? 'Offer' : 'Need',
			date: event.eventDate,
			ownerId: event.ownerId
		}));

		// Apply type filter if present (since API doesn't support it)
		if (searchParams.has('type')) {
			const type = searchParams.get('type');
			if (type && type !== 'all') {
				events = events.filter((event) => event.type === type);
			}
		}

		return { events };
	} catch (error) {
		console.error('Error fetching events:', error);
		// Fallback to empty array or mock data if needed
		return { events: [] };
	}
};
