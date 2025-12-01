import { env } from '$env/dynamic/private';
import { env as pubEnv } from '$env/dynamic/public';

console.log(`${env.SERVER_API}, ${pubEnv.PUBLIC_API}`);
export const HIVE_API = env.SERVER_API || pubEnv.PUBLIC_API;

if (!HIVE_API) {
	throw new Error('SERVER_API or PUBLIC_API is required for the server.');
}
