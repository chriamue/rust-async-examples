#[cfg(feature = "tokio")]
use rust_async_examples::simple_http_client::SimpleHttpClient;

#[cfg(feature = "tokio")]
#[tokio::main]
async fn main() -> Result<(), std::io::Error> {
    let test_body = "Hello from World!";
    let response = SimpleHttpClient::post("http://httpbin.org/post", test_body).await?;
    println!("POST response: {}", response);
    assert!(response.contains(test_body));
    Ok(())
}

#[cfg(not(feature = "tokio"))]
fn main() {
    panic!("tokio feature needed: cargo run --example tokio_http_post");
}
