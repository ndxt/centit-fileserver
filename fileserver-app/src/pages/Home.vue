<script setup lang="ts">
import FileBrowser from "../components/FileBrowser.vue";
import { ref, onMounted, watch, computed } from "vue";
import { listLibraries, listLibraryFiles } from "../services/files";
import { House, Building2 } from "lucide-vue-next";
import { useAuthStore } from "../stores/auth";
import { useGlobalStore } from "../stores/global";
import Breadcrumb from "../components/Breadcrumb.vue";

const auth = useAuthStore();
const global = useGlobalStore();
const sidebarItems = ref<any[]>([]);
const currentSidebar = ref<string>("");
const files = ref<any[]>([]);
const currentFolder = ref<string>("-1");
const path = ref<Array<{ id: string; name: string }>>([]);
const currentLibraryName = computed(() => {
  const id = currentSidebar.value;
  const s = sidebarItems.value.find((x: any) => x.id === id);
  return s?.name || "根目录";
});

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
      currentSidebar.value = items[0]?.id || r1.data[0].libraryId;
      currentFolder.value = "-1";
      path.value = [];
      await loadFiles(currentSidebar.value, currentFolder.value);
    } else {
      sidebarItems.value = [];
      currentSidebar.value = "";
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
    currentFolder.value = id;
    path.value = [...path.value, { id, name: entry.name }];
    await loadFiles(currentSidebar.value, id);
  } else {
    console.log("Open file", id);
  }
}

onMounted(() => { init(); });

watch(currentSidebar, async (id) => {
  if (id) {
    currentFolder.value = "-1";
    path.value = [];
    await loadFiles(id, currentFolder.value);
  }
});

async function gotoRoot() {
  currentFolder.value = "-1";
  path.value = [];
  await loadFiles(currentSidebar.value, currentFolder.value);
}

async function gotoIndex(i: number) {
  const target = path.value[i];
  if (!target) return;
  path.value = path.value.slice(0, i + 1);
  currentFolder.value = target.id;
  await loadFiles(currentSidebar.value, currentFolder.value);
}
</script>

<template>
  <FileBrowser 
    :sidebar-items="sidebarItems"
    v-model:selected-sidebar-id="currentSidebar"
    :files="files"
    @open-file="onOpen"
  >
    <template #header>
      <Breadcrumb :items="path" :root-name="currentLibraryName" @goto-root="gotoRoot" @goto-index="gotoIndex" />
    </template>
  </FileBrowser>
</template>

<style scoped>
</style>
