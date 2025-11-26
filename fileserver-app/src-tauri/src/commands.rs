use serde::Serialize;

#[derive(Serialize)]
pub struct AppInfo {
    pub name: String,
    pub version: String,
    pub ts: u64,
}

#[tauri::command]
pub fn app_info() -> AppInfo {
    let ts = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .unwrap()
        .as_secs();
    AppInfo {
        name: "file-cloud".into(),
        version: "0.1.0".into(),
        ts,
    }
}

#[tauri::command]
pub fn greet(name: &str) -> String {
    format!("Hello, {}! You've been greeted from Rust!", name)
}

#[tauri::command]
pub async fn http_get_json(url: String) -> Result<serde_json::Value, String> {
    crate::services::http::fetch_json(&url).await
}