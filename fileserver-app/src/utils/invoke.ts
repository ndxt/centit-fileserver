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