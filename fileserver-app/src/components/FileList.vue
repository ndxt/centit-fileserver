<script setup lang="ts">
import { defineProps, defineEmits } from "vue";
import { MoreHorizontal, Lock } from 'lucide-vue-next';
import FileIcon from './FileIcon.vue';

const props = defineProps<{ files: Array<{ id: string; name: string; size: string; date: string; folder?: boolean; encrypted?: boolean }> }>();
const emit = defineEmits<{ (e: 'open', id: string): void }>();
function open(id: string) { emit('open', id); }

</script>

<template>
  <div class="flex-1 bg-white flex flex-col min-h-0">
    <!-- List Header -->
    <div class="grid grid-cols-[auto_1fr_120px_150px] gap-4 px-4 py-3 text-xs text-slate-500 border-b border-slate-100 select-none"> 
      <div class="w-5 flex items-center justify-center">
        <div class="w-3.5 h-3.5 border-2 border-slate-300 rounded hover:border-sky-500 cursor-pointer"></div>
      </div>
      <div>文件名</div>
      <div>大小</div>
      <div>修改日期</div>
    </div>
    
    <!-- List Body -->
    <div class="overflow-y-auto flex-1 custom-scrollbar">
      <div 
        v-for="f in props.files" 
        :key="f.id" 
        class="group grid grid-cols-[auto_1fr_120px_150px] gap-4 px-4 py-3 hover:bg-[#F0F5FF] border-b border-slate-50 transition-colors cursor-pointer items-center"
        @click="open(f.id)"
      >
        <!-- Checkbox -->
        <div class="w-5 flex items-center justify-center" @click.stop>
          <div class="w-3.5 h-3.5 border-2 border-slate-300 rounded group-hover:border-sky-500 group-hover:bg-white cursor-pointer"></div>
        </div>

        <!-- Name & Icon -->
        <div class="flex items-center gap-3 min-w-0">
          <FileIcon :name="f.name" :folder="f.folder" :size="24" :stroke-width="1.5" class="shrink-0" />
          
          <div class="flex items-center gap-1.5 min-w-0">
            <div class="truncate text-sm text-slate-700 group-hover:text-sky-600 font-medium" :title="f.name">
              {{ f.name }}
            </div>
            <Lock v-if="f.encrypted" :size="14" class="text-amber-500 shrink-0" />
          </div>

          <!-- Hover Actions (Hidden by default, shown on group hover) -->
          <div class="hidden group-hover:flex items-center gap-2 ml-auto pr-4">
             <button class="p-1 hover:bg-sky-100 rounded text-sky-600">
               <MoreHorizontal :size="16" />
             </button>
          </div>
        </div>

        <!-- Size -->
        <div class="text-xs text-slate-500">{{ f.size }}</div>

        <!-- Date -->
        <div class="text-xs text-slate-400">{{ f.date }}</div>
      </div>
      
      <!-- Load More / Empty State -->
      <div class="py-8 text-center text-xs text-slate-400 hover:text-sky-500 cursor-pointer">
      </div>
    </div>
  </div>
 </template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #E2E8F0;
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #CBD5E1;
}
</style>
