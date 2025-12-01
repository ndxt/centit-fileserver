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
    eta_secs: Option<u64>,
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

#[derive(Serialize, Clone)]
struct TransferFolderContentPayload {
    files: Vec<TransferItem>,
}

#[derive(Serialize, Clone)]
#[serde(rename_all = "camelCase")]
struct TransferItem {
    id: String,
    name: String,
    url: String,
    library_id: Option<String>,
    is_folder: bool,
    dir_name: Option<String>,
    status: String,
    progress: u64,
    received: u64,
    total: Option<u64>,
    speed_bps: u64,
}

#[tauri::command]
pub async fn download_file(
    app: tauri::AppHandle,
    task_id: String,
    url: String,
    file_name: String,
    dir_name: Option<String>,
    max_kbps: Option<u64>,
    is_folder: Option<bool>,
    library_id: Option<String>,
) -> Result<String, String> {
    
    println!("download_start task_id={} url={} file_name={} dir_name={} is_folder={:?}", task_id, url, file_name, dir_name.clone().unwrap_or_default(), is_folder);

    if is_folder.unwrap_or(false) {
        #[cfg(target_os = "windows")]
        let mut base_dir = std::path::PathBuf::from(
            std::env::var("USERPROFILE").map_err(|e| e.to_string())?
        );
        #[cfg(target_os = "windows")]
        base_dir.push("Downloads");

        #[cfg(not(target_os = "windows"))]
        let mut base_dir = std::env::current_dir().map_err(|e| e.to_string())?;

        let current_rel_dir = dir_name.clone().unwrap_or_else(|| "download".to_string());
        println!("folder download: current_rel_dir='{}', file_name='{}'", current_rel_dir, file_name);
        base_dir.push(&current_rel_dir);
        println!("folder download: base_dir after push={}", base_dir.to_string_lossy());
        
        // For the folder itself, create it
        let folder_path = base_dir.join(&file_name);
        println!("folder download: folder_path to create={}", folder_path.to_string_lossy());
        if let Err(e) = std::fs::create_dir_all(&folder_path) {
            let _ = app.emit(
                "download_error",
                DownloadErrorPayload { task_id: task_id.clone(), file_name: file_name.clone(), error: e.to_string() },
            );
            return Err(e.to_string());
        }

        // Calculate the relative path for children: current_rel_dir + / + file_name
        // We use PathBuf to handle separators correctly but need string for API
        let child_rel_dir = std::path::Path::new(&current_rel_dir).join(&file_name).to_string_lossy().to_string();

        let parse_base = |u: &str| -> Option<(String, String)> {
            let marker = "/fileserver/fileserver/";
            let idx = u.find(marker)?;
            let base = u[..idx].to_string();
            let dl_marker = "/folder/download/";
            let di = u.find(dl_marker)?;
            let start = di + dl_marker.len();
            let end = u[start..].find('?').map(|i| start + i).unwrap_or(u.len());
            let folder_id = u[start..end].to_string();
            Some((base, folder_id))
        };

        let (base, root_folder_id) = parse_base(&url).ok_or_else(|| "invalid folder download url".to_string())?;

        // We need to find the correct library ID to list files.
        // If libraryId is passed, use it. Otherwise try to find it (expensive).
        let library_id_val = if let Some(lid) = library_id.clone() {
            lid
        } else {
            // Try to guess library ID (fallback)
            let libs_v = crate::services::http::fetch_json(&format!("{}/fileserver/fileserver/library", base)).await?;
            let mut lib_ids: Vec<String> = vec![];
            if let Some(arr) = libs_v.get("data").and_then(|d| d.get("objList")).and_then(|x| x.as_array()) {
                lib_ids = arr.iter().filter_map(|x| x.get("libraryId").and_then(|v| v.as_str()).map(|s| s.to_string())).collect();
            }
            if lib_ids.is_empty() {
                return Err("no libraries".to_string());
            }
            let mut used_lib: Option<String> = None;
            for lib_id in lib_ids.iter() {
                let url_list = format!("{}/fileserver/fileserver/folder/{}/{}", base, lib_id, root_folder_id);
                let r = crate::services::http::fetch_json(&url_list).await;
                if let Ok(v) = r {
                    let arr_opt = v.get("data").and_then(|d| d.get("objList")).and_then(|x| x.as_array()).or_else(|| v.get("data").and_then(|x| x.as_array()));
                    if arr_opt.is_some() {
                        used_lib = Some(lib_id.clone());
                        break;
                    }
                }
            }
            used_lib.ok_or_else(|| "folder not found in any library".to_string())?
        };

        // Fetch children
        let url_list = format!("{}/fileserver/fileserver/folder/{}/{}", base, library_id_val, root_folder_id);
        println!("fetching folder contents from: {}", url_list);
        let v = crate::services::http::fetch_json(&url_list).await?;
        println!("API response: {}", serde_json::to_string_pretty(&v).unwrap_or_else(|_| "failed to serialize".to_string()));
        let mut new_items: Vec<TransferItem> = vec![];

        let empty_vec = vec![];
        let entries = v.get("data").and_then(|d| d.get("objList")).and_then(|x| x.as_array())
            .or_else(|| v.get("data").and_then(|x| x.as_array()))
            .unwrap_or(&empty_vec);
        
        println!("found {} entries in API response", entries.len());

        for it in entries {
            let is_dir = it.get("folder").and_then(|v| v.as_bool()).unwrap_or(false);
            let name = it.get("fileName").and_then(|v| v.as_str()).unwrap_or("").to_string();
            let id = if is_dir {
                it.get("folderId").and_then(|v| v.as_str()).unwrap_or("").to_string()
            } else {
                // For files, API returns 'accessToken' instead of 'fileId'
                // Try accessToken first, then fileId as fallback
                it.get("accessToken")
                    .and_then(|v| v.as_str())
                    .or_else(|| it.get("fileId").and_then(|v| v.as_str()))
                    .unwrap_or("")
                    .to_string()
            };

            println!("  processing entry: name='{}', id='{}', is_dir={}", name, id, is_dir);
            
            if id.is_empty() || name.is_empty() { 
                println!("  SKIPPED: empty id or name");
                continue; 
            }

            // Construct URL for child
            // Frontend expects: 
            // if folder: `${base}/fileserver/fileserver/folder/download/${id}?${params}`
            // if file:   `${base}/fileserver/fileserver/download/downloadwithauth/${id}?${params}`
            // We need to reconstruct params. The original 'url' has params.
            let query_start = url.find('?').unwrap_or(url.len());
            let query_str = if query_start < url.len() { &url[query_start+1..] } else { "" };
            
            // We need to replace 'accessToken' param with new id
            let mut new_query = String::new();
            for pair in query_str.split('&') {
                let mut parts = pair.splitn(2, '=');
                let key = parts.next().unwrap_or("");
                let val = parts.next().unwrap_or("");
                if key == "accessToken" {
                    new_query.push_str(&format!("accessToken={}&", id));
                } else if !key.is_empty() {
                    new_query.push_str(&format!("{}={}&", key, val));
                }
            }
            // Remove trailing &
            if new_query.ends_with('&') { new_query.pop(); }

            let child_url = if is_dir {
                format!("{}/fileserver/fileserver/folder/download/{}?{}", base, id, new_query)
            } else {
                format!("{}/fileserver/fileserver/download/downloadwithauth/{}?{}", base, id, new_query)
            };

            new_items.push(TransferItem {
                id,
                name,
                url: child_url,
                library_id: Some(library_id_val.clone()),
                is_folder: is_dir,
                dir_name: Some(child_rel_dir.clone()), // Pass the new relative directory
                status: "queued".to_string(),
                progress: 0,
                received: 0,
                total: None,
                speed_bps: 0,
            });
        }

        println!("expanding folder {} -> {} items, child_rel_dir={}", task_id, new_items.len(), child_rel_dir);
        for item in &new_items {
            println!("  - {} (is_folder={}, dir_name={:?})", item.name, item.is_folder, item.dir_name);
        }
        
        // Emit event to frontend to add these items to queue
        let _ = app.emit("transfer_folder_content", TransferFolderContentPayload { files: new_items });

        // Mark this folder task as finished (it's just a generator)
        // We use the path of the folder we created as 'save_path'
        let save_path_str = folder_path.to_string_lossy().to_string();
        println!("folder expansion finished, created directory: {}", save_path_str);
        let _ = app.emit(
            "download_finished",
            DownloadFinishedPayload { task_id: task_id.clone(), file_name: file_name.clone(), save_path: save_path_str.clone() },
        );
        return Ok(save_path_str);
    }

    let client = crate::services::http::client();
    let resp = match client.get(&url).send().await {
        Ok(r) => r,
        Err(e) => {
            eprintln!("download_request_error task_id={} error={}", task_id, e);
            let _ = app.emit(
                "download_error",
                DownloadErrorPayload { task_id: task_id.clone(), file_name: file_name.clone(), error: e.to_string() },
            );
            return Err(e.to_string());
        }
    };
    let status = resp.status();
    let total = resp.content_length();
    println!("download_response task_id={} status={} total={:?}", task_id, status, total);

    #[cfg(target_os = "windows")]
    let mut base_dir = std::path::PathBuf::from(
        std::env::var("USERPROFILE").map_err(|e| e.to_string())?
    );
    #[cfg(target_os = "windows")]
    base_dir.push("Downloads");

    #[cfg(not(target_os = "windows"))]
    let mut base_dir = std::env::current_dir().map_err(|e| e.to_string())?;

    let dir = dir_name.clone().unwrap_or_else(|| "download".to_string());
    base_dir.push(&dir);
    println!("file download: dir_name={:?}, base_dir before create={}, file_name={}", dir_name, base_dir.to_string_lossy(), file_name);
    if let Err(e) = std::fs::create_dir_all(&base_dir) {
        eprintln!("create_dir_error task_id={} path={} error={}", task_id, base_dir.to_string_lossy(), e);
        return Err(e.to_string());
    }
    let save_path = base_dir.join(&file_name);
    println!("file download: final save_path={}", save_path.to_string_lossy());
    let mut file = match std::fs::File::create(&save_path) {
        Ok(f) => f,
        Err(e) => {
            eprintln!("create_file_error task_id={} path={} error={}", task_id, save_path.to_string_lossy(), e);
            let _ = app.emit(
                "download_error",
                DownloadErrorPayload { task_id: task_id.clone(), file_name: file_name.clone(), error: e.to_string() },
            );
            return Err(e.to_string());
        }
    };

    use std::io::Write;
    let mut received: u64 = 0;
    let mut last_ts = Instant::now();
    let mut last_received: u64 = 0;
    let max_bps: f64 = (
        max_kbps
            .or_else(|| std::env::var("FILE_CLOUD_MAX_KBPS").ok().and_then(|v| v.parse::<u64>().ok()))
            .unwrap_or(1024) as f64
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
                    eprintln!("write_chunk_error task_id={} error={}", task_id, e);
                    let _ = app.emit(
                        "download_error",
                        DownloadErrorPayload { task_id: task_id.clone(), file_name: file_name.clone(), error: e.to_string() },
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
                let eta_secs = if let Some(tot) = total {
                    if speed_to_emit > 0.0 {
                        let remaining = if tot > received { tot - received } else { 0 };
                        Some(((remaining as f64) / speed_to_emit).round() as u64)
                    } else {
                        None
                    }
                } else {
                    None
                };
                let _ = app.emit(
                    "download_progress",
                    DownloadProgressPayload { task_id: task_id.clone(), file_name: file_name.clone(), received, total, speed_bps: speed_to_emit, eta_secs },
                );
            }
            Err(e) => {
                eprintln!("download_stream_error task_id={} error={}", task_id, e);
                let _ = app.emit(
                    "download_error",
                    DownloadErrorPayload { task_id: task_id.clone(), file_name: file_name.clone(), error: e.to_string() },
                );
                return Err(e.to_string());
            }
        }
    }

    let save_path_str = save_path.to_string_lossy().to_string();
    println!("download_finished_log task_id={} path={}", task_id, save_path_str);
    let _ = app.emit(
        "download_finished",
        DownloadFinishedPayload { task_id: task_id.clone(), file_name: file_name.clone(), save_path: save_path_str.clone() },
    );

    Ok(save_path_str)
}
