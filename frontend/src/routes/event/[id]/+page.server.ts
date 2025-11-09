import type { PageServerLoad } from './$types';
import { error } from '@sveltejs/kit';
import { SERVER_URL } from '$lib/config';
import { fetchWithAuth } from '$lib/utils/fetch';

type ChatMessage = {
	id: string;
	author: string;
	avatar: string;
	content: string;
	timestamp: string;
};

type ParticipationRequest = {
	id: string;
	userId: string;
	userName: string;
	message: string;
	status: 'CREATED' | 'PENDING' | 'ACCEPTED' | 'REJECTED';
};

type EventData = {
	id: string;
	title: string;
	description: string;
	ownerId: number;
	tags: string[];
	location: string;
	coordinates: { lat: number; lng: number };
	type: 'Offer' | 'Need';
	participantCount: number;
	maxParticipants?: number;
	createdAt: string;
	updatedAt: string;
	targetDate?: string;
	duration?: string;
	capacity?: number;
	userParticipationStatus: 'none' | 'pending' | 'accepted' | 'rejected';
	participationRequests: ParticipationRequest[];
	chatMessages: ChatMessage[];
};

interface EventApiResponse {
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
	type: string;
	duration: number;
	eventDate: string;
	createdAt: string;
	updatedAt: string;
	ownerId: number;
}

function decodeJwtPayload(token: string): { id?: number } | null {
	try {
		const payload = token.split('.')[1];
		const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
		return JSON.parse(decoded);
	} catch (e) {
		console.error('Failed to decode JWT:', e);
		return null;
	}
}

export const load: PageServerLoad = async ({ params, cookies }) => {
	const id = Number(params.id);
	console.log('Loading event:', id);

	if (!Number.isInteger(id) || id < 0) {
		console.log('Invalid event id:', id);
		throw error(404, 'Event not found');
	}

	const token = cookies.get('token');
	if (!token) {
		throw error(401, 'Unauthorized');
	}

	const response = await fetchWithAuth(`${SERVER_URL}/event/${id}`, token, cookies);

	const serverData: EventApiResponse = await response.json();

	// Decode JWT to get current user id
	const payload = decodeJwtPayload(token);
	const currentUserId = payload?.id;

	const mappedData: EventData = {
		id: serverData.id.toString(),
		title: serverData.title,
		description: serverData.description,
		ownerId: serverData.ownerId,
		tags: serverData.tags,
		location: serverData.location.name,
		coordinates: { lat: serverData.location.lat, lng: serverData.location.lon },
		type: serverData.type === 'OFFER' ? 'Offer' : 'Need',
		participantCount: 0, // Default, as not provided in server response
		maxParticipants: serverData.maxParticipants,
		createdAt: new Date(serverData.createdAt).toLocaleDateString(),
		updatedAt: serverData.updatedAt ? new Date(serverData.updatedAt).toLocaleDateString() : '',
		targetDate: serverData.eventDate,
		duration: serverData.duration ? `${serverData.duration} hour(s)` : undefined,
		capacity: serverData.maxParticipants,
		userParticipationStatus: 'none', // Default
		participationRequests: [], // Default
		chatMessages: [] // Default
	};

	const isOwner = currentUserId === serverData.ownerId;

	return { event: mappedData, isOwner };
};
