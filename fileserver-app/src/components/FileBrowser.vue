<script setup lang="ts">
import FolderTree from "./FolderTree.vue";
import FileList from "./FileList.vue";
import { useGlobalStore } from "../stores/global";

defineProps<{
  sidebarItems: any[];
  files: any[];
  selectedSidebarId: string;
}>();

const emit = defineEmits<{
  (e: 'update:selectedSidebarId', id: string): void;
  (e: 'open-file', id: string): void;
}>();

const global = useGlobalStore();
function onSelect(id: string) {
  if (global.isLoading) return;
  emit('update:selectedSidebarId', id);
}
</script>

<template>
  <div class="flex-1 flex min-h-0">
    <!-- Sidebar -->
    <FolderTree 
      :items="sidebarItems" 
      :selected-id="selectedSidebarId"
      @select="onSelect" 
    />
    
    <!-- Content -->
    <div class="flex-1 flex flex-col min-w-0 bg-white">
      <slot name="header" />
      <FileList :files="files" @open="$emit('open-file', $event)" />
    </div>
  </div>
</template>

<style scoped>
</style>

