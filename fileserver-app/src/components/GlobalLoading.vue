<script setup lang="ts">
import { useGlobalStore } from "../stores/global";
import { Loader2 } from "lucide-vue-next";

const globalStore = useGlobalStore();
</script>

<template>
  <Transition
    enter-active-class="transition ease-out duration-200"
    enter-from-class="opacity-0 scale-95"
    enter-to-class="opacity-100 scale-100"
    leave-active-class="transition ease-in duration-150"
    leave-from-class="opacity-100 scale-100"
    leave-to-class="opacity-0 scale-95"
  >
    <div 
      v-if="globalStore.isLoading"
      class="fixed top-24 left-1/2 -translate-x-1/2 z-[9999] flex items-center gap-3 px-5 py-2.5 bg-white shadow-[0_8px_30px_rgb(0,0,0,0.12)] border border-slate-100 rounded-full select-none pointer-events-none"
    >
      <Loader2 class="w-4 h-4 text-indigo-600 animate-spin" />
      <span class="text-sm font-medium text-slate-700">{{ globalStore.loadingText }}</span>
    </div>
  </Transition>
</template>

<style scoped>
</style>
