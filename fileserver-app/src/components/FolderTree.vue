<script setup lang="ts">
import { defineProps, defineEmits } from "vue";
import { Folder } from 'lucide-vue-next';

const props = defineProps<{ 
  items: Array<{ id: string; name: string; children?: any[]; icon?: any }> 
  selectedId?: string
}>();
const emit = defineEmits<{ (e: 'select', id: string): void }>();
function pick(id: string) { emit('select', id); }
</script>

<template>
  <div class="w-48 bg-white border-r border-slate-100 flex flex-col py-4 shrink-0">
    <div class="px-4 space-y-1">
      <div v-for="n in props.items" :key="n.id">
        <button 
          :class="[
            'w-full text-left rounded-lg px-4 py-2.5 text-sm font-medium transition-colors flex items-center gap-3',
            (selectedId === n.id || (!selectedId && n.id === 'root')) 
              ? 'bg-sky-50 text-sky-600' 
              : 'text-slate-700 hover:bg-slate-50'
          ]" 
          @click="pick(n.id)"
        >
          <component :is="n.icon || Folder" :size="18" :stroke-width="2" class="opacity-80 shrink-0" />
          <span class="flex-1 min-w-0 truncate" :title="n.name">{{ n.name }}</span>
        </button>
        
        <div v-if="n.children" class="mt-1 space-y-1">
          <button 
            v-for="c in n.children" 
            :key="c.id" 
            :class="[
              'w-full text-left rounded-lg px-4 py-2 pl-11 text-sm transition-colors block',
              selectedId === c.id 
                ? 'bg-sky-50 text-sky-600 font-medium' 
                : 'text-slate-600 hover:bg-slate-50'
            ]" 
            @click="pick(c.id)"
          >
            <span class="truncate">{{ c.name }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
 </template>

<style scoped>
</style>
