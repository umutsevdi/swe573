import type { PageServerLoad } from './$types';
import { error } from '@sveltejs/kit';
import { HIVE_API } from '$lib/serverConfig';
import { loadWithAuth } from '$lib/utils/fetch';

type ChatMessage = {
	id: string;
	author: string;
	avatar: string;
	content: string;
	timestamp: string;
};

type ParticipationRequest = {
	id: string;
	userId: number;
	userName: string;
	message: string;
	status:
		| 'SENT'
		| 'PENDING'
		| 'CREATED'
		| 'NEGOTIATING'
		| 'ACCEPTED'
		| 'REJECTED'
		| 'WITHDRAWN'
		| 'COMPLETED';
};

type EventData = {
	id: string;
	title: string;
	description: string;
	ownerId: number;
	tags: string[];
	location: string;
	coordinates: { lat: number; lng: number };
	type: 'OFFER' | 'NEED';
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

import { decodeJwtPayload } from '$lib/utils/jwt';

export const load: PageServerLoad = async ({ params, cookies }) => {
	const id = Number(params.id);
	console.log('Loading event:', id);

	if (!Number.isInteger(id) || id < 0) {
		console.log('Invalid event id:', id);
		throw error(404, 'Event not found');
	}

	const serverData: EventApiResponse = await loadWithAuth(`${HIVE_API}/event/${id}`, cookies);

	// Fetch participation data
	const participationData: Record<string, string> = await loadWithAuth(
		`${HIVE_API}/event/${id}/participation`,
		cookies
	);

	// Decode JWT to get current user id
	const token = cookies.get('token')!;
	const payload = decodeJwtPayload(token);
	const currentUserId = payload?.id;

	// Map participation data to ParticipationRequest array
	const participationRequests: ParticipationRequest[] = Object.entries(participationData).map(
		([userIdStr, status]) => ({
			id: userIdStr,
			userId: parseInt(userIdStr),
			userName: '', // Will be loaded by UserAvatar
			message: '',
			status: status as ParticipationRequest['status']
		})
	);

	// Map user participation status
	const userStatus = participationData[currentUserId?.toString() || '']?.toUpperCase();
	const statusMap: Record<string, 'none' | 'pending' | 'accepted' | 'rejected'> = {
		SENT: 'pending',
		PENDING: 'pending',
		CREATED: 'pending',
		NEGOTIATING: 'accepted',
		ACCEPTED: 'accepted',
		REJECTED: 'rejected',
		WITHDRAWN: 'none',
		COMPLETED: 'accepted'
	};
	const userParticipationStatus = userStatus ? statusMap[userStatus] || 'none' : 'none';

	const isOwner = currentUserId === serverData.ownerId;

	const mappedData: EventData = {
		id: serverData.id.toString(),
		title: serverData.title,
		description: serverData.description,
		ownerId: serverData.ownerId,
		tags: serverData.tags,
		location: serverData.location.name,
		coordinates: { lat: serverData.location.lat, lng: serverData.location.lon },
		type: serverData.type === 'OFFER' ? 'OFFER' : 'NEED',
		participantCount: 0, // Default, as not provided in server response
		maxParticipants: serverData.maxParticipants,
		createdAt: new Date(serverData.createdAt).toLocaleDateString(),
		updatedAt: serverData.updatedAt ? new Date(serverData.updatedAt).toLocaleDateString() : '',
		targetDate: serverData.eventDate,
		duration: serverData.duration ? `${serverData.duration} hour(s)` : undefined,
		capacity: serverData.maxParticipants,
		userParticipationStatus,
		participationRequests,
		chatMessages: [] // Default
	};

	return { event: mappedData, isOwner, currentUserId };
};
