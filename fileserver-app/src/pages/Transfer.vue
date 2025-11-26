<script setup lang="ts">
import HeaderBar from "../components/HeaderBar.vue";
import NavMain from "../components/NavMain.vue";
import { ref } from "vue";
const tab = ref<'upload'|'download'>('download');
</script>

<template>
  <div class="h-screen w-screen bg-slate-50 flex">
    <NavMain />
    <div class="flex-1 flex flex-col">
      <HeaderBar />
      <div class="flex">
        <div class="w-56 bg-white border-r border-slate-200 p-3 space-y-2">
          <button :class="['w-full text-left rounded-md px-2 py-1', tab==='upload' ? 'bg-indigo-50 text-indigo-700' : 'hover:bg-slate-100']" @click="tab='upload'">上传</button>
          <button :class="['w-full text-left rounded-md px-2 py-1', tab==='download' ? 'bg-indigo-50 text-indigo-700' : 'hover:bg-slate-100']" @click="tab='download'">下载</button>
        </div>
        <div class="flex-1">
          <div class="p-4 text-sm text-slate-600">{{ tab==='upload' ? '上传任务' : '下载任务' }}</div>
        </div>
      </div>
    </div>
  </div>
 </template>

<style scoped>
</style>
