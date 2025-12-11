import type { PageServerLoad } from "./$types";
import { error } from "@sveltejs/kit";
import { HIVE_API } from "$lib/serverConfig";
import { API } from "$lib/api";

export const load: PageServerLoad = async ({ params, cookies }) => {
  const id = Number(params.id);
  if (!Number.isInteger(id) || id < 0) {
    console.log("Invalid user id:", id);
    throw error(404, "User not found");
  }

  const userData = await API.users.getById(HIVE_API as string, cookies, id);

  const mappedData = {
    fullName: userData.fullName,
    username: userData.username,
    biography: userData.biography,
    interests: userData.interests,
    self: userData.self,
    timeBank: userData.timeBank || 0,
    frozenAmount: userData.frozenAmount || 0,
    userScore: userData.userScore
  };
  return mappedData;
};
