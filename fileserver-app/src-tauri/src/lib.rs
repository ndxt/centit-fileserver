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
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
