import { Result, requestJson } from "../utils/invoke";

const BASE = "https://cloud.centit.com/locode/api";

function enc(s: string): string {
  const b = typeof btoa === "function" ? btoa(unescape(encodeURIComponent(s))) : s;
  return `encode:${b}`;
}

export type CurrentUser = {
  code: number;
  data?: any;
  message?: string;
};

export async function checkAuth(): Promise<Result<any>> {
  return requestJson("GET", `${BASE}/framework/system/mainframe/currentuser`);
}

export async function loginCommon(
  username: string,
  password: string,
  captcha: string
): Promise<Result<any>> {
  return requestJson("POST", `${BASE}/framework/login`, {
    form: {
      username: enc(username),
      password: enc(password),
      j_checkcode: captcha,
      ajax: "true",
    },
  });
}

export async function loginLdap(
  username: string,
  password: string,
  captcha: string
): Promise<Result<any>> {
  return requestJson("POST", `${BASE}/framework/system/ldap/login`, {
    form: {
      username: enc(username),
      password: enc(password),
      j_checkcode: captcha,
      ajax: "true",
    },
  });
}

export function captchaUrl(): string {
  const rand = Math.random();
  return `${BASE}/framework/system/mainframe/captchaimage?rand=${rand}`;
}
