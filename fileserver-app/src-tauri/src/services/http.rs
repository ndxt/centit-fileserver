use serde_json::Value;
use std::collections::HashMap;
use once_cell::sync::Lazy;

static CLIENT: Lazy<reqwest::Client> = Lazy::new(|| {
    reqwest::Client::builder()
        .cookie_store(true)
        .build()
        .unwrap()
});

pub async fn fetch_json(url: &str) -> Result<Value, String> {
    let resp = CLIENT
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

pub async fn request_json(
    method: &str,
    url: &str,
    headers: Option<Vec<(String, String)>>,
    json: Option<Value>,
    form: Option<HashMap<String, String>>,
) -> Result<Value, String> {
    let mut req = match method.to_uppercase().as_str() {
        "GET" => CLIENT.get(url),
        "POST" => CLIENT.post(url),
        "PUT" => CLIENT.put(url),
        "PATCH" => CLIENT.patch(url),
        "DELETE" => CLIENT.delete(url),
        _ => CLIENT.get(url),
    };
    if let Some(hs) = headers {
        for (k, v) in hs {
            req = req.header(k, v);
        }
    }
    if let Some(j) = json {
        req = req.json(&j);
    }
    if let Some(f) = form {
        req = req.form(&f);
    }
    let resp = req.send().await.map_err(|e| e.to_string())?;
    let status = resp.status();
    if !status.is_success() {
        return Err(format!("{}", status));
    }
    resp.json::<Value>().await.map_err(|e| e.to_string())
}
