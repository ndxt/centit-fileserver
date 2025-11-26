import { createRouter, createWebHistory } from "vue-router";
import Splash from "../pages/Splash.vue";
import Login from "../pages/Login.vue";
import Home from "../pages/Home.vue";
import { checkAuth } from "../services/auth";
import { loadAppConfig } from "../config/config";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", redirect: "/splash" },
    { path: "/splash", component: Splash },
    { path: "/login", component: Login },
    { path: "/home", component: Home, meta: { requiresAuth: true } },
    { path: "/transfer", component: () => import("../pages/Transfer.vue"), meta: { requiresAuth: true } },
  ],
});

router.beforeEach(async (to) => {
  await loadAppConfig();
  if (to.path === "/splash") return true;
  if (to.meta.requiresAuth) {
    const r = await checkAuth();
    const ok = r.ok && r.data?.data?.authenticated === true;
    if (!ok) return "/login";
  }
  if (to.path === "/login") {
    const r = await checkAuth();
    const ok = r.ok && r.data?.data?.authenticated === true;
    if (ok) return "/home";
  }
  return true;
});

export default router;
