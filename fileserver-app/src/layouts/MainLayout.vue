<script setup lang="ts">
import HeaderBar from "../components/HeaderBar.vue";
import NavMain from "../components/NavMain.vue";
</script>

<template>
  <div class="h-screen w-screen bg-white flex overflow-hidden font-sans text-slate-700">
    <!-- Left Main Navigation -->
    <NavMain />
    
    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- Top Header -->
      <HeaderBar />
      
      <!-- Body: Content from nested routes -->
      <router-view />
    </div>
  </div>
</template>

