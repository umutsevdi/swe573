import { env } from "$env/dynamic/public";

if (!env.PUBLIC_API) {
  throw new Error("PUBLIC_API is required for the client.");
}
export const HIVE_API = env.PUBLIC_API!;
