import { defineStore } from "pinia";

export type PathEntry = { id: string; name: string };
export type ExplorerState = {
  libraryId: string;
  libraryName: string;
  folderId: string;
  path: PathEntry[];
  backStack: Array<{ libraryId: string; libraryName: string; folderId: string; path: PathEntry[] }>;
  forwardStack: Array<{ libraryId: string; libraryName: string; folderId: string; path: PathEntry[] }>;
  refreshTs: number;
};

function cloneState(s: ExplorerState) {
  return {
    libraryId: s.libraryId,
    libraryName: s.libraryName,
    folderId: s.folderId,
    path: s.path.slice(),
  };
}

export const useExplorerStore = defineStore("explorer", {
  state: (): ExplorerState => ({
    libraryId: "",
    libraryName: "",
    folderId: "-1",
    path: [],
    backStack: [],
    forwardStack: [],
    refreshTs: 0,
  }),
  actions: {
    setLibrary(id: string, name: string) {
      this.libraryId = id;
      this.libraryName = name;
      this.folderId = "-1";
      this.path = [];
      this.backStack = [];
      this.forwardStack = [];
    },
    setLibraryName(name: string) {
      this.libraryName = name;
    },
    enterFolder(id: string, name: string) {
      this.backStack.push(cloneState(this));
      this.forwardStack = [];
      this.folderId = id;
      this.path = [...this.path, { id, name }];
    },
    gotoRoot() {
      this.backStack.push(cloneState(this));
      this.forwardStack = [];
      this.folderId = "-1";
      this.path = [];
    },
    gotoIndex(idx: number) {
      const target = this.path[idx];
      if (!target) return;
      this.backStack.push(cloneState(this));
      this.forwardStack = [];
      this.path = this.path.slice(0, idx + 1);
      this.folderId = target.id;
    },
    canBack(): boolean { return this.backStack.length > 0; },
    canForward(): boolean { return this.forwardStack.length > 0; },
    back() {
      if (!this.canBack()) return;
      const prev = this.backStack.pop()!;
      this.forwardStack.push(cloneState(this));
      this.libraryId = prev.libraryId;
      this.libraryName = prev.libraryName;
      this.folderId = prev.folderId;
      this.path = prev.path.slice();
    },
    forward() {
      if (!this.canForward()) return;
      const next = this.forwardStack.pop()!;
      this.backStack.push(cloneState(this));
      this.libraryId = next.libraryId;
      this.libraryName = next.libraryName;
      this.folderId = next.folderId;
      this.path = next.path.slice();
    },
    requestRefresh() { this.refreshTs = Date.now(); },
    saveState(key: string) {
      const payload = {
        libraryId: this.libraryId,
        libraryName: this.libraryName,
        folderId: this.folderId,
        path: this.path,
        backStack: this.backStack,
        forwardStack: this.forwardStack,
      };
      try { sessionStorage.setItem(`explorer/${key}`, JSON.stringify(payload)); } catch {}
    },
    restoreState(key: string) {
      try {
        const raw = sessionStorage.getItem(`explorer/${key}`);
        if (!raw) return false;
        const s = JSON.parse(raw);
        this.libraryId = s.libraryId || this.libraryId;
        this.libraryName = s.libraryName || this.libraryName;
        this.folderId = s.folderId || this.folderId;
        this.path = Array.isArray(s.path) ? s.path : [];
        this.backStack = Array.isArray(s.backStack) ? s.backStack : [];
        this.forwardStack = Array.isArray(s.forwardStack) ? s.forwardStack : [];
        return true;
      } catch {
        return false;
      }
    },
  },
});
