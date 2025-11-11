import { env } from '$env/dynamic/private';

export const SERVER_URL = env.SERVER_URL || 'http://localhost:8080';
