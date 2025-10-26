type ChatMessage = {
	id: string;
	author: string;
	avatar: string;
	content: string;
	timestamp: string;
};

type EventData = {
	id: string;
	title: string;
	description: string;
	author: string;
	authorAvatar: string;
	tags: string[];
	backgroundImage: string;
	location: string;
	coordinates: { lat: number; lng: number };
	type: 'Offer' | 'Need';
	participantCount: number;
	maxParticipants?: number;
	createdAt: string;
	targetDate?: string;
	duration?: string;
	capacity?: number;
	userParticipationStatus: 'none' | 'pending' | 'accepted' | 'rejected';
	participationRequests: {
		id: string;
		userId: string;
		userName: string;
		message: string;
		status: 'CREATED' | 'PENDING' | 'ACCEPTED' | 'REJECTED';
	}[];
	chatMessages: ChatMessage[];
};

const mockEvents: EventData[] = [
	{
		id: '1',
		title: 'Free Mountain Bike for City Commuting',
		description:
			'A gently used mountain bike perfect for city commuting or weekend rides. The bike is in excellent condition with new tires and brakes. It has been well-maintained and is ready for its next adventure. Perfect for someone looking to reduce their carbon footprint while getting around town efficiently.',
		author: 'Dr. Alex Rivera',
		authorAvatar:
			'https://images.unsplash.com/photo-1587778082149-bd5b1bf5d3fa?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxjb2xsZWN0aW9uLXBhZ2V8MXw5NDUyNDk0fHxlbnwwfHx8fHw%3D&auto=format&fit=crop&q=60&w=400',
		tags: ['bike', 'free', 'outdoor', 'transportation'],
		backgroundImage:
			'https://images.unsplash.com/photo-1485965120184-e220f721d03e?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1000&q=80',
		location: 'Berlin, Germany',
		coordinates: { lat: 52.52, lng: 13.405 },
		type: 'Offer',
		participantCount: 4,
		maxParticipants: 1,
		createdAt: '2024-10-20',
		targetDate: '2024-10-25',
		duration: '1 hour',
		userParticipationStatus: 'none',
		participationRequests: [],
		chatMessages: [
			{
				id: '1',
				author: 'BikeEnthusiast',
				avatar:
					'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=100&q=80',
				content: "Is the bike still available? I'm very interested!",
				timestamp: '2024-10-21 10:30 AM'
			},
			{
				id: '2',
				author: 'Dr. Alex Rivera',
				avatar:
					'https://images.unsplash.com/photo-1587778082149-bd5b1bf5d3fa?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxjb2xsZWN0aW9uLXBhZ2V8MXw5NDUyNDk0fHxlbnwwfHx8fHw%3D&auto=format&fit=crop&q=60&w=400',
				content:
					"Yes, it's still available! The bike is currently at my apartment. When would be a good time for pickup?",
				timestamp: '2024-10-21 11:15 AM'
			},
			{
				id: '3',
				author: 'BikeEnthusiast',
				avatar:
					'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=100&q=80',
				content: "Great! I'm free this weekend. Does Saturday afternoon work?",
				timestamp: '2024-10-21 2:45 PM'
			}
		]
	},
	{
		id: '2',
		title: 'Need Help with Calculus Tutoring',
		description:
			"I'm struggling with integral and differential calculus concepts for my university course. Looking for someone patient who can explain things clearly and help me understand the fundamentals. I'm willing to pay a reasonable rate or exchange for help in another subject.",
		author: 'StudentAnna',
		authorAvatar:
			'https://images.unsplash.com/photo-1494790108755-2616b612b786?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=100&q=80',
		tags: ['tutoring', 'math', 'calculus', 'university'],
		backgroundImage:
			'https://images.unsplash.com/photo-1509228468518-180dd4864904?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1000&q=80',
		location: 'Munich, Germany',
		coordinates: { lat: 48.1351, lng: 11.582 },
		type: 'Need',
		participantCount: 2,
		maxParticipants: 3,
		createdAt: '2024-10-19',
		targetDate: '2024-10-30',
		duration: '3 hours',
		userParticipationStatus: 'none',
		participationRequests: [
			{
				id: '1',
				userId: 'user1',
				userName: 'MathTutor',
				message: 'I can help with calculus tutoring.',
				status: 'PENDING'
			},
			{
				id: '2',
				userId: 'user2',
				userName: 'CalcExpert',
				message: 'Experienced in teaching integrals.',
				status: 'PENDING'
			}
		],
		chatMessages: [
			{
				id: '1',
				author: 'MathTutor',
				avatar:
					'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=100&q=80',
				content:
					"Hi Anna! I'd be happy to help with calculus. I have experience tutoring university-level math. What specific topics are you struggling with?",
				timestamp: '2024-10-20 9:30 AM'
			},
			{
				id: '2',
				author: 'StudentAnna',
				avatar:
					'https://images.unsplash.com/photo-1494790108755-2616b612b786?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=100&q=80',
				content:
					"Hi! Thanks for reaching out. I'm really struggling with integration by parts and trigonometric integrals. Also, some of the applications of derivatives are confusing me.",
				timestamp: '2024-10-20 10:45 AM'
			},
			{
				id: '3',
				author: 'MathTutor',
				avatar:
					'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=100&q=80',
				content:
					'Those are common challenging topics! I can definitely help. I charge €25/hour. Would you like to schedule a session?',
				timestamp: '2024-10-20 11:20 AM'
			}
		]
	},
	{
		id: '3',
		title: 'Fresh Organic Vegetables from My Garden',
		description:
			'I have an abundance of fresh organic vegetables from my backyard garden this season. Tomatoes, cucumbers, peppers, zucchini, and herbs are all available. Everything is grown without pesticides and harvested fresh. Free to anyone who wants healthy, home-grown produce!',
		author: 'Eva Navarro',
		authorAvatar: 'https://images.pexels.com/photos/16466784/pexels-photo-16466784.jpeg',
		tags: ['food', 'vegetables', 'organic', 'free', 'garden'],
		backgroundImage:
			'https://images.unsplash.com/photo-1416879595882-3373a0480b5b?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1000&q=80',
		location: 'Leipzig, Germany',
		coordinates: { lat: 51.3397, lng: 12.3731 },
		type: 'Offer',
		participantCount: 7,
		maxParticipants: 10,
		createdAt: '2024-10-18',
		targetDate: '2024-11-05',
		duration: 'Ongoing',
		userParticipationStatus: 'accepted',
		participationRequests: [
			{
				id: '1',
				userId: 'currentUser',
				userName: 'You',
				message: 'I would love some vegetables!',
				status: 'ACCEPTED'
			},
			{
				id: '2',
				userId: 'user3',
				userName: 'HealthyEater',
				message: 'Tomatoes and cucumbers please.',
				status: 'ACCEPTED'
			},
			{
				id: '3',
				userId: 'user4',
				userName: 'LocalChef',
				message: 'Herbs and peppers for my restaurant.',
				status: 'PENDING'
			}
		],
		chatMessages: [
			{
				id: '1',
				author: 'HealthyEater',
				avatar:
					'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=100&q=80',
				content:
					"Wow, this looks amazing! I'd love some tomatoes and cucumbers. When can I pick them up?",
				timestamp: '2024-10-19 8:15 AM'
			},
			{
				id: '2',
				author: 'Eva Navarro',
				avatar: 'https://images.pexels.com/photos/16466784/pexels-photo-16466784.jpeg',
				content:
					"Morning! You can come by anytime this week. I'm usually home in the evenings. Just let me know what you need!",
				timestamp: '2024-10-19 9:30 AM'
			},
			{
				id: '3',
				author: 'LocalChef',
				avatar:
					'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=100&q=80',
				content:
					'I run a small restaurant and would love to buy some herbs and peppers for my dishes. How much would you charge?',
				timestamp: '2024-10-19 2:20 PM'
			}
		]
	}
];

export const load = async ({ params }: { params: { id: string } }) => {
	const event = mockEvents.find((e) => e.id === params.id);
	if (!event) {
		throw new Error('Event not found');
	}
	return { event, isOwner: Number(event.id) % 2 == 0 };
};
