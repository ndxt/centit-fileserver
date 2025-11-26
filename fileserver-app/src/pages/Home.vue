<script setup lang="ts">
import { ref, computed } from "vue";
import { getJson } from "../utils/invoke";

const searchQuery = ref("");
const activeFilter = ref("all");
const uploading = ref(false);
const files = ref([
  { name: "项目文档.pdf", type: "pdf", size: "2.1 MB", modified: "2025-11-20", starred: true, shared: true },
  { name: "Logo.png", type: "image", size: "420 KB", modified: "2025-11-22", starred: false, shared: false },
  { name: "预算.xlsx", type: "sheet", size: "318 KB", modified: "2025-10-02", starred: false, shared: true },
  { name: "会议录音.mp3", type: "audio", size: "11.8 MB", modified: "2025-08-14", starred: false, shared: false },
  { name: "备份.zip", type: "archive", size: "120 MB", modified: "2025-09-01", starred: true, shared: false },
  { name: "读我.md", type: "text", size: "4 KB", modified: "2025-11-01", starred: false, shared: true },
  { name: "设计稿.fig", type: "design", size: "8.7 MB", modified: "2025-11-25", starred: false, shared: false },
]);

const apiUrl = ref("https://api.github.com/repos/tauri-apps/tauri");
const apiLoading = ref(false);
const apiError = ref("");
const apiResult = ref<any | null>(null);

async function fetchJsonFromRust() {
  apiError.value = "";
  apiLoading.value = true;
  apiResult.value = null;
  const r = await getJson(apiUrl.value);
  if (r.ok) apiResult.value = r.data; else apiError.value = r.error;
  apiLoading.value = false;
}

const filteredFiles = computed(() => {
  const q = searchQuery.value.toLowerCase();
  let list = files.value.filter((f) => f.name.toLowerCase().includes(q));
  if (activeFilter.value === "starred") list = list.filter((f) => f.starred);
  if (activeFilter.value === "shared") list = list.filter((f) => f.shared);
  return list;
});

function toggleStar(index: number) {
  files.value[index].starred = !files.value[index].starred;
}

function setFilter(key: string) {
  activeFilter.value = key;
}

function mockUpload() {
  if (uploading.value) return;
  uploading.value = true;
  setTimeout(() => {
    files.value.unshift({ name: "新上传文件.txt", type: "text", size: "1 KB", modified: "刚刚", starred: false, shared: false });
    uploading.value = false;
  }, 1200);
}
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <header class="sticky top-0 z-40 bg-white/80 backdrop-blur border-b border-slate-200">
      <div class="mx-auto max-w-7xl px-6 py-4 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="h-9 w-9 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-semibold">F</div>
          <div class="text-lg font-semibold text-slate-900">File Cloud</div>
          <div class="mx-4 h-6 w-px bg-slate-200" />
          <nav class="hidden md:flex items-center text-sm text-slate-600 gap-2">
            <span class="hover:text-slate-900 cursor-pointer">首页</span>
            <span>/</span>
            <span class="text-slate-900 font-medium">Dashboard</span>
          </nav>
        </div>
        <div class="flex items-center gap-2">
          <button class="inline-flex items-center gap-2 rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100" @click="setFilter('all')">全部</button>
          <button class="inline-flex items-center gap-2 rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100" @click="setFilter('starred')">星标</button>
          <button class="inline-flex items-center gap-2 rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100" @click="setFilter('shared')">共享</button>
          <button class="ml-2 inline-flex items-center gap-2 rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700" @click="mockUpload">
            <span v-if="!uploading">上传</span>
            <span v-else class="flex items-center gap-2">
              <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
              上传中
            </span>
          </button>
        </div>
      </div>
    </header>

    <main class="mx-auto max-w-7xl px-6 py-8">
      <section class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="text-sm text-slate-500">文件总数</div>
          <div class="mt-2 text-3xl font-semibold text-slate-900">{{ files.length }}</div>
          <div class="mt-4 h-2 rounded bg-slate-100">
            <div class="h-2 w-2/3 rounded bg-indigo-600"></div>
          </div>
        </div>
        <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="text-sm text-slate-500">已用存储</div>
          <div class="mt-2 text-3xl font-semibold text-slate-900">132 GB</div>
          <div class="mt-4 h-2 rounded bg-slate-100">
            <div class="h-2 w-1/2 rounded bg-emerald-500"></div>
          </div>
        </div>
        <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="text-sm text-slate-500">同步状态</div>
          <div class="mt-2 flex items-center gap-2 text-emerald-600">
            <span class="h-2 w-2 rounded-full bg-emerald-500"></span>
            正常
          </div>
          <div class="mt-4 text-xs text-slate-500">最近同步：5 分钟前</div>
        </div>
      </section>

      <section class="mt-8">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div class="relative w-full md:w-96">
            <input v-model="searchQuery" type="text" class="w-full rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-indigo-600 focus:outline-none" placeholder="搜索文件" />
            <div class="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-slate-400">⌘K</div>
          </div>
          <div class="flex items-center gap-2 text-sm text-slate-600">
            <span>筛选：</span>
            <span :class="['rounded-full px-3 py-1 border', activeFilter==='all' ? 'bg-indigo-50 border-indigo-200 text-indigo-700' : 'bg-white border-slate-200']">全部</span>
            <span :class="['rounded-full px-3 py-1 border', activeFilter==='starred' ? 'bg-indigo-50 border-indigo-200 text-indigo-700' : 'bg-white border-slate-200']">星标</span>
            <span :class="['rounded-full px-3 py-1 border', activeFilter==='shared' ? 'bg-indigo-50 border-indigo-200 text-indigo-700' : 'bg-white border-slate-200']">共享</span>
          </div>
        </div>

        <div class="mt-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          <div v-for="(f, i) in filteredFiles" :key="f.name" class="group rounded-xl border border-slate-200 bg-white p-4 shadow-sm hover:shadow-md hover:border-slate-300 transition">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-3">
                <div class="h-10 w-10 rounded-lg bg-slate-100 flex items-center justify-center text-slate-600">
                  <span v-if="f.type==='pdf'">PDF</span>
                  <span v-else-if="f.type==='image'">IMG</span>
                  <span v-else-if="f.type==='sheet'">XLS</span>
                  <span v-else-if="f.type==='audio'">AUD</span>
                  <span v-else-if="f.type==='archive'">ZIP</span>
                  <span v-else-if="f.type==='design'">DSN</span>
                  <span v-else>TXT</span>
                </div>
                <div>
                  <div class="text-sm font-medium text-slate-900 truncate max-w-[12rem]">{{ f.name }}</div>
                  <div class="text-xs text-slate-500">{{ f.size }} · {{ f.modified }}</div>
                </div>
              </div>
              <button class="rounded-md px-2 py-1 text-sm" :class="f.starred ? 'text-amber-500' : 'text-slate-400 hover:text-slate-600'" @click="toggleStar(i)">★</button>
            </div>
            <div class="mt-4 flex items-center justify-between">
              <div class="flex items-center gap-2 text-xs text-slate-500">
                <span v-if="f.shared" class="inline-flex items-center gap-1 rounded-full bg-slate-100 px-2 py-1">共享</span>
                <span class="inline-flex items-center gap-1 rounded-full bg-slate-100 px-2 py-1">预览</span>
              </div>
              <div class="flex items-center gap-2">
                <button class="rounded-md border border-slate-300 px-3 py-1 text-xs text-slate-700 hover:bg-slate-100">下载</button>
                <button class="rounded-md bg-indigo-600 px-3 py-1 text-xs text-white hover:bg-indigo-700">打开</button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="mt-10 grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-2 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex items-center justify-between">
            <div class="text-base font-semibold text-slate-900">最近活动</div>
            <button class="text-sm text-indigo-600 hover:text-indigo-700">查看全部</button>
          </div>
          <div class="mt-4 space-y-3">
            <div class="flex items-center justify-between">
              <div class="text-sm text-slate-700">你上传了 新上传文件.txt</div>
              <div class="text-xs text-slate-500">刚刚</div>
            </div>
            <div class="flex items中心 justify-between">
              <div class="text-sm text-slate-700">共享了 预算.xlsx 给 3 人</div>
              <div class="text-xs text-slate-500">昨天</div>
            </div>
            <div class="flex items-center justify-between">
              <div class="text-sm text-slate-700">为 备份.zip 添加星标</div>
              <div class="text-xs text-slate-500">本周</div>
            </div>
          </div>
        </div>
        <div class="rounded-xl border border-slate-200 bg白 p-6 shadow-sm">
          <div class="text-base font-semibold text-slate-900">快速操作</div>
          <div class="mt-4 grid grid-cols-2 gap-3">
            <button class="rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-700 hover:bg-slate-100" @click="mockUpload">上传文件</button>
            <button class="rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-700 hover:bg-slate-100">新建文件夹</button>
            <button class="rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-700 hover:bg-slate-100">共享设置</button>
            <button class="rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-700 hover:bg-slate-100">清理空间</button>
          </div>
        </div>
      </section>

      <section class="mt-10 grid grid-cols-1 gap-6">
        <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex items-center justify-between">
            <div class="text-base font-semibold text-slate-900">接口调试</div>
          </div>
          <div class="mt-4 flex flex-col md:flex-row gap-3">
            <input v-model="apiUrl" type="text" class="flex-1 rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-indigo-600 focus:outline-none" placeholder="输入请求地址" />
            <button class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700" @click="fetchJsonFromRust">
              <span v-if="!apiLoading">请求</span>
              <span v-else class="flex items-center gap-2">
                <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
                加载中
              </span>
            </button>
          </div>
          <div class="mt-4">
            <div v-if="apiError" class="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{{ apiError }}</div>
            <div v-else-if="apiResult" class="rounded-lg border border-slate-200 bg-slate-50 p-4">
              <pre class="text-xs overflow-auto max-h-64"><code>{{ JSON.stringify(apiResult, null, 2) }}</code></pre>
            </div>
            <div v-else class="text-sm text-slate-500">结果将显示在这里</div>
          </div>
        </div>
      </section>
    </main>
  </div>
 </template>

<style scoped>
</style>
