type NotificationType = "forum_reply" | "offer_response" | "need_response";

type NotificationData = {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  sourceId: string;
  sourceUrl: string;
  timestamp: string;
  isRead: boolean;
};

const mockNotifications: NotificationData[] = [
  {
    id: "1",
    type: "forum_reply",
    title: 'New reply in "Tutoring Exchange Ideas"',
    message: "StudentAnna replied to your post about math tutoring",
    sourceId: "2",
    sourceUrl: "/forum/2",
    timestamp: "2024-10-22 10:30 AM",
    isRead: false
  },
  {
    id: "2",
    type: "offer_response",
    title: "Interest in your bike offer",
    message: "Someone wants to pick up your free mountain bike",
    sourceId: "0",
    sourceUrl: "/user/0",
    timestamp: "2024-10-21 3:45 PM",
    isRead: true
  },
  {
    id: "3",
    type: "need_response",
    title: "Response to your tutoring need",
    message: "TeacherJohn can help with calculus tutoring",
    sourceId: "1",
    sourceUrl: "/user/1",
    timestamp: "2024-10-20 9:15 AM",
    isRead: false
  },
  {
    id: "4",
    type: "forum_reply",
    title: 'Reply to "Welcome to the Hive Forum!"',
    message: "User2 asked about posting new topics",
    sourceId: "1",
    sourceUrl: "/forum/1",
    timestamp: "2024-10-19 12:15 PM",
    isRead: false
  },
  {
    id: "5",
    type: "offer_response",
    title: "Response to your bookshelf offer",
    message: "Maya Lévesque is interested in your free bookshelf",
    sourceId: "1",
    sourceUrl: "/user/1",
    timestamp: "2024-10-18 4:20 PM",
    isRead: true
  }
];

export const load = async () => {
  return { notifications: mockNotifications };
};
