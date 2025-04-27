use std::io;

// A simple runtime-agnostic HTTP client
pub struct SimpleHttpClient;

impl SimpleHttpClient {
    // Makes an HTTP GET request and returns the response body
    pub async fn get(url: &str) -> io::Result<String> {
        #[cfg(feature = "tokio")]
        {
            return Self::get_with_tokio(url).await;
        }

        #[cfg(feature = "smol")]
        {
            return Self::get_with_smol(url).await;
        }

        #[cfg(not(any(feature = "tokio", feature = "smol")))]
        {
            return Err(io::Error::new(
                io::ErrorKind::Other,
                "No async runtime feature enabled",
            ));
        }
    }

    // Makes an HTTP POST request with a text body and returns the response
    pub async fn post(url: &str, body: &str) -> io::Result<String> {
        #[cfg(feature = "tokio")]
        {
            return Self::post_with_tokio(url, body).await;
        }

        #[cfg(feature = "smol")]
        {
            return Self::post_with_smol(url, body).await;
        }

        #[cfg(not(any(feature = "tokio", feature = "smol")))]
        {
            return Err(io::Error::new(
                io::ErrorKind::Other,
                "No async runtime feature enabled",
            ));
        }
    }

    // Rest of the implementation remains the same...
    #[cfg(feature = "tokio")]
    async fn get_with_tokio(url: &str) -> io::Result<String> {
        use tokio::io::{AsyncReadExt, AsyncWriteExt};
        use tokio::net::TcpStream;

        // Parse URL to get host and path
        let url =
            url::Url::parse(url).map_err(|e| io::Error::new(io::ErrorKind::InvalidInput, e))?;
        let host = url
            .host_str()
            .ok_or_else(|| io::Error::new(io::ErrorKind::InvalidInput, "Missing host"))?;
        let path = url.path();
        let port = url.port().unwrap_or(80);

        // Connect to the server
        let mut stream = TcpStream::connect(format!("{}:{}", host, port)).await?;

        // Send HTTP GET request
        let request = format!(
            "GET {} HTTP/1.1\r\nHost: {}\r\nConnection: close\r\n\r\n",
            path, host
        );
        stream.write_all(request.as_bytes()).await?;

        // Read response
        let mut response = String::new();
        stream.read_to_string(&mut response).await?;

        // Extract body (simplified - just getting content after the first empty line)
        if let Some(idx) = response.find("\r\n\r\n") {
            Ok(response[idx + 4..].to_string())
        } else {
            Ok(response)
        }
    }

    #[cfg(feature = "tokio")]
    async fn post_with_tokio(url: &str, body: &str) -> io::Result<String> {
        // Implementation unchanged
        use tokio::io::{AsyncReadExt, AsyncWriteExt};
        use tokio::net::TcpStream;

        // Parse URL to get host and path
        let url =
            url::Url::parse(url).map_err(|e| io::Error::new(io::ErrorKind::InvalidInput, e))?;
        let host = url
            .host_str()
            .ok_or_else(|| io::Error::new(io::ErrorKind::InvalidInput, "Missing host"))?;
        let path = url.path();
        let port = url.port().unwrap_or(80);

        // Connect to the server
        let mut stream = TcpStream::connect(format!("{}:{}", host, port)).await?;

        // Send HTTP POST request
        let request = format!(
            "POST {} HTTP/1.1\r\nHost: {}\r\nContent-Type: text/plain\r\nContent-Length: {}\r\nConnection: close\r\n\r\n{}",
            path, host, body.len(), body
        );
        stream.write_all(request.as_bytes()).await?;

        // Read response
        let mut response = String::new();
        stream.read_to_string(&mut response).await?;

        // Extract body (simplified - just getting content after the first empty line)
        if let Some(idx) = response.find("\r\n\r\n") {
            Ok(response[idx + 4..].to_string())
        } else {
            Ok(response)
        }
    }

    #[cfg(feature = "smol")]
    async fn get_with_smol(url: &str) -> io::Result<String> {
        // Implementation unchanged
        use smol::io::{AsyncReadExt, AsyncWriteExt};
        use smol::net::TcpStream;

        // Parse URL to get host and path
        let url =
            url::Url::parse(url).map_err(|e| io::Error::new(io::ErrorKind::InvalidInput, e))?;
        let host = url
            .host_str()
            .ok_or_else(|| io::Error::new(io::ErrorKind::InvalidInput, "Missing host"))?;
        let path = url.path();
        let port = url.port().unwrap_or(80);

        // Connect to the server
        let mut stream = TcpStream::connect(format!("{}:{}", host, port)).await?;

        // Send HTTP GET request
        let request = format!(
            "GET {} HTTP/1.1\r\nHost: {}\r\nConnection: close\r\n\r\n",
            path, host
        );
        stream.write_all(request.as_bytes()).await?;

        // Read response
        let mut response = String::new();
        stream.read_to_string(&mut response).await?;

        // Extract body (simplified - just getting content after the first empty line)
        if let Some(idx) = response.find("\r\n\r\n") {
            Ok(response[idx + 4..].to_string())
        } else {
            Ok(response)
        }
    }

    #[cfg(feature = "smol")]
    async fn post_with_smol(url: &str, body: &str) -> io::Result<String> {
        // Implementation unchanged
        use smol::io::{AsyncReadExt, AsyncWriteExt};
        use smol::net::TcpStream;

        // Parse URL to get host and path
        let url =
            url::Url::parse(url).map_err(|e| io::Error::new(io::ErrorKind::InvalidInput, e))?;
        let host = url
            .host_str()
            .ok_or_else(|| io::Error::new(io::ErrorKind::InvalidInput, "Missing host"))?;
        let path = url.path();
        let port = url.port().unwrap_or(80);

        // Connect to the server
        let mut stream = TcpStream::connect(format!("{}:{}", host, port)).await?;

        // Send HTTP POST request
        let request = format!(
            "POST {} HTTP/1.1\r\nHost: {}\r\nContent-Type: text/plain\r\nContent-Length: {}\r\nConnection: close\r\n\r\n{}",
            path, host, body.len(), body
        );
        stream.write_all(request.as_bytes()).await?;

        // Read response
        let mut response = String::new();
        stream.read_to_string(&mut response).await?;

        // Extract body (simplified - just getting content after the first empty line)
        if let Some(idx) = response.find("\r\n\r\n") {
            Ok(response[idx + 4..].to_string())
        } else {
            Ok(response)
        }
    }
}
