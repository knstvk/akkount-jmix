import { Currency } from "./akk_Currency";
export class Account {
  static NAME = "akk_Account";
  name?: string | null;
  description?: string | null;
  currency?: Currency | null;
  currencyCode?: string | null;
  active?: boolean | null;
  group?: number | null;
}
export type AccountViewName =
  | "_base"
  | "_instance_name"
  | "_local"
  | "account-with-currency";
export type AccountView<V extends AccountViewName> = V extends "_base"
  ? Pick<Account, "name" | "description" | "currencyCode" | "active" | "group">
  : V extends "_local"
  ? Pick<Account, "name" | "description" | "currencyCode" | "active" | "group">
  : V extends "account-with-currency"
  ? Pick<
      Account,
      "name" | "description" | "currencyCode" | "active" | "group" | "currency"
    >
  : never;
