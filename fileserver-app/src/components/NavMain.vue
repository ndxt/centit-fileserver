<script setup lang="ts">
import { useRoute, useRouter } from "vue-router";
import { Cloud, ArrowRightLeft } from 'lucide-vue-next';
import { useExplorerStore } from "../stores/explorer";
import { useTransferStore } from "../stores/transfer";

const route = useRoute();
const router = useRouter();
const explorer = useExplorerStore();
const transfer = useTransferStore();

function go(path: string) {
  if (route.path.startsWith('/home')) explorer.saveState('home');
  if (path === '/home') explorer.restoreState('home');
  router.replace(path);
}

const mainMenu = [
  { path: '/home', icon: Cloud, label: '首页' },
  { path: '/transfer', icon: ArrowRightLeft, label: '传输' },
];

</script>

<template>
  <div class="w-[72px] bg-[#F7F9FC] border-r border-slate-200 flex flex-col items-center py-4 justify-between h-full shrink-0 select-none">
    <div class="flex flex-col items-center gap-4 w-full">
      <!-- Logo Placeholder -->
      <div class="w-10 h-10 mb-2 flex items-center justify-center">
        <svg viewBox="0 0 24 24" class="w-8 h-8 text-sky-500 fill-current">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
        </svg>
      </div>
      
      <button 
        v-for="item in mainMenu" 
        :key="item.path"
        :id="`nav-item-${item.path.replace('/', '')}`"
        :class="[
          'flex flex-col items-center gap-1 w-full py-2 cursor-pointer transition-colors group relative',
          route.path.startsWith(item.path) ? 'text-sky-600' : 'text-slate-500 hover:text-slate-700'
        ]" 
        @click="go(item.path)"
      >
        <div 
          :class="[
            'p-2 rounded-xl transition-all duration-200 relative',
            route.path.startsWith(item.path) ? 'bg-sky-100 text-sky-600' : 'group-hover:bg-slate-200/50'
          ]"
        >
          <component :is="item.icon" :size="22" :stroke-width="2" />
          <div v-if="item.path === '/transfer' && transfer.hasPendingOrRunning" class="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-red-500 border border-white"></div>
        </div>
        <span class="text-[10px] font-medium">{{ item.label }}</span>
      </button>
    </div>
  </div>
 </template>

<style scoped>
</style>
