import { invoke } from "@tauri-apps/api/core";

export type Result<T> = { ok: true; data: T } | { ok: false; error: string };

export const isTauri = (): boolean => typeof (window as any).__TAURI__ !== "undefined";

export async function invokeSafe<T>(cmd: string, payload?: Record<string, unknown>): Promise<Result<T>> {
  try {
    const data = await invoke<T>(cmd, payload);
    return { ok: true, data };
  } catch (e: any) {
    return { ok: false, error: String(e) };
  }
}

export async function getJson(url: string): Promise<Result<any>> {
  if (isTauri()) {
    const r = await invokeSafe<any>("http_get_json", { url });
    if (r.ok) return r;
  }
  try {
    const resp = await fetch(url);
    if (!resp.ok) return { ok: false, error: String(resp.status) };
    const data = await resp.json();
    return { ok: true, data };
  } catch (e: any) {
    return { ok: false, error: String(e) };
  }
}

export async function requestJson(
  method: string,
  url: string,
  opts?: {
    headers?: Record<string, string>;
    json?: any;
    form?: Record<string, string>;
  }
): Promise<Result<any>> {
  if (isTauri()) {
    const headers = opts?.headers
      ? Object.entries(opts.headers).map(([k, v]) => [k, v])
      : undefined;
    const r = await invokeSafe<any>("http_request_json", {
      method,
      url,
      headers,
      json: opts?.json,
      form: opts?.form,
    });
    if (r.ok) return r;
  }
  try {
    let body: BodyInit | undefined;
    const headers: Record<string, string> = opts?.headers ? { ...opts.headers } : {};
    if (opts?.json) {
      headers["Content-Type"] ||= "application/json";
      body = JSON.stringify(opts.json);
    } else if (opts?.form) {
      const usp = new URLSearchParams();
      Object.entries(opts.form).forEach(([k, v]) => usp.append(k, v));
      headers["Content-Type"] ||= "application/x-www-form-urlencoded";
      body = usp;
    }
    const resp = await fetch(url, { method, headers, body });
    if (!resp.ok) return { ok: false, error: String(resp.status) };
    const data = await resp.json();
    return { ok: true, data };
  } catch (e: any) {
    return { ok: false, error: String(e) };
  }
}
