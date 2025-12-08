/**
 * Decodes the payload of a JWT token.
 * @param token - The JWT token.
 * @returns The decoded payload or null if invalid.
 */
export function decodeJwtPayload(token: string): { id?: number } | null {
  try {
    const payload = token.split(".")[1];
    const decoded = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(decoded);
  } catch (e) {
    console.error("Failed to decode JWT:", e);
    return null;
  }
}
