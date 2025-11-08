import type { PageServerLoad } from './$types';
import { error } from '@sveltejs/kit';
import { SERVER_URL } from '$lib/config';

interface UserApiResponse {
	id: number;
	username: string;
	email: string;
	fullName: string;
	biography: string;
	interests: string[];
	role: string;
	createdAt: string;
	userScore: number;
	timeBank: number;
	enabled: boolean;
	active: boolean;
	authorities: Array<{ authority: string }>;
	accountTerminated: boolean;
	accountNonLocked: boolean;
	accountNonExpired: boolean;
	credentialsNonExpired: boolean;
}

interface OfferNeedItem {
	id: string;
	title: string;
	description: string;
	tags: string[];
	location: string;
	participantCount: number;
	type?: 'Offer' | 'Need';
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
	console.log('Loading user:', id);

	if (!Number.isInteger(id) || id < 0) {
		console.log('Invalid user id:', id);
		throw error(404, 'User not found');
	}

	const token = cookies.get('token');
	if (!token) {
		throw error(401, 'Unauthorized');
	}

	try {
		const response = await fetch(`${SERVER_URL}/user/${id}`, {
			headers: {
				Authorization: `Bearer ${token}`,
				'Content-Type': 'application/json'
			}
		});

		if (!response.ok) {
			if (response.status === 404) {
				throw error(404, 'User not found');
			}
			throw error(response.status, 'Failed to fetch user data');
		}

		const userData: UserApiResponse = await response.json();

		// Decode JWT to get current user id
		const payload = decodeJwtPayload(token);
		const currentUserId = payload?.id;

		const mappedData = {
			fullName: userData.fullName,
			bio: userData.biography,
			interests: userData.interests,
			profileUrl: `${SERVER_URL}/assets/profile/${id}`,
			backgroundUrl: `${SERVER_URL}/assets/bg/${id}`,
			isSelf: currentUserId === id,
			offers: [] as OfferNeedItem[], // API doesn't return offers/needs yet
			needs: [] as OfferNeedItem[], // API doesn't return offers/needs yet
			remainingTime: userData.timeBank || 0, // Default to 0 if not available
			userScore: userData.userScore
		};

		console.log('Returning user data:', mappedData);
		return mappedData;
	} catch (err) {
		console.error('Error fetching user data:', err);
		throw error(500, 'Internal server error');
	}
};
