use serde::Serialize;
use serde_json::Value;
use std::collections::HashMap;

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

#[tauri::command]
pub async fn http_request_json(
    method: String,
    url: String,
    headers: Option<Vec<(String, String)>>,
    json: Option<Value>,
    form: Option<HashMap<String, String>>,
) -> Result<Value, String> {
    crate::services::http::request_json(&method, &url, headers, json, form).await
}

#[tauri::command]
pub async fn api_libraries(base: String) -> Result<Value, String> {
    let url = format!("{}/api/fileserver/fileserver/library", base);
    crate::services::http::fetch_json(&url).await
}

#[tauri::command]
pub async fn api_folder_files(base: String, library_id: String, folder_id: String) -> Result<Value, String> {
    let url = format!(
        "{}/api/fileserver/fileserver/folder/{}/{}",
        base, library_id, folder_id
    );
    crate::services::http::fetch_json(&url).await
}
