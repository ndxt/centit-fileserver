<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { checkAuth } from "../services/auth";
const tips = ["正在初始化", "检查更新", "准备工作区"];
const idx = ref(0);
const router = useRouter();
onMounted(async () => {
  const t = setInterval(() => {
    idx.value = (idx.value + 1) % tips.length;
  }, 800);
  const r = await checkAuth();
  const ok = r.ok && r.data?.data?.authenticated === true;
  setTimeout(() => {
    clearInterval(t);
    router.replace(ok ? "/home" : "/login");
  }, 600);
});
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-50">
    <div class="flex flex-col items-center gap-4">
      <div class="h-12 w-12 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
      <div class="text-sm text-slate-600">{{ tips[idx] }}</div>
    </div>
  </div>
 </template>

<style scoped>
</style>
