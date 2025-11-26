import { createApp } from "vue";
import "./styles/tailwind.css";
import Home from "./pages/Home.vue";
import Login from "./pages/Login.vue";
import { checkAuth } from "./services/auth";

(async () => {
  const r = await checkAuth();
  const authenticated = r.ok && r.data?.data?.authenticated === true;
  const Root = authenticated ? Home : Login;
  createApp(Root).mount("#app");
})();
