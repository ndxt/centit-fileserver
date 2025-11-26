import { Result, requestJson, isTauri } from "../utils/invoke";
import { getConfig } from "../config/config";

const BASE = import.meta.env.DEV && !isTauri() ? "/api" : `${getConfig().locodeOrigin}/api`;

function enc(s: string): string {
  try {
    if (typeof btoa === "function") {
      return `encode:${btoa(unescape(encodeURIComponent(s)))}`;
    }
    const globalObj: any = typeof globalThis !== "undefined" ? globalThis : typeof window !== "undefined" ? window : undefined;
    const BufferCtor = globalObj?.Buffer;
    if (BufferCtor && typeof BufferCtor.from === "function") {
      return `encode:${BufferCtor.from(s, "utf-8").toString("base64")}`;
    }
    throw new Error("base64 encoding not available in this environment");
  } catch (e: any) {
    throw new Error("Failed to base64-encode string: " + String(e));
  }
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
