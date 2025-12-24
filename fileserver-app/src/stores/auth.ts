import { defineStore } from "pinia";
import { checkAuth } from "../services/auth";

export type LoginResult = any;
export type CurrentUser = any;

export const useAuthStore = defineStore("auth", {
  state: () => ({
    loginResult: null as LoginResult | null,
    currentUser: null as CurrentUser | null,
  }),
  actions: {
    setLoginResult(payload: LoginResult) {
      this.loginResult = payload;
    },
    setCurrentUser(payload: CurrentUser) {
      this.currentUser = payload;
    },
    async fetchCurrentUser(): Promise<void> {
      await checkAuth();
    },
    reset() {
      this.loginResult = null;
      this.currentUser = null;
    },
  },
});
