<script setup lang="ts">
import FileBrowser from "../components/FileBrowser.vue";
import { ref } from "vue";
import { UploadCloud, DownloadCloud, CloudCheck } from 'lucide-vue-next';

const tab = ref('download');

const sidebarItems = [
  { id: 'download', name: '下载中', icon: DownloadCloud },
  { id: 'download-done', name: '已完成', icon: CloudCheck },
  { id: 'upload', name: '上传中', icon: UploadCloud },
];

// Mock files for transfer view
const files = ref([
  { id: "t1", name: "正在下载的文件.zip", size: "120 MB / 200 MB", date: "剩余 2 分钟", folder: false },
  { id: "t2", name: "已完成的文件.docx", size: "1.2 MB", date: "已完成", folder: false }
]);

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
