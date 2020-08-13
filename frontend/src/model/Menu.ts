import type { Dish } from "./Dish";

export interface Menu {
    provider: string;
    dishes: Dish[];
}