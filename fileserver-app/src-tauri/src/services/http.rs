use serde_json::Value;

pub async fn fetch_json(url: &str) -> Result<Value, String> {
    let resp = reqwest::Client::new()
        .get(url)
        .send()
        .await
        .map_err(|e| e.to_string())?;
    let status = resp.status();
    if !status.is_success() {
        return Err(format!("{}", status));
    }
    resp.json::<Value>().await.map_err(|e| e.to_string())
}