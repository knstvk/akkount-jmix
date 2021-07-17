import { User } from "./akk_User";
export class UserData {
  static NAME = "akk_UserData";
  user?: User | null;
  key?: string | null;
  value?: string | null;
  createTs?: any | null;
  createdBy?: string | null;
}
export type UserDataViewName = "_base" | "_instance_name" | "_local";
export type UserDataView<V extends UserDataViewName> = V extends "_base"
  ? Pick<UserData, "key" | "value" | "createTs" | "createdBy">
  : V extends "_local"
  ? Pick<UserData, "key" | "value" | "createTs" | "createdBy">
  : never;
