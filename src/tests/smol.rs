use crate::simple_http_client::SimpleHttpClient;
use std::io::Result;

#[test]
fn smol_http_post() -> Result<()> {
    smol::block_on(async {
        let test_body = "Hello from World!";
        let response = SimpleHttpClient::post("http://httpbin.org/post", test_body).await?;
        println!("POST response: {}", response);
        assert!(response.contains(test_body));
        Ok(())
    })
}
