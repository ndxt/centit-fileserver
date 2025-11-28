<script setup lang="ts">
import FileBrowser from "../components/FileBrowser.vue";
import { ref, computed } from "vue";
import { UploadCloud, DownloadCloud, CloudCheck } from 'lucide-vue-next';
import { useTransferStore } from "../stores/transfer";

const tab = ref('download');

const sidebarItems = [
  { id: 'download', name: '下载中', icon: DownloadCloud },
  { id: 'download-done', name: '已完成', icon: CloudCheck },
  { id: 'upload', name: '上传中', icon: UploadCloud },
];

const transfer = useTransferStore();
const files = computed(() => {
  if (tab.value === 'download') {
    const downloading = transfer.downloading.map(t => ({
      id: t.id,
      name: t.name,
      size: `${transfer.displaySize(t.received)} / ${transfer.displaySize(t.total)}`,
      date: '', // Not used when progress is present
      folder: false,
      progress: t.progress,
      status: 'downloading',
      speed: transfer.displaySpeed(t.speedBps),
      eta: (() => {
        if (!t.total || t.total <= 0 || t.speedBps <= 0) return undefined;
        const remaining = t.total - t.received;
        const etaSec = remaining > 0 ? Math.floor(remaining / t.speedBps) : 0;
        return transfer.displayEta(etaSec);
      })()
    }));
    const waiting = (transfer as any).queue.map((t: any) => ({
      id: t.id,
      name: t.name,
      size: `${transfer.displaySize(t.received)} / ${transfer.displaySize(t.total)}`,
      date: '',
      folder: false,
      progress: 0,
      status: 'waiting',
      // no speed/eta for waiting
    }));
    return [...downloading, ...waiting];
  }
  if (tab.value === 'download-done') {
    return transfer.done.map(t => ({
      id: t.id,
      name: t.name,
      size: t.total ? transfer.displaySize(t.total) : "",
      date: t.status === 'failed' ? '失败' : '已完成',
      folder: false,
      // No progress bar for done items, show date/status text instead as before
      // Or if you want progress bar for done items too:
      progress: t.status === 'failed' ? 0 : 100,
      status: t.status === 'failed' ? 'failed' : 'done'
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
    list-type="transfer"
    @open-file="onOpen"
  />
</template>

<style scoped>
</style>
