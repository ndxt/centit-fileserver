import { getJson, isTauri } from "../utils/invoke";
import { getConfig } from "../config/config";

const BASE = import.meta.env.DEV && !isTauri() ? "/api" : `${getConfig().locodeOrigin}/api`;

export type LibraryItem = {
  libraryId: string;
  libraryName: string;
  libraryType?: string;
  ownUnit?: string;
};

export type FolderEntry = {
  id: string;
  name: string;
  size: string;
  date: string;
  folder?: boolean;
  encrypted?: boolean;
};

function humanSize(n: number | undefined): string {
  if (!n || n <= 0) return "--";
  const units = ["B","KB","MB","GB","TB"]; 
  let i = 0; 
  let x = n; 
  while (x >= 1024 && i < units.length - 1) { x /= 1024; i++; }
  return `${x.toFixed(x >= 100 ? 0 : x >= 10 ? 1 : 2)} ${units[i]}`;
}

export async function listLibraries(): Promise<{ ok: boolean; data?: LibraryItem[]; error?: string }>{
  const url = `${BASE}/fileserver/fileserver/library?sort=createTime&order=desc`;
  const r = await getJson(url);
  if (!r.ok) return { ok: false, error: r.error };
  const arr = (r.data?.data?.objList || []) as any[];
  const items: LibraryItem[] = arr.map(x => ({
    libraryId: String(x.libraryId || ""),
    libraryName: String(x.libraryName || ""),
    libraryType: x.libraryType ? String(x.libraryType) : undefined,
    ownUnit: x.ownUnit ? String(x.ownUnit) : undefined,
  }));
  return { ok: true, data: items };
}

export async function listLibraryFiles(libraryId: string, folderId: string = "-1"): Promise<{ ok: boolean; data?: FolderEntry[]; error?: string }>{
  const url = `${BASE}/fileserver/fileserver/folder/${libraryId}/${folderId}`;
  const r = await getJson(url);
  if (!r.ok) return { ok: false, error: r.error };
  const arr = (r.data?.data || []) as any[];
  const files: FolderEntry[] = arr.map(x => ({
    id: String(x.folder ? x.folderId || x.fileName || Math.random() : x.accessToken || x.fileName || Math.random()),
    name: String(x.fileName || ""),
    size: x.folder ? "" : humanSize(Number(x.fileSize || 0)),
    date: String(x.createTime || ""),
    folder: Boolean(x.folder),
    encrypted: Boolean(x.encrypt),
  }));
  return { ok: true, data: files };
}
