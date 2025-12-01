import { defineStore } from "pinia";
import { invoke } from "@tauri-apps/api/core";
import { listen } from "@tauri-apps/api/event";
import { getConfig } from "../config/config";
import { isTauri } from "../utils/invoke";
import { useAuthStore } from "./auth";

export type TransferStatus = "queued" | "downloading" | "completed" | "failed";

export type TransferItem = {
  id: string;
  name: string;
  url: string;
  status: TransferStatus;
  progress: number;
  received: number;
  total?: number;
  speedBps: number;
  etaSec?: number;
  savePath?: string;
  error?: string;
};

function humanSize(n: number | undefined): string {
  if (!n || n <= 0) return "--";
  const units = ["B","KB","MB","GB","TB"]; 
  let i = 0; 
  let x = n; 
  while (x >= 1024 && i < units.length - 1) { x /= 1024; i++; }
  return `${x.toFixed(x >= 100 ? 0 : x >= 10 ? 1 : 2)} ${units[i]}`;
}

export const useTransferStore = defineStore("transfer", {
  state: () => ({
    queue: [] as TransferItem[],
    active: [] as TransferItem[],
    completed: [] as TransferItem[],
    maxConcurrent: 5,
    initialized: false,
  }),
  getters: {
    downloading(state) { return state.active; },
    done(state) { return state.completed; },
  },
  actions: {
    async initListeners() {
      if (this.initialized || !isTauri()) return;
      this.initialized = true;
      await listen<{
        task_id: string;
        file_name: string;
        received: number;
        total?: number;
        speed_bps: number;
        eta_secs?: number;
      }>("download_progress", (evt) => {
        console.log("event: download_progress", evt.payload);
        const p = evt.payload;
        const t = this.active.find(x => x.id === p.task_id);
        if (!t) return;
        t.received = p.received;
        t.total = p.total;
        t.speedBps = p.speed_bps || 0;
        t.etaSec = p.eta_secs;
        t.progress = p.total && p.total > 0 ? Math.min(100, (p.received * 100) / p.total) : 0;
      });
      await listen<{ task_id: string; file_name: string; save_path: string }>("download_finished", (evt) => {
        console.log("event: download_finished", evt.payload);
        const p = evt.payload;
        const idx = this.active.findIndex(x => x.id === p.task_id);
        if (idx >= 0) {
          const item = this.active[idx];
          item.status = "completed";
          item.progress = 100;
          item.savePath = p.save_path;
          this.active.splice(idx, 1);
          this.completed.unshift(item);
          this.startIfNeeded();
        }
      });
      await listen<{ task_id: string; file_name: string; error: string }>("download_error", (evt) => {
        console.error("event: download_error", evt.payload);
        const p = evt.payload;
        const idx = this.active.findIndex(x => x.id === p.task_id);
        if (idx >= 0) {
          const item = this.active[idx];
          item.status = "failed";
          item.error = p.error;
          this.active.splice(idx, 1);
          this.completed.unshift(item);
          this.startIfNeeded();
        }
      });
    },
    enqueue(files: Array<{ id: string; name: string }>) {
      const base = `${getConfig().locodeOrigin}/api`;
      const auth = useAuthStore();
      const userCode: string | undefined = (auth.currentUser?.userInfo?.userCode || auth.currentUser?.userCode) as string | undefined;
      const newItems: TransferItem[] = files.map(f => ({
        id: f.id,
        name: f.name,
        url: `${base}/fileserver/fileserver/download/downloadwithauth/${f.id}${userCode ? `?userCode=${encodeURIComponent(userCode)}&accessToken=${encodeURIComponent(f.id)}` : `?accessToken=${encodeURIComponent(f.id)}`}`,
        status: "queued",
        progress: 0,
        received: 0,
        total: undefined,
        speedBps: 0,
      }));
      console.log("enqueue", newItems);
      this.queue.push(...newItems);
      this.startIfNeeded();
    },
    simulate() {
      if (this.active.length || this.queue.length || this.completed.length) return;
      const a1: TransferItem = {
        id: "sim-1",
        name: "报告.pdf",
        url: "",
        status: "downloading",
        progress: Math.min(100, (15 * 1024 * 1024 * 100) / (50 * 1024 * 1024)),
        received: 15 * 1024 * 1024,
        total: 50 * 1024 * 1024,
        speedBps: 95 * 1024,
      };
      const a2: TransferItem = {
        id: "sim-2",
        name: "视频.mp4",
        url: "",
        status: "downloading",
        progress: Math.min(100, (120 * 1024 * 1024 * 100) / (200 * 1024 * 1024)),
        received: 120 * 1024 * 1024,
        total: 200 * 1024 * 1024,
        speedBps: 80 * 1024,
      };
      const q1: TransferItem = {
        id: "sim-q1",
        name: "图片.zip",
        url: "",
        status: "queued",
        progress: 0,
        received: 0,
        total: undefined,
        speedBps: 0,
      };
      const q2: TransferItem = {
        id: "sim-q2",
        name: "源码.tar.gz",
        url: "",
        status: "queued",
        progress: 0,
        received: 0,
        total: undefined,
        speedBps: 0,
      };
      this.active = [a1, a2];
      this.queue = [q1, q2];
    },
    async startIfNeeded() {
      await this.initListeners();
      console.log("startIfNeeded", { active: this.active.length, queue: this.queue.length, isTauri: isTauri() });
      while (this.active.length < this.maxConcurrent && this.queue.length > 0) {
        const item = this.queue.shift()!;
        item.status = "downloading";
        this.active.push(item);
        if (isTauri()) {
          console.log("invoke download_file", { task_id: item.id, url: item.url, file_name: item.name });
          invoke<string>("download_file", {
            taskId: item.id,
            url: item.url,
            fileName: item.name,
            dirName: "download",
          }).catch((e: any) => {
            console.error("invoke download_file error", e, { task_id: item.id, url: item.url, file_name: item.name });

            const idx = this.active.findIndex(x => x.id === item.id);
            if (idx >= 0) {
              const it = this.active[idx];
              it.status = "failed";
              it.error = String(e);
              this.active.splice(idx, 1);
              this.completed.unshift(it);
              this.startIfNeeded();
            }
          });
        }
      }
    },
    clearCompleted() {
      this.completed = [];
    },
    stats(): { waiting: number; downloading: number; completed: number } {
      return { waiting: this.queue.length, downloading: this.active.length, completed: this.completed.length };
    },
    displaySize(n?: number) { return humanSize(n); },
    displaySpeed(bps: number) { return humanSize(bps) + "/s"; },
    displayEta(sec?: number) {
      if (sec === undefined || sec <= 0 || !isFinite(sec)) return "--";
      const s = Math.max(0, Math.floor(sec));
      const h = Math.floor(s / 3600);
      const m = Math.floor((s % 3600) / 60);
      const ss = s % 60;
      if (h > 0) return `${h}:${m.toString().padStart(2,'0')}:${ss.toString().padStart(2,'0')}`;
      return `${m}:${ss.toString().padStart(2,'0')}`;
    },
  }
});
