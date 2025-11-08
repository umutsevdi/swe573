import type { PageServerLoad } from './$types';
import { error } from '@sveltejs/kit';
const users: Array<{
	fullName: string;
	bio: string;
	interests: string[];
	profileUrl: string;
	isSelf: boolean;
	offers: Array<{
		id: string;
		title: string;
		description: string;
		tags: string[];
		location: string;
		participantCount: number;
		type?: 'Offer' | 'Need';
	}>;
	needs: Array<{
		id: string;
		title: string;
		description: string;
		tags: string[];
		location: string;
		participantCount: number;
		type?: 'Offer' | 'Need';
	}>;
	remainingTime: number;
	userScore: number;
}> = [
	{
		fullName: 'Dr. Alex Rivera',
		bio: 'Embedded software engineer with a Master’s degree in Software Engineering, specializing in real‑time operating systems and low‑level hardware interfacing.',
		interests: [
			'real‑time systems',
			'hardware‑accelerated cryptography',
			'formal verification',
			'open‑source firmware development',
			'IoT security'
		],
		profileUrl:
			'https://images.unsplash.com/photo-1587778082149-bd5b1bf5d3fa?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxjb2xsZWN0aW9uLXBhZ2V8MXw5NDUyNDk0fHxlbnwwfHx8fHw%3D&auto=format&fit=crop&q=60&w=400',
		isSelf: false,
		offers: [
			{
				id: '1',
				title: 'Free bike for anyone',
				description: 'A gently used mountain bike, perfect for city commuting or weekend rides.',
				tags: ['bike', 'free', 'outdoor'],
				location: 'Berlin, DE',
				type: 'Offer',
				participantCount: 4
			}
		],
		needs: [
			{
				id: '2',
				title: 'Looking for a JavaScript mentor',
				description: 'Junior developer seeking guidance on modern ES6+ patterns and React hooks.',
				tags: ['programming', 'javascript', 'mentor'],
				location: 'Düsseldorf, DE',
				type: 'Need',
				participantCount: 0
			},
			{
				id: '3',
				title: 'Need a tutor for calculus',
				description:
					'Looking for a patient tutor to help with integral and differential calculus concepts.',
				tags: ['tutoring', 'math', 'college'],
				location: 'Munich, DE',
				type: 'Need',
				participantCount: 0
			}
		],
		remainingTime: 0,
		userScore: 100
	},
	{
		fullName: 'Maya Lévesque',
		bio: 'Freelance photographer specializing in street and portrait work. Fluent in French and English, Maya blends visual storytelling with a keen eye for candid moments.',
		interests: ['jogging', 'photography', 'travel', 'French language & culture'],
		profileUrl:
			'https://images.unsplash.com/photo-1595290293434-555d42427e84?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=687',
		isSelf: false,
		offers: [
			{
				id: '4',
				title: 'Free bookshelf donation',
				description: 'Solid wood bookshelf, 5 shelves, no scratches. Pick‑up only.',
				tags: ['furniture', 'free', 'bookshelf'],
				location: 'Cologne, DE',
				type: 'Offer',
				participantCount: 0
			},
			{
				id: '5',
				title: 'Garden tools giveaway',
				description:
					'Hand tools (shovel, rake, pruning shears) in good condition, free to anyone interested.',
				tags: ['garden', 'tools', 'free'],
				location: 'Hamburg, DE',
				type: 'Offer',
				participantCount: 2
			},
			{
				id: '6',
				title: 'Offer: Spare laptop charger',
				description: '13‑inch MacBook charger, works perfectly, free to anyone who needs it.',
				tags: ['electronics', 'charger', 'free'],
				location: 'Hanover, DE',
				type: 'Offer',
				participantCount: 0
			}
		],
		needs: [
			{
				id: '7',
				title: 'Need a tutor for English',
				description:
					'Seeking an experienced native speaker to improve conversational and academic English.',
				tags: ['tutoring', 'english', 'college'],
				location: 'Istanbul, TR',
				type: 'Need',
				participantCount: 0
			}
		],
		userScore: 50,
		remainingTime: 8
	},
	{
		fullName: 'Ethan Patel',
		bio: 'Undergraduate physics major passionate about exploring quantum phenomena and expressing creativity through acrylic painting. Balances lab work with weekend art sessions.',
		interests: ['physics', 'painting', 'science outreach', 'art exhibitions'],
		profileUrl:
			'https://images.unsplash.com/photo-1543610892-0b1f7e6d8ac1?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=687',
		isSelf: true,
		offers: [
			{
				id: '8',
				title: 'Free yoga mat',
				description: 'Non‑slip yoga mat, lightly used, perfect for home practice.',
				tags: ['fitness', 'yoga', 'free'],
				location: 'Nuremberg, DE',
				type: 'Offer',
				participantCount: 1
			}
		],
		needs: [
			{
				id: '9',
				title: 'Need a dog walker',
				description:
					'Looking for someone reliable to walk my Labrador twice a day during work hours.',
				tags: ['pets', 'dog-walking', 'need'],
				location: 'Frankfurt, DE',
				type: 'Need',
				participantCount: 0
			},
			{
				id: '10',
				title: 'Need a language exchange partner (German ↔ Turkish)',
				description:
					'Native German speaker wants to improve Turkish; willing to help with German in return.',
				tags: ['language', 'exchange', 'german', 'turkish'],
				location: 'Bremen, DE',
				type: 'Need',
				participantCount: 0
			}
		],
		userScore: 30,
		remainingTime: 8
	},
	{
		fullName: 'Eva Navarro',
		bio: 'Graduate student in environmental engineering who loves turning data into actionable sustainability solutions. When she isn’t modeling water‑resource systems, she explores the city’s street‑food scene and captures moments with her vintage film camera.',
		interests: [
			'environmental engineering',
			'data analytics',
			'urban gardening',
			'street photography'
		],
		profileUrl: 'https://images.pexels.com/photos/16466784/pexels-photo-16466784.jpeg',
		isSelf: false,
		offers: [
			{
				id: '11',
				title: 'Offer: Fresh vegetables from garden',
				description: 'Organic tomatoes, cucumbers, and peppers – harvested this week, free pickup.',
				tags: ['food', 'vegetables', 'free'],
				location: 'Leipzig, DE',
				type: 'Offer',
				participantCount: 3
			}
		],
		needs: [
			{
				id: '12',
				title: 'Looking for a piano teacher',
				description: 'Beginner seeking weekly lessons for classical piano, preferably evenings.',
				tags: ['music', 'piano', 'lessons'],
				location: 'Stuttgart, DE',
				type: 'Need',
				participantCount: 1
			}
		],
		userScore: 10,
		remainingTime: 8
	}
];

export const load: PageServerLoad = async ({ params }) => {
	const id = Number(params.id);
	console.log('Loading user:', id);

	if (!Number.isInteger(id) || id < 0 || id >= users.length) {
		console.log('User not found:', id);
		throw error(404, 'User not found');
	}

	const userData = users[id];
	console.log('Returning user data:', userData);
	return userData;
};
