import getDay from "date-fns/getDay";
import getWeek from "date-fns/getWeek";
import addWeeks from "date-fns/addWeeks";
import { de } from "date-fns/locale";
import type { MenusByWeek } from "./interfaces/interfaces";



export async function getLunchData(): Promise<MenusByWeek> {
	const lunchdata = "https://d127kst3f1l6wp.cloudfront.net/lunchdata.json";
	const headers = new Headers();
	headers.append("pragma", "no-cache");
	headers.append("cache-control", "no-cache");

	const response = await fetch(lunchdata, { headers });
	//await new Promise(res => setTimeout(res, 2000));
  return response.json();
}

/***
 * Returns the current day, if it's Saturday / Sunday returns the next monday
 */
export function getCurrentDay() {
  const day = getDay(new Date()) - 1;
  const normalized = day === -1 ? 6 : day; // 0 = monday, 6 = sunday
  return normalized >= 5 ? 0 : normalized;
}

/***
 * Returns the current week, if it's Saturday / Sunday returns the next week
 */
export function getCurrentWeek() {
  const today = new Date();
  const day = getDay(new Date()) - 1;
  const normalized = day === -1 ? 6 : day;
  
  return normalized >= 5 
    ? getWeek(addWeeks(today, 1), { weekStartsOn: 1, locale: de })
    : getWeek(today, { weekStartsOn: 1, locale: de });
}