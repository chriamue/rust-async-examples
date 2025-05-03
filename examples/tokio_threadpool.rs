use std::env;
use std::time::{Duration, Instant};

fn main() {
    // Get worker thread count from environment variable or default to 1
    let worker_threads: usize = env::var("TOKIO_WORKER_THREADS")
        .ok()
        .and_then(|s| s.parse().ok())
        .unwrap_or(1);

    let rt = tokio::runtime::Builder::new_multi_thread()
        .worker_threads(worker_threads)
        .enable_all()
        .build()
        .unwrap();

    rt.block_on(async_main(worker_threads));
}

async fn async_main(worker_threads: usize) {
    let start = Instant::now();

    println!(
        "Starting 3 animals with a thread pool of {} worker threads...",
        worker_threads
    );

    let lion = tokio::spawn(async {
        println!("🦁 Lion starts running!");
        // will not run, await is missing
        cpu_bound_task("🦁 Lion");
        println!("🦁 Lion finished running!");
    });

    let fox = tokio::spawn(async {
        println!("🦊 Fox starts running!");
        cpu_bound_task("🦊 Fox").await;
        println!("🦊 Fox finished running!");
    });

    let rabbit = tokio::spawn(async {
        println!("🐇 Rabbit starts running!");
        cpu_bound_task("🐇 Rabbit").await;
        println!("🐇 Rabbit finished running!");
    });

    // Wait for all animals to finish
    let _ = tokio::join!(lion, fox, rabbit);

    println!(
        "All animals finished! Total time: {:.2} seconds",
        start.elapsed().as_secs_f64()
    );
}

// This function will busy-loop for about 1 second
async fn cpu_bound_task(name: &str) {
    std::thread::sleep(Duration::from_millis(1000));
    println!("{} is running!", name);
}
