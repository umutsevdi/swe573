import type { PageServerLoad } from './$types';

import { calculateDistance } from '$lib/utils/geo';

const EVENTS: Array<{
	id: string;
	title: string;
	description: string;
	ownerUrl: string;
	ownerProfileUrl: string;
	ownerId: number;
	tags: string[];
	latitude: number;
	longitude: number;
	location: string;
	participantCount: number;
	date: string;
	type: 'Offer' | 'Need';
}> = [
	{
		id: '1',
		title: 'Free bike for anyone',
		description: 'Free bike for anyone',
		tags: ['bike', 'free', 'outdoor'],
		location: 'Berlin, DE',
		latitude: 52.52,
		longitude: 13.405,
		type: 'Offer' as const,
		ownerUrl: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
		ownerProfileUrl: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
		ownerId: 1,
		participantCount: 4,
		date: '2024-12-15'
	},
	{
		id: '2',
		title: 'Need a tutor for calculus',
		description: 'Free bike for anyone',
		tags: ['tutoring', 'math', 'college'],
		location: 'Munich, DE',
		latitude: 48.1351,
		ownerId: 2,
		longitude: 11.582,
		type: 'Need' as const,
		ownerUrl: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
		ownerProfileUrl: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
		participantCount: 0,
		date: '2024-12-20'
	},
	{
		id: '3',
		title: 'Need a tutor for english',
		description: 'Free bike for anyone',
		tags: ['tutoring', 'english', 'college'],
		location: 'Turkey, TR',
		latitude: 39.9334,
		longitude: 32.8597,
		type: 'Need' as const,
		ownerUrl: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
		ownerProfileUrl: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
		ownerId: 3,
		participantCount: 0,
		date: '2024-12-25'
	},
	{
		id: '4',
		title: 'Free bookshelf donation',
		description: 'Solid wood bookshelf, 5 shelves, no scratches. Pick‑up only.',
		tags: ['furniture', 'free', 'bookshelf'],
		location: 'Turkey, TR',
		type: 'Offer',
		participantCount: 0,
		ownerUrl: '/user/1',
		ownerProfileUrl:
			'https://images.unsplash.com/photo-1595290293434-555d42427e84?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=687',
		ownerId: 4,
		latitude: 39.9334,
		longitude: 32.8597,
		date: '2025-12-25'
	},
	{
		id: '5',
		title: 'Garden tools giveaway',
		description:
			'Hand tools (shovel, rake, pruning shears) in good condition, free to anyone interested.',
		tags: ['garden', 'tools', 'free'],
		location: 'Hamburg, DE',
		latitude: 49.1351,
		longitude: 12.582,
		type: 'Offer',
		participantCount: 2,
		ownerUrl: '/user/1',
		ownerProfileUrl:
			'https://images.unsplash.com/photo-1595290293434-555d42427e84?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=687',
		ownerId: 5,
		date: '2025-12-25'
	}
];
export const load: PageServerLoad = async ({ url }) => {
	let events = EVENTS;
	const searchParams = url.searchParams;
	if (searchParams.has('q')) {
		const text = searchParams.get('q')!.toLowerCase();
		events = events.filter(
			(e) => e.description.toLowerCase().includes(text) || e.title.toLowerCase().includes(text)
		);
	}

	if (searchParams.has('tags')) {
		const tags: Array<string> = searchParams.get('tags')!.split(',').filter(Boolean);
		events = events.filter((event) => tags.some((tag) => event.tags.includes(tag)));
	}

	if (searchParams.has('type')) {
		const type = searchParams.get('type');
		if (type && type !== 'all') {
			events = events.filter((event) => event.type === type);
		}
	}

	if (searchParams.has('user_lat') && searchParams.has('user_lng')) {
		const userLat = parseFloat(searchParams.get('user_lat') || '');
		const userLng = parseFloat(searchParams.get('user_lng') || '');
		const proximityKm = parseInt(searchParams.get('proximity') || '50');

		if (!isNaN(userLat) && !isNaN(userLng)) {
			events = events.filter((event) => {
				const distance = calculateDistance(userLat, userLng, event.latitude, event.longitude);
				return distance <= proximityKm;
			});
		}
	}
	const start = searchParams.get('start');
	const end = searchParams.get('end');
	if (start || end) {
		events = events.filter((event) => {
			const eventDate = new Date(event.date);
			if (start) {
				const startDate = new Date(start);
				if (eventDate < startDate) return false;
			}
			if (end) {
				const endDate = new Date(end);
				if (eventDate > endDate) return false;
			}
			return true;
		});
	}
	return { events };
};
