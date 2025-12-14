import { HIVE_API } from "$lib/serverConfig";
import { type Cookies } from "@sveltejs/kit";
import {
  API,
  type TransactionRequest,
  type TransactionResponse
} from "$lib/api";

export const load = async ({
  url,
  cookies
}: {
  url: URL;
  cookies: Cookies;
}) => {
  const params = url.searchParams;

  const message = params.get("message") || "";
  const typesParam = params.get("types");
  const types = typesParam ? typesParam.split(",").filter(Boolean) : [];
  const before = params.get("before") || "";
  const after = params.get("after") || "";
  const idType = params.get("id_type");
  const idValue = params.get("id_value");
  const id =
    idType && idValue
      ? { type: idType as "USER" | "EVENT" | "THREAD", id: parseInt(idValue) }
      : undefined;

  let transactions: TransactionResponse[] = [];
  let hasSearched = false;

  // Always search, but apply filters if provided
  try {
    const requestBody: TransactionRequest = {};

    // If no filters are provided, default to today's transactions
    if (!message && !types.length && !before && !after && !id) {
      const today = new Date();
      const startOfDay = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate()
      ).toISOString();
      const endOfDay = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate(),
        23,
        59,
        59,
        999
      ).toISOString();
      requestBody.after = startOfDay;
      requestBody.before = endOfDay;
    } else {
      // Apply provided filters
      if (before) {
        // If date-only string, append end of day time
        requestBody.before = before.includes("T")
          ? before
          : `${before}T23:59:59`;
      }
      if (after) {
        // If date-only string, append start of day time
        requestBody.after = after.includes("T") ? after : `${after}T00:00:00`;
      }
    }

    if (message.trim()) requestBody.message = message.trim();
    if (types.length > 0) requestBody.type = types as any;
    if (id) requestBody.id = id;

    transactions = await API.admin.transactions.filter(
      HIVE_API!,
      cookies,
      requestBody
    );
    hasSearched = true;
  } catch (error) {
    console.error("Error fetching transactions:", error);
    transactions = [];
    hasSearched = true;
  }

  return {
    transactions,
    hasSearched,
    message,
    types,
    before,
    after,
    idType,
    idValue
  };
};
