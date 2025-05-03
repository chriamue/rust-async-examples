#[cfg(feature = "tokio")]
use std::time::Instant;
#[cfg(feature = "tokio")]
use tokio::time::{sleep, Duration};

#[cfg(feature = "tokio")]
#[tokio::main]
async fn main() {
    let world_start = Instant::now();
    println!("🌍 World: 🚀 starting");

    // Spawn Mammal branch
    let mammal = tokio::spawn(async {
        let mammal_start = Instant::now();
        println!("  🐾 Mammal: 🚀 started (child of World)");

        // Spawn Lion (child of Mammal)
        let lion = tokio::spawn(async {
            let start = Instant::now();
            println!("    🦁 Lion: 🚀 started (child of Mammal)");
            sleep(Duration::from_millis(100)).await;
            println!(
                "    🦁 Lion: ✅ finished in {} ms",
                start.elapsed().as_millis()
            );
        });

        // Spawn Tiger (child of Mammal)
        let tiger = tokio::spawn(async {
            let start = Instant::now();
            println!("    🐯 Tiger: 🚀 started (child of Mammal)");
            sleep(Duration::from_millis(100)).await;
            println!(
                "    🐯 Tiger: ✅ finished in {} ms",
                start.elapsed().as_millis()
            );
        });

        // Spawn Bear (child of Mammal)
        let bear = tokio::spawn(async {
            let bear_start = Instant::now();
            println!("    🐻 Bear: 🚀 started (child of Mammal)");

            // Fruits as sub-tasks (children of Bear)
            let apple = tokio::spawn(async {
                let start = Instant::now();
                println!("      🍎 Apple: 🚀 started (child of Bear)");
                sleep(Duration::from_millis(550)).await;
                println!(
                    "      🍎 Apple: ✅ finished in {} ms",
                    start.elapsed().as_millis()
                );
            });
            let banana = tokio::spawn(async {
                let start = Instant::now();
                println!("      🍌 Banana: 🚀 started (child of Bear)");
                sleep(Duration::from_millis(150)).await;
                println!(
                    "      🍌 Banana: ✅ finished in {} ms",
                    start.elapsed().as_millis()
                );
            });
            let cherry = tokio::spawn(async {
                let start = Instant::now();
                println!("      🍒 Cherry: 🚀 started (child of Bear)");
                sleep(Duration::from_millis(50)).await;
                println!(
                    "      🍒 Cherry: ✅ finished in {} ms",
                    start.elapsed().as_millis()
                );
            });

            sleep(Duration::from_millis(100)).await;

            println!(
                "    🐻 Bear: 💥 panicking now! Fruits finished: 🍎{:?}, 🍌{:?}, 🍒{:?}",
                apple.is_finished(),
                banana.is_finished(),
                cherry.is_finished()
            );
            panic!("Bear panicked");
            // If you .await here, fruits will always finish:
            // let _ = apple.await;
            // let _ = banana.await;
            // let _ = cherry.await;
        });

        // Wait for Lion, Tiger, and Bear to finish
        let _ = lion.await;
        let _ = tiger.await;
        let bear_result = bear.await;
        match bear_result {
            Ok(_) => println!("    🐻 Bear: ✅ finished normally"),
            Err(_) => println!("    🐻 Bear: 💥 panicked and was joined"),
        }

        println!(
            "  🐾 Mammal: ✅ finished (all children done) in {} ms",
            mammal_start.elapsed().as_millis()
        );
    });

    // Spawn Bird branch
    let bird = tokio::spawn(async {
        let bird_start = Instant::now();
        println!("  🐦 Bird: 🚀 started (child of World)");

        // Spawn Eagle (child of Bird)
        let eagle = tokio::spawn(async {
            let start = Instant::now();
            println!("    🦅 Eagle: 🚀 started (child of Bird)");
            sleep(Duration::from_millis(100)).await;
            println!(
                "    🦅 Eagle: ✅ finished in {} ms",
                start.elapsed().as_millis()
            );
        });

        // Spawn Sparrow (child of Bird)
        let sparrow = tokio::spawn(async {
            let sparrow_start = Instant::now();
            println!("    🐦 Sparrow: 🚀 started (child of Bird)");

            // Worm as a sub-task (child of Sparrow)
            let worm = tokio::spawn(async {
                let start = Instant::now();
                println!("      🪱 Worm: 🚀 started (child of Sparrow)");
                sleep(Duration::from_millis(300)).await;
                println!(
                    "      🪱 Worm: ✅ finished in {} ms",
                    start.elapsed().as_millis()
                );
            });

            let _ = worm.await;
            println!(
                "    🐦 Sparrow: ✅ finished in {} ms",
                sparrow_start.elapsed().as_millis()
            );
        });

        let _ = eagle.await;
        let _ = sparrow.await;
        println!(
            "  🐦 Bird: ✅ finished (all children done) in {} ms",
            bird_start.elapsed().as_millis()
        );
    });

    // Wait for Mammal and Bird to finish
    let _ = mammal.await;
    let _ = bird.await;

    println!(
        "🌍 World: ✅ finished in {} ms",
        world_start.elapsed().as_millis()
    );

    println!(
        "🍎 Apple never finished by Bear 🐻 in {} ms",
        world_start.elapsed().as_millis()
    );
}

#[cfg(not(feature = "tokio"))]
fn main() {
    panic!("tokio feature needed: cargo run --example tokio_hierarchy_panics --features tokio");
}
