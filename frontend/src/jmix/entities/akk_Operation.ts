import { OperationType } from "../enums/enums";
import { Account } from "./akk_Account";
import { Category } from "./akk_Category";
export class Operation {
  static NAME = "akk_Operation";
  opType?: OperationType | null;
  opDate?: any | null;
  acc1?: Account | null;
  acc2?: Account | null;
  amount1?: any | null;
  amount2?: any | null;
  category?: Category | null;
  comments?: string | null;
}
export type OperationViewName =
  | "_base"
  | "_instance_name"
  | "_local"
  | "operation-browse"
  | "operation-edit"
  | "operation-recalc-balance";
export type OperationView<V extends OperationViewName> = V extends "_base"
  ? Pick<Operation, "opType" | "opDate" | "amount1" | "amount2" | "comments">
  : V extends "_local"
  ? Pick<Operation, "opType" | "opDate" | "amount1" | "amount2" | "comments">
  : V extends "operation-browse"
  ? Pick<
      Operation,
      | "opType"
      | "opDate"
      | "amount1"
      | "amount2"
      | "comments"
      | "acc1"
      | "acc2"
      | "category"
    >
  : V extends "operation-edit"
  ? Pick<
      Operation,
      | "opType"
      | "opDate"
      | "amount1"
      | "amount2"
      | "comments"
      | "acc1"
      | "acc2"
      | "category"
    >
  : V extends "operation-recalc-balance"
  ? Pick<Operation, "opDate" | "acc1" | "acc2" | "amount1" | "amount2">
  : never;
