<script setup lang="ts">
import HeaderBar from "../components/HeaderBar.vue";
import NavMain from "../components/NavMain.vue";
import FolderTree from "../components/FolderTree.vue";
import FileList from "../components/FileList.vue";
import { ref } from "vue";
import { UploadCloud, DownloadCloud, FolderPlus, Zap } from 'lucide-vue-next';

// Data mimicking the "Transfer" view in Baidu Netdisk
const sidebarItems = ref([
  { id: "upload", name: "上传", icon: UploadCloud },
  { id: "download", name: "下载", icon: DownloadCloud },
  { id: "cloud-add", name: "云添加", icon: FolderPlus },
  { id: "quick-transfer", name: "文件快传", icon: Zap }
]);

const currentSidebar = ref("download");

const files = ref([
  { id: "f1", name: "tianhe.rar", size: "1.31 GB", date: "11月6日" },
  { id: "f2", name: "前端小册4", size: "--", date: "9月23日", folder: true },
  { id: "f3", name: "前端小册2", size: "--", date: "9月23日", folder: true },
  { id: "f4", name: "前端小册3", size: "--", date: "9月23日", folder: true },
  { id: "f5", name: "【必看】找图参考指南，怎么找到我在哪张图？.png", size: "7.77 MB", date: "9月8日" },
  { id: "f6", name: "浅海-珊瑚-2 (普通).jpg", size: "2.98 MB", date: "9月8日" },
  { id: "f7", name: "浅海-海葵-3 (普通).jpg", size: "3.09 MB", date: "9月8日" },
  { id: "f8", name: "夜幕-巨型蓝洞-3.jpg", size: "5.02 MB", date: "9月8日" }
]);

const activeTab = ref('completed'); // 'downloading' | 'completed'

function onSelect(id: string) { 
  currentSidebar.value = id; 
}

function onOpen(id: string) { 
  console.log("Open file", id);
}
</script>

<template>
  <div class="h-screen w-screen bg-white flex overflow-hidden font-sans text-slate-700">
    <!-- Left Main Navigation -->
    <NavMain />
    
    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- Top Header -->
      <HeaderBar />
      
      <!-- Body: Sidebar + File List -->
      <div class="flex-1 flex min-h-0">
        <!-- Secondary Sidebar (Folder/Category Tree) -->
        <FolderTree 
          :items="sidebarItems" 
          :selected-id="currentSidebar"
          @select="onSelect" 
        />
        
        <!-- Right Content: Toolbar + File List -->
        <div class="flex-1 flex flex-col min-w-0 bg-white">
          
          <!-- Toolbar / Tabs -->
          <div class="px-6 pt-4 pb-2 shrink-0">
            <div class="flex items-center justify-between mb-4">
               <button class="flex items-center gap-1 text-xs text-sky-500 hover:text-sky-600 font-medium px-3 py-1.5 rounded hover:bg-sky-50 transition-colors">
                 <span class="text-base">🗑️</span> 清空全部记录
               </button>
               
               <div class="flex items-center gap-2 text-xs text-slate-500">
                 <span class="flex items-center gap-1">
                   <span class="w-2 h-2 bg-amber-400 rounded-full"></span>
                   <span>尊享极速流量中</span>
                 </span>
               </div>
            </div>

            <div class="flex items-center gap-8 border-b border-slate-100">
              <button 
                :class="['pb-2 text-sm font-medium transition-colors relative', activeTab === 'downloading' ? 'text-slate-800' : 'text-slate-500 hover:text-slate-700']"
                @click="activeTab = 'downloading'"
              >
                下载中(0)
                <div v-if="activeTab === 'downloading'" class="absolute bottom-0 left-1/2 -translate-x-1/2 w-4 h-0.5 bg-slate-800 rounded-full"></div>
              </button>
              
              <button 
                :class="['pb-2 text-sm font-bold transition-colors relative', activeTab === 'completed' ? 'text-slate-800' : 'text-slate-500 hover:text-slate-700']"
                @click="activeTab = 'completed'"
              >
                已完成(436)
                <div v-if="activeTab === 'completed'" class="absolute bottom-0 left-1/2 -translate-x-1/2 w-6 h-0.5 bg-slate-800 rounded-full"></div>
              </button>
            </div>
          </div>

          <!-- File List Component -->
          <FileList :files="files" @open="onOpen" />
        </div>
      </div>
    </div>
  </div>
 </template>

<style scoped>
</style>
