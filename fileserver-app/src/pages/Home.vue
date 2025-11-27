<script setup lang="ts">
import FileBrowser from "../components/FileBrowser.vue";
import { ref } from "vue";
import { House, Building } from 'lucide-vue-next';

// Data mimicking the "Transfer" view in Baidu Netdisk
const sidebarItems = ref([
  { id: "home", name: "我的", icon: House },
  { id: "centit", name: "南大先腾", icon: Building },
  { id: "tmc", name: "技术管理中心" },
]);

const currentSidebar = ref("download");

const files = ref([
  { id: "f1", name: "项目资料", size: "--", date: "11月20日", folder: true },
  { id: "f2", name: "banner.png", size: "320 KB", date: "11月19日" },
  { id: "f3", name: "backup.zip", size: "820 MB", date: "11月18日", encrypted: true },
  { id: "f4", name: "track.mp3", size: "9.6 MB", date: "11月17日" },
  { id: "f5", name: "movie.mp4", size: "1.6 GB", date: "11月16日" },
  { id: "f6", name: "report.xlsx", size: "1.2 MB", date: "11月15日", encrypted: true },
  { id: "f7", name: "app.ts", size: "42 KB", date: "11月14日" },
  { id: "f8", name: "manual.pdf", size: "2.4 MB", date: "11月13日" },
  { id: "f9", name: "proposal.docx", size: "856 KB", date: "11月12日" },
  { id: "f10", name: "slides.pptx", size: "4.1 MB", date: "11月11日" },
  { id: "f11", name: "readme.md", size: "12 KB", date: "11月10日" },
  { id: "f12", name: "design.psd", size: "86 MB", date: "11月9日" },
  { id: "f13", name: "logo.ai", size: "4.8 MB", date: "11月8日" },
  { id: "f14", name: "mock.sketch", size: "18.5 MB", date: "11月7日", encrypted: true },
  { id: "f15", name: "android-app.apk", size: "34 MB", date: "11月6日" },
  { id: "f16", name: "setup.exe", size: "22 MB", date: "11月5日" },
  { id: "f17", name: "Inter.woff2", size: "128 KB", date: "11月4日" },
  { id: "f18", name: "dump.sql", size: "9.3 MB", date: "11月3日", encrypted: true },
  { id: "f19", name: "deploy.sh", size: "3 KB", date: "11月2日" },
  { id: "f20", name: "server.crt", size: "4 KB", date: "11月1日", encrypted: true }
]);

function onOpen(id: string) { 
  console.log("Open file", id);
}
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
