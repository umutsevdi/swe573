import { env } from '$env/dynamic/public';

export const HIVE_API = env.PUBLIC_API;

if (!HIVE_API) {
	throw new Error('PUBLIC_API is required for the client.');
}
