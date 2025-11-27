pub mod services;
pub mod commands;

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![
            commands::greet,
            commands::app_info,
            commands::http_get_json,
            commands::http_request_json,
            commands::api_libraries,
            commands::api_folder_files,
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
