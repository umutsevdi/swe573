const mockTopics = [
	{
		id: '1',
		title: 'Welcome to the Hive Forum!',
		author: 'Admin',
		avatar: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
		createdAt: '2024-10-20',
		repliesCount: 6,
		tags: ['announcement', 'welcome'],
		posts: [
			{
				id: '1-1',
				author: 'Admin',
				avatar: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
				content:
					'Welcome everyone! This is the official forum for discussing offers and needs in our community.',
				timestamp: '2024-10-20 10:00 AM',
				replies: [
					{
						id: '1-1-1',
						author: 'User1',
						avatar: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
						content: 'Thanks for the welcome! Excited to be part of this.',
						timestamp: '2024-10-20 10:15 AM',
						replies: []
					},
					{
						id: '1-1-2',
						author: 'User2',
						avatar: '',
						content: 'How do we start posting offers?',
						timestamp: '2024-10-20 10:30 AM',
						replies: [
							{
								id: '1-1-2-1',
								author: 'Admin',
								avatar:
									'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
								content: 'Just use the "New Post" button on the homepage!',
								timestamp: '2024-10-20 10:45 AM',
								replies: []
							}
						]
					}
				]
			},
			{
				id: '1-2',
				author: 'User1',
				avatar: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
				content: 'Thanks for setting this up! Looking forward to connecting with others.',
				timestamp: '2024-10-20 11:30 AM',
				replies: []
			},
			{
				id: '1-3',
				author: 'User2',
				avatar: '',
				content: 'Great initiative. How do we post new topics?',
				timestamp: '2024-10-20 12:00 PM',
				replies: []
			}
		]
	},
	{
		id: '2',
		title: 'Tutoring Exchange Ideas',
		author: 'TeacherJohn',
		avatar: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
		createdAt: '2024-10-21',
		repliesCount: 10,
		tags: ['tutoring', 'education', 'exchange'],
		posts: [
			{
				id: '2-1',
				author: 'TeacherJohn',
				avatar: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
				content: "I'm offering math tutoring. Anyone interested in language exchange?",
				timestamp: '2024-10-21 2:00 PM',
				replies: [
					{
						id: '2-1-1',
						author: 'StudentAnna',
						avatar: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
						content: 'Yes! I need math help.',
						timestamp: '2024-10-21 2:30 PM',
						replies: []
					},
					{
						id: '2-1-2',
						author: 'LanguageLover',
						avatar: '',
						content: 'Language exchange sounds good!',
						timestamp: '2024-10-21 3:00 PM',
						replies: [
							{
								id: '2-1-2-1',
								author: 'TeacherJohn',
								avatar:
									'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
								content: 'Sure, we can do both!',
								timestamp: '2024-10-21 3:10 PM',
								replies: []
							}
						]
					},
					{
						id: '2-1-3',
						author: 'Polyglot',
						avatar: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
						content: 'Count me in for the group!',
						timestamp: '2024-10-21 3:20 PM',
						replies: []
					}
				]
			},
			{
				id: '2-2',
				author: 'StudentAnna',
				avatar: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
				content: 'I need help with calculus. Your offer sounds perfect!',
				timestamp: '2024-10-21 3:15 PM',
				replies: [
					{
						id: '2-2-1',
						author: 'TeacherJohn',
						avatar: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
						content: 'Absolutely, when are you free?',
						timestamp: '2024-10-21 3:45 PM',
						replies: []
					}
				]
			},
			{
				id: '2-3',
				author: 'TeacherJohn',
				avatar: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
				content: "Great! Let's connect. I can also help with physics.",
				timestamp: '2024-10-21 4:00 PM',
				replies: []
			},
			{
				id: '2-4',
				author: 'LanguageLover',
				avatar: '',
				content: "I'm interested in Spanish-English exchange. Anyone available?",
				timestamp: '2024-10-21 5:30 PM',
				replies: []
			},
			{
				id: '2-5',
				author: 'Polyglot',
				avatar: 'https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp',
				content: "I speak multiple languages. Let's set up a group session!",
				timestamp: '2024-10-21 6:00 PM',
				replies: []
			}
		]
	}
];

export const load = async ({ params }: { params: { id: string } }) => {
	const topic = mockTopics.find((t) => t.id === params.id);
	if (!topic) {
		throw new Error('Topic not found');
	}
	return { topic };
};
