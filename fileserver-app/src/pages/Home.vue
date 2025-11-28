<script setup lang="ts">
import FileBrowser from "../components/FileBrowser.vue";
import { ref, onMounted, onUnmounted, watch, computed } from "vue";
import { listLibraries, listLibraryFiles } from "../services/files";
import { House, Building2, Plus, Upload, ChevronDown, FileUp, FolderUp, Download } from "lucide-vue-next";
import { useAuthStore } from "../stores/auth";
import { useGlobalStore } from "../stores/global";
import { useExplorerStore } from "../stores/explorer";
import Breadcrumb from "../components/Breadcrumb.vue";

const auth = useAuthStore();
const global = useGlobalStore();
const explorer = useExplorerStore();
const sidebarItems = ref<any[]>([]);
const files = ref<any[]>([]);
const selectedIds = ref<string[]>([]);
const selectedSidebarId = ref<string>("");
const currentLibraryName = computed(() => explorer.libraryName || "根目录");

const showUploadMenu = ref(false);
const uploadBtnRef = ref<HTMLElement | null>(null);
const uploadMenuRef = ref<HTMLElement | null>(null);

function toggleUploadMenu() {
  showUploadMenu.value = !showUploadMenu.value;
}

function handleClickOutside(event: MouseEvent) {
  if (showUploadMenu.value && 
      uploadMenuRef.value && 
      !uploadMenuRef.value.contains(event.target as Node) &&
      uploadBtnRef.value &&
      !uploadBtnRef.value.contains(event.target as Node)
  ) {
    showUploadMenu.value = false;
  }
}

async function init() {
  try {
    global.startLoading("加载目录中...");
    const r1 = await listLibraries();
    if (r1.ok && r1.data && r1.data.length > 0) {
      const topUnitCode = (auth.currentUser?.topUnitCode ?? "") as string;
      const items = r1.data.map(x => {
        const isP = x.libraryType === "P";
        const isTopUnit = (x.ownUnit || "") === (topUnitCode || "");
        const icon = isP ? House : (isTopUnit ? Building2 : undefined);
        const priority = isP ? 2 : (isTopUnit ? 1 : 0);
        return { id: x.libraryId, name: x.libraryName, icon, priority };
      }).sort((a, b) => b.priority - a.priority).map(({ id, name, icon }) => ({ id, name, icon }));
      sidebarItems.value = items;
      const hasExisting = explorer.libraryId && items.some(i => i.id === explorer.libraryId);
      if (hasExisting) {
        const s = items.find(i => i.id === explorer.libraryId);
        explorer.setLibraryName(s?.name || explorer.libraryName);
        selectedSidebarId.value = explorer.libraryId;
        await loadFiles(explorer.libraryId, explorer.folderId);
      } else {
        const firstId = items[0]?.id || r1.data[0].libraryId;
        const firstName = items[0]?.name || r1.data[0].libraryName;
        explorer.setLibrary(firstId, firstName);
        selectedSidebarId.value = firstId;
        await loadFiles(explorer.libraryId, explorer.folderId);
      }
    } else {
      sidebarItems.value = [];
      explorer.setLibrary("", "");
      files.value = [];
    }
  } finally {
    global.stopLoading();
  }
}

async function loadFiles(libId: string, folderId: string = "-1") {
  try {
    global.startLoading("加载文件中...");
    const r2 = await listLibraryFiles(libId, folderId);
    files.value = r2.ok && r2.data ? r2.data : [];
  } finally {
    global.stopLoading();
  }
}

async function onOpen(id: string) { 
  const entry = files.value.find((f: any) => f.id === id);
  if (entry?.folder) {
    explorer.enterFolder(id, entry.name);
    selectedIds.value = []; // Clear selection when entering a folder
    await loadFiles(explorer.libraryId, explorer.folderId);
  } else {
    console.log("Open file", id);
  }
}

onMounted(() => { 
  init(); 
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});

watch(() => selectedSidebarId.value, async (id) => {
  if (!id) return;
  const s = sidebarItems.value.find((x: any) => x.id === id);
  if (id === explorer.libraryId) {
    explorer.setLibraryName(s?.name || explorer.libraryName);
    await loadFiles(explorer.libraryId, explorer.folderId);
    return;
  }
  explorer.setLibrary(id, s?.name || "");
  selectedIds.value = [];
  await loadFiles(explorer.libraryId, explorer.folderId);
});

watch(() => explorer.folderId, async (fid) => {
  if (explorer.libraryId) {
    selectedIds.value = []; // Clear selection when changing folder (via breadcrumb/nav)
    await loadFiles(explorer.libraryId, fid);
  }
});

watch(() => explorer.refreshTs, async () => {
  if (explorer.libraryId) {
    await loadFiles(explorer.libraryId, explorer.folderId);
  }
});
</script>

<template>
  <FileBrowser 
    :sidebar-items="sidebarItems"
    v-model:selected-sidebar-id="selectedSidebarId"
    :files="files"
    v-model:selected-ids="selectedIds"
    @open-file="onOpen"
  >
    <template #header>
      <div class="h-12 border-b border-slate-100 bg-white flex items-center justify-between px-4 shrink-0 select-none z-10 relative">
        <div class="flex-1 flex items-center min-w-0 mr-4 overflow-hidden">
          <Breadcrumb 
            :items="explorer.path" 
            :root-name="currentLibraryName" 
            @goto-root="explorer.gotoRoot" 
            @goto-index="explorer.gotoIndex" 
          />
        </div>
        <div class="flex items-center gap-3 shrink-0">
          <button class="flex items-center gap-1.5 px-3 py-1.5 rounded-md bg-sky-50 text-sky-600 hover:bg-sky-100 transition-colors text-sm font-medium">
            <Plus :size="16" />
            <span>新建文件夹</span>
          </button>

          <div class="relative">
            <button 
              ref="uploadBtnRef"
              @click="toggleUploadMenu" 
              class="flex items-center gap-1.5 px-3 py-1.5 rounded-md bg-sky-600 text-white hover:bg-sky-700 transition-colors text-sm font-medium shadow-sm shadow-sky-200"
            >
              <Upload :size="16" />
              <span>上传</span>
              <ChevronDown :size="14" :class="{ 'rotate-180': showUploadMenu }" class="transition-transform duration-200" />
            </button>
            
            <div 
              v-if="showUploadMenu" 
              ref="uploadMenuRef"
              class="absolute top-full right-0 mt-1 w-36 bg-white rounded-lg shadow-xl border border-slate-100 py-1 z-50"
            >
                <button class="w-full text-left px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 flex items-center gap-2">
                    <FileUp :size="16" class="text-slate-400" />
                    上传文件
                </button>
                <button class="w-full text-left px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 flex items-center gap-2">
                    <FolderUp :size="16" class="text-slate-400" />
                    上传文件夹
                </button>
            </div>
          </div>

          <button 
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm border border-slate-200 text-slate-600 hover:bg-slate-50 hover:border-slate-300 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
            :disabled="selectedIds.length === 0"
          >
            <Download :size="16" />
            <span>下载</span>
          </button>
        </div>
      </div>
    </template>
  </FileBrowser>
</template>

<style scoped>
</style>
