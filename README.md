# rust-async-examples
Examples to explain rust async

## Prerequisites

- Rust installed (https://www.rust-lang.org/tools/install)
- Cargo installed (https://doc.rust-lang.org/cargo/getting-started/installation.html)
- Mini-Redis installed (https://docs.rs/mini-redis/0.4.1/mini_redis/)

```sh
cargo install mini-redis
```

## Examples

### Async HTTP POST using Tokio runtime

```sh
cargo run --example tokio_http_post --features tokio
```

---

### Thread Hierarchy (std::thread)

```sh
cargo run --example thread_hierarchy
```

---

### Thread Deadlock (std::thread)

```sh
cargo run --example thread_deadlock
```

---

### Thread Poisoned (std::thread)

```sh
cargo run --example thread_poisoned
```

---

### Thread Hierarchy with Panics (std::thread)

```sh
cargo run --example thread_hierarchy_panics
```

---

### Async Task Hierarchy with Panics (Tokio)

```sh
cargo run --example tokio_hierarchy_panics --features tokio
```

### Tokio Thread Pool Demo (configurable worker threads)

You can control the number of worker threads in the Tokio thread pool using the `TOKIO_WORKER_THREADS` environment variable:

```sh
# Run with 1 worker thread (default)
cargo run --example tokio_threadpool --features tokio

# Run with 3 worker threads
TOKIO_WORKER_THREADS=3 cargo run --example tokio_threadpool --features tokio
```

This demonstrates how thread pool size affects concurrency for CPU-bound tasks.


## Sources

https://rust-lang.github.io/async-book/08_ecosystem/00_chapter.html
https://www.youtube.com/watch?v=GA8NZVGPiNo

### Scala

#### Sync, Async, Blocking and Non-Blocking | Rock the JVM

[![Sync, Async, Blocking and Non-Blocking | Rock the JVM](https://img.youtube.com/vi/Hlu-zYeNsSU/0.jpg)](https://www.youtube.com/watch?v=Hlu-zYeNsSU)

[Watch on YouTube](https://www.youtube.com/watch?v=Hlu-zYeNsSU)

---

#### Adam Rosien - Async/Await for the Monadic Programmer | Scala Days 2023 Seattle

[![Adam Rosien - Async/Await for the Monadic Programmer | Scala Days 2023 Seattle](https://img.youtube.com/vi/OH5cxLNTTPo/0.jpg)](https://www.youtube.com/watch?v=OH5cxLNTTPo)

[Watch on YouTube](https://www.youtube.com/watch?v=OH5cxLNTTPo)

---

#### Scala Programming - Introduction to Threads and Futures

[![Scala Programming - Introduction to Threads and Futures](https://img.youtube.com/vi/6b24sszy6Js/0.jpg)](https://youtu.be/6b24sszy6Js)

[Watch on YouTube](https://youtu.be/6b24sszy6Js)

---

## Slides

Start slides with:

```sh
python3 -m http.server
```
