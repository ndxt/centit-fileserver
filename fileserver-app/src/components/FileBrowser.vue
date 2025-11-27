<script setup lang="ts">
import FolderTree from "./FolderTree.vue";
import FileList from "./FileList.vue";

defineProps<{
  sidebarItems: any[];
  files: any[];
  selectedSidebarId: string;
}>();

defineEmits<{
  (e: 'update:selectedSidebarId', id: string): void;
  (e: 'open-file', id: string): void;
}>();
</script>

<template>
  <div class="flex-1 flex min-h-0">
    <!-- Sidebar -->
    <FolderTree 
      :items="sidebarItems" 
      :selected-id="selectedSidebarId"
      @select="$emit('update:selectedSidebarId', $event)" 
    />
    
    <!-- Content -->
    <div class="flex-1 flex flex-col min-w-0 bg-white">
      <FileList :files="files" @open="$emit('open-file', $event)" />
    </div>
  </div>
</template>

<style scoped>
</style>

