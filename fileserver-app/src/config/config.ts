export type AppConfig = {
  locodeOrigin: string;
  useLdapDefault: boolean;
};

const defaultConfig: AppConfig = {
  locodeOrigin: "https://cloud.centit.com/locode",
  useLdapDefault: false,
};

let current: AppConfig | null = null;
let loading: Promise<void> | null = null;

export async function loadAppConfig(): Promise<void> {
  if (current) return;
  if (loading) return loading;
  loading = (async () => {
    try {
      const resp = await fetch("/app.config.json");
      if (resp.ok) {
        const data = await resp.json();
        current = {
          locodeOrigin: String(data.locodeOrigin || defaultConfig.locodeOrigin),
          useLdapDefault: Boolean(data.useLdapDefault ?? defaultConfig.useLdapDefault),
        };
        return;
      }
    } catch {}
    current = defaultConfig;
  })();
  return loading;
}

export function getConfig(): AppConfig {
  return current || defaultConfig;
}
