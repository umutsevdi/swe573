export const load = async ({ url }) => {
	const searchParams = url.searchParams;

	let events = [
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
			participantCount: 0,
			date: '2024-12-25'
		}
	];

	// Apply search filter
	const q = searchParams.get('q');
	if (q) {
		const query = q.toLowerCase();
		events = events.filter(
			(event) =>
				event.title.toLowerCase().includes(query) ||
				event.description.toLowerCase().includes(query) ||
				event.tags.some((tag) => tag.toLowerCase().includes(query))
		);
	}

	// Apply tag filter
	const tags = searchParams.get('tags');
	if (tags) {
		const tagList = tags.split(',').filter(Boolean);
		events = events.filter((event) => tagList.some((tag) => event.tags.includes(tag)));
	}

	// Apply date range filter
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

	// Apply type filter
	const type = searchParams.get('type');
	if (type && type !== 'all') {
		events = events.filter((event) => event.type === type);
	}

	// Apply sorting
	const sort = searchParams.get('sort') || 'date';
	const order = searchParams.get('order') || 'desc';
	events.sort((a, b) => {
		let comparison = 0;
		if (sort === 'date') {
			comparison = new Date(a.date).getTime() - new Date(b.date).getTime();
		} else if (sort === 'participants') {
			comparison = a.participantCount - b.participantCount;
		}
		return order === 'asc' ? comparison : -comparison;
	});

	return { events };
};
