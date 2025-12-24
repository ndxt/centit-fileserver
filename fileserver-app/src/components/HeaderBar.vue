<script setup lang="ts">
import { ref, computed } from "vue";
import { ArrowLeft, ArrowRight, RotateCcw, Search, Settings, Shirt } from 'lucide-vue-next';
import { useExplorerStore } from "../stores/explorer";
const q = ref("");
const explorer = useExplorerStore();
const canBack = computed(() => explorer.canBack());
const canForward = computed(() => explorer.canForward());
function goBack() { explorer.back(); }
function goForward() { explorer.forward(); }
function refresh() { explorer.requestRefresh(); }
</script>

<template>
  <div class="h-16 bg-white border-b border-slate-100 flex items-center justify-between px-4 shrink-0 select-none">
    <!-- Left: Navigation & Breadcrumb -->
    <div class="flex items-center gap-4">
      <div class="flex items-center gap-1 text-slate-400">
        <button class="p-1 hover:bg-slate-100 rounded text-slate-600 disabled:opacity-30" :disabled="!canBack" @click="goBack">
          <ArrowLeft :size="20" />
        </button>
        <button class="p-1 hover:bg-slate-100 rounded text-slate-600 disabled:opacity-30" :disabled="!canForward" @click="goForward">
          <ArrowRight :size="20" />
        </button>
      </div>
      <button class="p-1 text-slate-600 hover:bg-slate-100 rounded" @click="refresh">
        <RotateCcw :size="18" />
      </button>
    </div>

    <!-- Center: Search -->
    <div class="flex-1 max-w-2xl px-8">
      <div class="relative group">
        <div class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-sky-500 transition-colors">
          <Search :size="18" />
        </div>
        <input 
          v-model="q" 
          type="text" 
          class="w-full h-10 rounded-full bg-slate-100 border-none pl-10 pr-4 text-sm text-slate-700 placeholder-slate-400 focus:ring-2 focus:ring-sky-500/20 focus:bg-white transition-all" 
          placeholder="搜网盘文件、全网资源资讯" 
        />
      </div>
    </div>

    <!-- Right: User & Actions -->
    <div class="flex items-center gap-3">
      <button class="p-2 text-slate-500 hover:bg-slate-100 rounded-full transition-colors">
        <Shirt :size="18" />
      </button>
      
      <button class="p-2 text-slate-500 hover:bg-slate-100 rounded-full transition-colors">
        <Settings :size="18" />
      </button>

      <div class="relative ml-2 cursor-pointer">
        <div class="h-8 w-8 rounded-full bg-slate-200 overflow-hidden border border-slate-100">
           <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" alt="avatar" />
        </div>
        <div class="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-green-500 border-2 border-white rounded-full"></div>
      </div>
    </div>
  </div>
 </template>

<style scoped>
</style>
