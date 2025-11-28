use serde::Serialize;
use serde_json::Value;
use std::collections::HashMap;
use std::time::Instant;
use futures_util::StreamExt;
use tauri::Emitter;

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

#[derive(Serialize, Clone)]
struct DownloadProgressPayload {
    task_id: String,
    file_name: String,
    received: u64,
    total: Option<u64>,
    speed_bps: f64,
}

#[derive(Serialize, Clone)]
struct DownloadFinishedPayload {
    task_id: String,
    file_name: String,
    save_path: String,
}

#[derive(Serialize, Clone)]
struct DownloadErrorPayload {
    task_id: String,
    file_name: String,
    error: String,
}

#[tauri::command]
pub async fn download_file(
    app: tauri::AppHandle,
    taskId: String,
    url: String,
    fileName: String,
    dirName: Option<String>,
    maxKbps: Option<u64>,
) -> Result<String, String> {
    println!("download_start task_id={} url={} file_name={} dir_name={}", taskId, url, fileName, dirName.clone().unwrap_or_default());
    let client = crate::services::http::client();
    let resp = match client.get(&url).send().await {
        Ok(r) => r,
        Err(e) => {
            eprintln!("download_request_error task_id={} error={}", taskId, e);
            let _ = app.emit(
                "download_error",
                DownloadErrorPayload { task_id: taskId.clone(), file_name: fileName.clone(), error: e.to_string() },
            );
            return Err(e.to_string());
        }
    };
    let status = resp.status();
    let total = resp.content_length();
    println!("download_response task_id={} status={} total={:?}", taskId, status, total);

    #[cfg(target_os = "windows")]
    let mut base_dir = std::path::PathBuf::from(
        std::env::var("USERPROFILE").map_err(|e| e.to_string())?
    );
    #[cfg(target_os = "windows")]
    base_dir.push("Downloads");

    #[cfg(not(target_os = "windows"))]
    let mut base_dir = std::env::current_dir().map_err(|e| e.to_string())?;

    let dir = dirName.unwrap_or_else(|| "download".to_string());
    base_dir.push(dir);
    if let Err(e) = std::fs::create_dir_all(&base_dir) {
        eprintln!("create_dir_error task_id={} path={} error={}", taskId, base_dir.to_string_lossy(), e);
        return Err(e.to_string());
    }
    let save_path = base_dir.join(&fileName);
    let mut file = match std::fs::File::create(&save_path) {
        Ok(f) => f,
        Err(e) => {
            eprintln!("create_file_error task_id={} path={} error={}", taskId, save_path.to_string_lossy(), e);
            let _ = app.emit(
                "download_error",
                DownloadErrorPayload { task_id: taskId.clone(), file_name: fileName.clone(), error: e.to_string() },
            );
            return Err(e.to_string());
        }
    };

    use std::io::Write;
    let mut received: u64 = 0;
    let mut last_ts = Instant::now();
    let mut last_received: u64 = 0;
    let max_bps: f64 = (
        maxKbps
            .or_else(|| std::env::var("FILE_CLOUD_MAX_KBPS").ok().and_then(|v| v.parse::<u64>().ok()))
            .unwrap_or(100) as f64
    ) * 1024.0;
    let mut ema_speed: f64 = 0.0;
    let mut last_speed: f64 = 0.0;
    let mut stream = resp.bytes_stream();
    while let Some(chunk) = stream.next().await {
        match chunk {
            Ok(bytes) => {
                let sleep_secs = (bytes.len() as f64) / max_bps;
                if sleep_secs > 0.0 {
                    tokio::time::sleep(std::time::Duration::from_secs_f64(sleep_secs)).await;
                }
                if let Err(e) = file.write_all(&bytes) {
                    eprintln!("write_chunk_error task_id={} error={}", taskId, e);
                    let _ = app.emit(
                        "download_error",
                        DownloadErrorPayload { task_id: taskId.clone(), file_name: fileName.clone(), error: e.to_string() },
                    );
                    return Err(e.to_string());
                }
                received += bytes.len() as u64;
                let now = Instant::now();
                let dt = now.duration_since(last_ts).as_secs_f64();
                let mut speed = 0.0;
                if dt > 0.5 {
                    let diff = received.saturating_sub(last_received) as f64;
                    last_received = received;
                    last_ts = now;
                    let inst = diff / dt;
                    ema_speed = if ema_speed <= 0.0 { inst } else { 0.7 * ema_speed + 0.3 * inst };
                    speed = ema_speed;
                }
                let speed_to_emit = if speed > 0.0 { last_speed = speed; speed } else { last_speed };
                if speed_to_emit > 0.0 {
                    // println!("download_progress_log task_id={} received={} total={:?} speed_bps={}", taskId, received, total, speed_to_emit);
                }
                let _ = app.emit(
                    "download_progress",
                    DownloadProgressPayload { task_id: taskId.clone(), file_name: fileName.clone(), received, total, speed_bps: speed_to_emit },
                );
            }
            Err(e) => {
                eprintln!("download_stream_error task_id={} error={}", taskId, e);
                let _ = app.emit(
                    "download_error",
                    DownloadErrorPayload { task_id: taskId.clone(), file_name: fileName.clone(), error: e.to_string() },
                );
                return Err(e.to_string());
            }
        }
    }

    let save_path_str = save_path.to_string_lossy().to_string();
    println!("download_finished_log task_id={} path={}", taskId, save_path_str);
    let _ = app.emit(
        "download_finished",
        DownloadFinishedPayload { task_id: taskId.clone(), file_name: fileName.clone(), save_path: save_path_str.clone() },
    );

    Ok(save_path_str)
}
