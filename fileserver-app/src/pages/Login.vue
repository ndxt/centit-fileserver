<script setup lang="ts">
import { useRouter } from "vue-router";
const router = useRouter();
import { ref, onMounted } from "vue";
import { captchaUrl, loginCommon, loginLdap } from "../services/auth";
import { loadAppConfig, getConfig } from "../config/config";
import { useAuthStore } from "../stores/auth";

const username = ref("");
const password = ref("");
const captcha = ref("");
const useLdap = ref(false);
const loading = ref(false);
const error = ref("");
const imgUrl = ref(captchaUrl());
const auth = useAuthStore();

function refreshCaptcha() {
  imgUrl.value = captchaUrl();
}

async function submit() {
  if (loading.value) return;
  error.value = "";
  loading.value = true;
  const r = useLdap.value
    ? await loginLdap(username.value, password.value, captcha.value)
    : await loginCommon(username.value, password.value, captcha.value);
  loading.value = false;
  if (r.ok) {
    const code = r.data?.code;
    if (code === 0) {
      auth.setLoginResult(r.data);
      await auth.fetchCurrentUser();
      router.replace("/home");
    } else {
      error.value = r.data?.message || "登录失败";
      refreshCaptcha();
    }
  } else {
    error.value = r.error;
    refreshCaptcha();
  }
}

onMounted(async () => {
  await loadAppConfig();
  useLdap.value = getConfig().useLdapDefault;
});
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-50">
    <div class="w-full max-w-sm rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div class="text-lg font-semibold text-slate-900">登录</div>
      <div class="mt-4 space-y-3">
        <input v-model="username" type="text" class="w-full rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-indigo-600 focus:outline-none" placeholder="用户名" />
        <input v-model="password" type="password" class="w-full rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-indigo-600 focus:outline-none" placeholder="密码" />
        <div class="flex items-center gap-2">
          <input v-model="captcha" type="text" class="flex-1 rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-indigo-600 focus:outline-none" placeholder="验证码" />
          <img :src="imgUrl" alt="captcha" class="h-10 w-24 rounded border border-slate-200 cursor-pointer" @click="refreshCaptcha" />
        </div>
        <label class="flex items-center gap-2 text-sm text-slate-600">
          <input type="checkbox" v-model="useLdap" /> 使用 LDAP 登录
        </label>
        <button class="w-full rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700" @click="submit">
          <span v-if="!loading">登录</span>
          <span v-else class="flex items-center justify-center gap-2">
            <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
            处理中
          </span>
        </button>
        <div v-if="error" class="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{{ error }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
