import { defineStore } from "pinia";
import { ref } from "vue";

export const useGlobalStore = defineStore("global", () => {
  const isLoading = ref(false);
  const loadingText = ref("加载中...");

  function startLoading(text?: string) {
    isLoading.value = true;
    if (text) loadingText.value = text;
  }

  function stopLoading() {
    isLoading.value = false;
    loadingText.value = "加载中...";
  }

  return { isLoading, loadingText, startLoading, stopLoading };
});

