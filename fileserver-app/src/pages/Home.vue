<script setup lang="ts">
import FileBrowser from "../components/FileBrowser.vue";
import { ref, onMounted, watch } from "vue";
import { listLibraries, listLibraryFiles } from "../services/files";
import { House, Building2 } from "lucide-vue-next";
import { useAuthStore } from "../stores/auth";
import { useGlobalStore } from "../stores/global";

const auth = useAuthStore();
const global = useGlobalStore();
const sidebarItems = ref<any[]>([]);
const currentSidebar = ref<string>("");
const files = ref<any[]>([]);

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
      await loadFiles(currentSidebar.value);
    } else {
      sidebarItems.value = [];
      currentSidebar.value = "";
      files.value = [];
    }
  } finally {
    global.stopLoading();
  }
}

async function loadFiles(libId: string) {
  try {
    global.startLoading("加载文件中...");
    const r2 = await listLibraryFiles(libId, "-1");
    files.value = r2.ok && r2.data ? r2.data : [];
  } finally {
    global.stopLoading();
  }
}

function onOpen(id: string) { 
  console.log("Open file", id);
}

onMounted(() => { init(); });

watch(currentSidebar, async (id) => {
  if (id) await loadFiles(id);
});
</script>

<template>
  <FileBrowser 
    :sidebar-items="sidebarItems"
    v-model:selected-sidebar-id="currentSidebar"
    :files="files"
    @open-file="onOpen"
  />
</template>

<style scoped>
</style>
