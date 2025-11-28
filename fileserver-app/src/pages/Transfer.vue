<script setup lang="ts">
import FileBrowser from "../components/FileBrowser.vue";
import { ref, computed } from "vue";
import { UploadCloud, DownloadCloud, CloudCheck } from 'lucide-vue-next';
import { useTransferStore } from "../stores/transfer";

const tab = ref('download');

const sidebarItems = [
  { id: 'download', name: '下载中', icon: DownloadCloud },
  { id: 'download-wait', name: '等待中', icon: DownloadCloud },
  { id: 'download-done', name: '已完成', icon: CloudCheck },
  { id: 'upload', name: '上传中', icon: UploadCloud },
];

const transfer = useTransferStore();
const files = computed(() => {
  if (tab.value === 'download') {
    return transfer.downloading.map(t => ({
      id: t.id,
      name: t.name,
      size: `${transfer.displaySize(t.received)} / ${transfer.displaySize(t.total)} (${Math.round(t.progress)}%)`,
      date: transfer.displaySpeed(t.speedBps),
      folder: false,
    }));
  }
  if (tab.value === 'download-wait') {
    return transfer.queue.map(t => ({
      id: t.id,
      name: t.name,
      size: '--',
      date: '等待中',
      folder: false,
    }));
  }
  if (tab.value === 'download-done') {
    return transfer.done.map(t => ({
      id: t.id,
      name: t.name,
      size: t.total ? transfer.displaySize(t.total) : "",
      date: t.status === 'failed' ? '失败' : '已完成',
      folder: false,
    }));
  }
  return [];
});

function onOpen(id: string) {
  console.log("Open transfer item", id);
}
</script>

<template>
  <FileBrowser 
    :sidebar-items="sidebarItems"
    v-model:selected-sidebar-id="tab"
    :files="files"
    @open-file="onOpen"
  />
</template>

<style scoped>
</style>
