import { Category } from "./akk_Category";
export class CategoryAmount {
  static NAME = "akk_CategoryBalance";
  category?: Category | null;
  amount?: any | null;
}
export type CategoryAmountViewName = "_base" | "_instance_name" | "_local";
export type CategoryAmountView<V extends CategoryAmountViewName> = never;
