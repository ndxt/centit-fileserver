<script setup lang="ts">
import { defineProps, computed } from 'vue';
import { Folder, Image, FileArchive, Music, Video, Table, Code, FileText, Palette, PenTool, Diamond, Package, Cpu, Type, Database, Terminal, Shield } from 'lucide-vue-next';

const props = defineProps<{ name: string; folder?: boolean; size?: number; strokeWidth?: number }>();

const ext = computed(() => {
  const n = (props.name || '').toLowerCase();
  return n.includes('.') ? n.split('.').pop() as string : '';
});

function iconFor(extName: string, folder?: boolean) {
  if (folder) return Folder;
  if (["jpg","jpeg","png","gif","bmp","webp","svg"].includes(extName)) return Image;
  if (["zip","rar","7z","tar","gz","bz2"].includes(extName)) return FileArchive;
  if (["mp3","wav","flac","m4a","aac","ogg"].includes(extName)) return Music;
  if (["mp4","mkv","avi","mov","wmv","webm","flv"].includes(extName)) return Video;
  if (["xls","xlsx","csv"].includes(extName)) return Table;
  if (["js","ts","tsx","jsx","java","py","go","rs","c","cpp","json","yaml","yml","xml"].includes(extName)) return Code;
  if (["pdf","txt","md","log","doc","docx","ppt","pptx"].includes(extName)) return FileText;
  if (["psd"].includes(extName)) return Palette;
  if (["ai"].includes(extName)) return PenTool;
  if (["sketch"].includes(extName)) return Diamond;
  if (["apk"].includes(extName)) return Package;
  if (["exe"].includes(extName)) return Cpu;
  if (["ttf","otf","woff","woff2"].includes(extName)) return Type;
  if (["sql","db","sqlite"].includes(extName)) return Database;
  if (["sh","bat","cmd","ps1"].includes(extName)) return Terminal;
  if (["key","pem","cer","crt"].includes(extName)) return Shield;
  return FileText;
}

function colorFor(extName: string, folder?: boolean) {
  if (folder) return 'text-amber-400';
  if (["jpg","jpeg","png","gif","bmp","webp","svg"].includes(extName)) return 'text-orange-400';
  if (["zip","rar","7z","tar","gz","bz2"].includes(extName)) return 'text-violet-500';
  if (["mp3","wav","flac","m4a","aac","ogg"].includes(extName)) return 'text-emerald-500';
  if (["mp4","mkv","avi","mov","wmv","webm","flv"].includes(extName)) return 'text-indigo-500';
  if (["xls","xlsx","csv"].includes(extName)) return 'text-green-600';
  if (["js","ts","tsx","jsx","java","py","go","rs","c","cpp","json","yaml","yml","xml"].includes(extName)) return 'text-sky-500';
  if (["pdf"].includes(extName)) return 'text-red-500';
  if (["doc","docx"].includes(extName)) return 'text-blue-600';
  if (["ppt","pptx"].includes(extName)) return 'text-amber-500';
  if (["psd"].includes(extName)) return 'text-sky-600';
  if (["ai"].includes(extName)) return 'text-orange-500';
  if (["sketch"].includes(extName)) return 'text-amber-500';
  if (["apk"].includes(extName)) return 'text-green-600';
  if (["exe"].includes(extName)) return 'text-purple-600';
  if (["ttf","otf","woff","woff2"].includes(extName)) return 'text-pink-600';
  if (["sql","db","sqlite"].includes(extName)) return 'text-emerald-600';
  if (["sh","bat","cmd","ps1"].includes(extName)) return 'text-slate-600';
  if (["key","pem","cer","crt"].includes(extName)) return 'text-rose-500';
  return 'text-slate-400';
}

const IconComp = computed(() => iconFor(ext.value, props.folder));
const colorClass = computed(() => colorFor(ext.value, props.folder));
</script>

<template>
  <component :is="IconComp" :size="props.size || 24" :stroke-width="props.strokeWidth || 1.5" :class="colorClass" />
</template>

<style scoped>
</style>
