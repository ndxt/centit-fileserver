<script setup lang="ts">
import FileBrowser from "../components/FileBrowser.vue";
import { ref, onMounted, watch, computed } from "vue";
import { listLibraries, listLibraryFiles } from "../services/files";
import { House, Building2 } from "lucide-vue-next";
import { useAuthStore } from "../stores/auth";
import { useGlobalStore } from "../stores/global";
import { useExplorerStore } from "../stores/explorer";
import Breadcrumb from "../components/Breadcrumb.vue";

const auth = useAuthStore();
const global = useGlobalStore();
const explorer = useExplorerStore();
const sidebarItems = ref<any[]>([]);
const files = ref<any[]>([]);
const currentLibraryName = computed(() => explorer.libraryName || "根目录");

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
      const firstId = items[0]?.id || r1.data[0].libraryId;
      const firstName = items[0]?.name || r1.data[0].libraryName;
      explorer.setLibrary(firstId, firstName);
      await loadFiles(explorer.libraryId, explorer.folderId);
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
    await loadFiles(explorer.libraryId, explorer.folderId);
  } else {
    console.log("Open file", id);
  }
}

onMounted(() => { init(); });

watch(() => explorer.libraryId, async (id) => {
  if (id) {
    const s = sidebarItems.value.find((x: any) => x.id === id);
    explorer.setLibrary(id, s?.name || "");
    await loadFiles(explorer.libraryId, explorer.folderId);
  }
});

watch(() => explorer.folderId, async (fid) => {
  if (explorer.libraryId) {
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
    v-model:selected-sidebar-id="explorer.libraryId"
    :files="files"
    @open-file="onOpen"
  >
    <template #header>
      <Breadcrumb :items="explorer.path" :root-name="currentLibraryName" @goto-root="explorer.gotoRoot" @goto-index="explorer.gotoIndex" />
    </template>
  </FileBrowser>
</template>

<style scoped>
</style>
