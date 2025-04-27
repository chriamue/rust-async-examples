use bytes::Bytes;

use mini_redis::{
    Result,
    client::{Client, connect},
};

pub async fn init_client() -> Result<Client> {
    connect("127.0.0.1:6379").await
}

pub async fn set(client: &mut Client, key: &str, value: Bytes) -> Result<()> {
    client.set(key, value).await
}

pub async fn get(client: &mut Client, key: &str) -> Result<Option<Bytes>> {
    client.get(key).await
}
