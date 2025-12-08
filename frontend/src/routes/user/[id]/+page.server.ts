import type { PageServerLoad } from './$types';
import { error } from '@sveltejs/kit';
import { HIVE_API } from '$lib/serverConfig';
import { loadWithAuth } from '$lib/utils/fetch';

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
	frozenAmount: number;
	enabled: boolean;
	active: boolean;
	authorities: Array<{ authority: string }>;
	accountTerminated: boolean;
	accountNonLocked: boolean;
	accountNonExpired: boolean;
	credentialsNonExpired: boolean;
}

import { decodeJwtPayload } from '$lib/utils/jwt';

export const load: PageServerLoad = async ({ params, cookies }) => {
	const id = Number(params.id);
	if (!Number.isInteger(id) || id < 0) {
		console.log('Invalid user id:', id);
		throw error(404, 'User not found');
	}

	const userData: UserApiResponse = await loadWithAuth(`${HIVE_API}/user/${id}`, cookies);

	// Decode JWT to get current user id
	const token = cookies.get('token')!;
	const payload = decodeJwtPayload(token);
	const currentUserId = payload?.id;
	const mappedData = {
		fullName: userData.fullName,
		username: userData.username,
		biography: userData.biography,
		interests: userData.interests,
		isSelf: currentUserId === id,
		timeBank: userData.timeBank || 0, // Default to 0 if not available
		frozenAmount: userData.frozenAmount || 0, // Default to 0 if not available
		userScore: userData.userScore
	};
	console.log(mappedData);
	return mappedData;
};
