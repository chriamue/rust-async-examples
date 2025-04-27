use crate::tasks::redis::*;
use mini_redis::Result;

#[tokio::test]
async fn tokio_redis() -> Result<()> {
    // Open a connection to the mini-redis address.
    let mut client = init_client().await?;

    // Set the key "hello" with value "world"
    set(&mut client, "hello", "world".into()).await?;

    // Get key "hello"
    let result = get(&mut client, "hello").await?;

    println!("got value from the server; result={:?}", result);

    assert_eq!(result, Some("world".into()));

    Ok(())
}
