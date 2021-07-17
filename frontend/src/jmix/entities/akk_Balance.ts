import { Account } from "./akk_Account";
export class Balance {
  static NAME = "akk_Balance";
  balanceDate?: any | null;
  account?: Account | null;
  amount?: any | null;
}
export type BalanceViewName = "_base" | "_instance_name" | "_local";
export type BalanceView<V extends BalanceViewName> = V extends "_base"
  ? Pick<Balance, "balanceDate" | "amount">
  : V extends "_local"
  ? Pick<Balance, "balanceDate" | "amount">
  : never;
