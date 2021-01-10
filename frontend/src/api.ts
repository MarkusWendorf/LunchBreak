import dayjs from "dayjs";
import isoWeek from "dayjs/plugin/isoWeek";
import type { MenusByWeek } from "./interfaces/interfaces";

dayjs.extend(isoWeek);

export function getLunchData(): Promise<MenusByWeek> {
  return exponentialBackoff(async () => {
    const lunchdata = "https://d127kst3f1l6wp.cloudfront.net/lunchdata.json";
    const headers = new Headers();
    headers.append("cache-control", "no-cache");

    const response = await fetch(lunchdata, { headers });
    return response.json();
  });
}

/***
 * Returns the current day, if it's Saturday / Sunday returns the next monday
 */
export function getCurrentDay() {
  const day = dayjs().isoWeekday();
  return day > 5 ? 1 : day;
}

/***
 * Returns the current week number, if it's Saturday / Sunday returns the next week
 */
export function getCurrentWeek() {
  const currentDate = dayjs();
  const day = currentDate.isoWeekday();
  return day > 5 ? currentDate.add(1, "week").isoWeek() : currentDate.isoWeek();
}

async function exponentialBackoff<T>(
  apiCall: () => Promise<T>,
  maxRetries = 5,
  retries = 0,
  error: Error = null
): Promise<T> {
  if (retries > maxRetries) throw error;

  try {
    return await apiCall();
  } catch (err) {
    await sleep(10 ** retries);
    return exponentialBackoff(apiCall, maxRetries, retries + 1, err);
  }
}

function sleep(ms: number) {
  return new Promise((res) => setTimeout(res, ms));
}
