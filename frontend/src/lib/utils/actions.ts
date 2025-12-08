/**
 * Validates that required fields are present and not empty.
 * @param fields - Object with field names and values.
 * @returns Error message or null if valid.
 */
export function validateRequired(
  fields: Record<string, unknown>
): string | null {
  for (const [key, value] of Object.entries(fields)) {
    if (!value || (typeof value === "string" && !value.trim())) {
      return `${key} is required`;
    }
  }
  return null;
}
