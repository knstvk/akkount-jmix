export class User {
  static NAME = "akk_User";
  id?: string;
  version?: number | null;
  username?: string | null;
  password?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  email?: string | null;
  enabled?: boolean | null;
  authorities?: any | null;
}
export type UserViewName = "_base" | "_instance_name" | "_local";
export type UserView<V extends UserViewName> = V extends "_base"
  ? Pick<
      User,
      | "id"
      | "firstName"
      | "lastName"
      | "username"
      | "version"
      | "password"
      | "email"
      | "enabled"
    >
  : V extends "_instance_name"
  ? Pick<User, "id" | "firstName" | "lastName" | "username">
  : V extends "_local"
  ? Pick<
      User,
      | "id"
      | "version"
      | "username"
      | "password"
      | "firstName"
      | "lastName"
      | "email"
      | "enabled"
    >
  : never;
