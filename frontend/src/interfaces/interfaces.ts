export interface IDish {
  name: string;
  price: number;
}

export interface IMenu {
  provider: string;
  date: string;
  dishes: IDish[];
}

export interface MenusByWeek {
  [week: number]: MenusByDay;
}

export interface MenusByDay {
  [day: string]: IMenu[];
}