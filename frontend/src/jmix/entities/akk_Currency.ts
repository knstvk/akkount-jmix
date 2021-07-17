export class Currency {
  static NAME = "akk_Currency";
  code?: string | null;
  name?: string | null;
}
export type CurrencyViewName = "_base" | "_instance_name" | "_local";
export type CurrencyView<V extends CurrencyViewName> = V extends "_base"
  ? Pick<Currency, "code" | "name">
  : V extends "_local"
  ? Pick<Currency, "code" | "name">
  : never;
