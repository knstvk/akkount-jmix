import { CategoryType } from "../enums/enums";
export class Category {
  static NAME = "akk_Category";
  name?: string | null;
  description?: string | null;
  catType?: CategoryType | null;
}
export type CategoryViewName = "_base" | "_instance_name" | "_local";
export type CategoryView<V extends CategoryViewName> = V extends "_base"
  ? Pick<Category, "name" | "description" | "catType">
  : V extends "_local"
  ? Pick<Category, "name" | "description" | "catType">
  : never;
