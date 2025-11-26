import { createApp } from "vue";
import "./styles/tailwind.css";
import App from "./AppRoot.vue";
import router from "./router";

createApp(App).use(router).mount("#app");
