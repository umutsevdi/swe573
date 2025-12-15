import { env as pubEnv } from "$env/dynamic/public";

export const CLIENT_API = pubEnv.PUBLIC_API;

if (!CLIENT_API) {
  throw new Error("PUBLIC_API is required for the client.");
}
