<script setup lang="ts">
import { defineProps, defineEmits, computed } from "vue";
import { ChevronRight } from "lucide-vue-next";

const props = defineProps<{ 
  items: Array<{ id: string; name: string }>;
  rootName: string;
}>();
const emit = defineEmits<{ (e: 'goto-root'): void; (e: 'goto-index', index: number): void }>();

function gotoRoot() { emit('goto-root'); }
function gotoIndex(i: number) { emit('goto-index', i); }

const maxSegments = 5; // root + up to 4 levels
const maxItems = maxSegments - 1; // items excludes root

const total = computed(() => props.items.length);
const hasEllipsis = computed(() => total.value > maxItems);
const visibleItems = computed(() => {
  if (!hasEllipsis.value) {
    return props.items.map((p, i) => ({ ...p, idx: i }));
  }
  const start = total.value - 3; // show last 3 items
  return props.items.slice(start).map((p, i) => ({ ...p, idx: start + i }));
});
</script>

<template>
  <div class="flex items-center gap-2 select-none whitespace-nowrap overflow-hidden text-ellipsis">
    <button class="text-sm text-slate-600 hover:text-sky-600 font-medium transition-colors shrink-0" @click="gotoRoot">{{ rootName }}</button>
    <ChevronRight :size="16" class="text-slate-300 shrink-0" />
    <div class="flex items-center gap-2 overflow-hidden">
      <span v-if="hasEllipsis" class="text-sm text-slate-400 shrink-0">...</span>
      <ChevronRight v-if="hasEllipsis" :size="16" class="text-slate-300 shrink-0" />
      <template v-for="(p, i) in visibleItems" :key="p.id">
        <button 
          v-if="i < visibleItems.length - 1"
          class="max-w-[160px] text-sm text-slate-700 hover:text-sky-600 font-medium truncate shrink-0"
          @click="gotoIndex(p.idx)"
          :title="p.name"
        >
          {{ p.name }}
        </button>
        <span 
          v-else 
          class="max-w-[160px] text-sm font-semibold truncate text-sky-600 shrink-0"
          :title="p.name"
        >
          {{ p.name }}
        </span>
        <ChevronRight v-if="i < visibleItems.length - 1" :size="16" class="text-slate-300 shrink-0" />
      </template>
    </div>
  </div>
</template>

<style scoped>
</style>
