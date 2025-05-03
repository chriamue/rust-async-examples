#[cfg(feature = "tokio")]
#[tokio::main]
async fn main() {
    println!("🦁 lion_runs() is called.");
    let lion_future = lion_runs(); // Nothing happens yet!
    println!("🦊 fox_runs() is called.");
    let fox_future = fox_runs(); // Nothing happens yet!
    println!("🐇 rabbit_runs() is called.");
    let _rabbit_future = rabbit_runs(); // Nothing happens yet!
    println!("(No animals are running yet...)");

    // Now we await the fox
    fox_future.await;

    // Now we await the lion
    lion_future.await;

    // We never await the rabbit!
    println!("All animals have finished running (except the rabbit 🐇, who never started)!");
}

#[cfg(feature = "tokio")]
async fn lion_runs() {
    println!("🦁 Lion is running!");
}

#[cfg(feature = "tokio")]
async fn fox_runs() {
    println!("🦊 Fox is running!");
}

#[cfg(feature = "tokio")]
async fn rabbit_runs() {
    println!("🐇 Rabbit is running!");
}

#[cfg(not(feature = "tokio"))]
fn main() {
    panic!("tokio feature needed: cargo run --example tokio_await --features tokio");
}
