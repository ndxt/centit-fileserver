<script setup lang="ts">
import { defineProps, defineEmits, computed, ref, onMounted, onUnmounted } from "vue";
import { MoreHorizontal, Lock, Trash2, Copy, FolderInput, Star, Check } from 'lucide-vue-next';
import FileIcon from './FileIcon.vue';

const props = defineProps<{ files: Array<{ id: string; name: string; size: string; date: string; folder?: boolean; encrypted?: boolean }>; selectedIds?: string[] }>();
const emit = defineEmits<{ 
  (e: 'open', id: string): void; 
  (e: 'update:selected-ids', ids: string[]): void;
  (e: 'delete', file: any): void;
  (e: 'copy', file: any): void;
  (e: 'move', file: any): void;
  (e: 'toggle-favorite', file: any): void;
}>();

function open(id: string) { emit('open', id); }

const selectedSet = computed(() => new Set(props.selectedIds || []));
const allSelected = computed(() => {
  const ids = (props.files || []).map(f => f.id);
  const sel = selectedSet.value;
  return ids.length > 0 && ids.every(id => sel.has(id));
});

function toggle(id: string) {
  const current = new Set(props.selectedIds || []);
  if (current.has(id)) current.delete(id); else current.add(id);
  emit('update:selected-ids', Array.from(current));
}

function toggleAll() {
  const ids = (props.files || []).map(f => f.id);
  const setAll = !allSelected.value;
  emit('update:selected-ids', setAll ? ids : []);
}

const activeDropdownId = ref<string | null>(null);

function toggleMenu(id: string) {
  if (activeDropdownId.value === id) {
    activeDropdownId.value = null;
  } else {
    activeDropdownId.value = id;
  }
}

function closeMenu() {
  activeDropdownId.value = null;
}

function handleClickOutside() {
  // Dropdowns are closed when clicking outside
  // The trigger button uses @click.stop so this won't fire on trigger click
  if (activeDropdownId.value) {
    closeMenu();
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});

function onAction(action: 'delete' | 'copy' | 'move' | 'toggle-favorite', file: any) {
  closeMenu();
  switch (action) {
    case 'delete':
      emit('delete', file);
      break;
    case 'copy':
      emit('copy', file);
      break;
    case 'move':
      emit('move', file);
      break;
    case 'toggle-favorite':
      emit('toggle-favorite', file);
      break;
  }
}
</script>

<template>
  <div class="flex-1 bg-white flex flex-col min-h-0">
    <!-- List Header -->
    <div class="grid grid-cols-[auto_1fr_120px_150px] gap-4 px-4 py-3 text-xs text-slate-500 border-b border-slate-100 select-none"> 
      <div class="w-5 flex items-center justify-center">
        <div 
          class="w-4 h-4 border rounded flex items-center justify-center cursor-pointer transition-all duration-200"
          :class="allSelected ? 'bg-sky-500 border-sky-500' : 'bg-white border-slate-300 hover:border-sky-500'"
          @click="toggleAll"
        >
          <Check v-if="allSelected" :size="12" class="text-white" stroke-width="3" />
        </div>
      </div>
      <div>文件名</div>
      <div>大小</div>
      <div>修改日期</div>
    </div>
    
    <!-- List Body -->
    <div class="overflow-y-auto flex-1 custom-scrollbar">
      <div 
        v-for="(f, index) in props.files" 
        :key="f.id" 
        class="group grid grid-cols-[auto_1fr_120px_150px] gap-4 px-4 py-3 hover:bg-[#F0F5FF] border-b border-slate-50 transition-colors cursor-pointer items-center"
        @click="open(f.id)"
      >
        <!-- Checkbox -->
        <div class="w-5 flex items-center justify-center" @click.stop>
          <div 
            class="w-4 h-4 border rounded flex items-center justify-center cursor-pointer transition-all duration-200"
            :class="selectedSet.has(f.id) 
              ? 'bg-sky-500 border-sky-500' 
              : 'bg-white border-slate-300 group-hover:border-sky-400'"
            @click="toggle(f.id)"
          >
            <Check v-if="selectedSet.has(f.id)" :size="12" class="text-white" stroke-width="3" />
          </div>
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

          <!-- Hover Actions (Hidden by default, shown on group hover or if active) -->
          <div 
            class="items-center gap-2 ml-auto pr-4 relative"
            :class="activeDropdownId === f.id ? 'flex' : 'hidden group-hover:flex'"
            @click.stop
          >
             <button 
               class="p-1 hover:bg-sky-100 rounded text-sky-600 transition-colors"
               :class="{ 'bg-sky-100': activeDropdownId === f.id }"
               @click="toggleMenu(f.id)"
             >
               <MoreHorizontal :size="16" />
             </button>
             
             <!-- Dropdown -->
             <div 
               v-if="activeDropdownId === f.id" 
               class="absolute right-0 w-32 bg-white rounded-lg shadow-xl border border-slate-100 py-1 z-50"
               :class="index > props.files.length - 4 ? 'bottom-full mb-1' : 'top-full mt-1'"
             >
                <button class="w-full text-left px-3 py-2 text-xs text-slate-700 hover:bg-slate-50 flex items-center gap-2" @click="onAction('copy', f)">
                    <Copy :size="14" class="text-slate-400" />
                    复制
                </button>
                <button class="w-full text-left px-3 py-2 text-xs text-slate-700 hover:bg-slate-50 flex items-center gap-2" @click="onAction('move', f)">
                    <FolderInput :size="14" class="text-slate-400" />
                    转移
                </button>
                <button class="w-full text-left px-3 py-2 text-xs text-slate-700 hover:bg-slate-50 flex items-center gap-2" @click="onAction('toggle-favorite', f)">
                    <Star :size="14" class="text-slate-400" />
                    收藏
                </button>
                <div class="my-1 border-t border-slate-100"></div>
                <button class="w-full text-left px-3 py-2 text-xs text-red-600 hover:bg-red-50 flex items-center gap-2" @click="onAction('delete', f)">
                    <Trash2 :size="14" class="text-red-500" />
                    删除
                </button>
             </div>
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
