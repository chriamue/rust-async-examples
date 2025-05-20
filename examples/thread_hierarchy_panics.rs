use std::sync::{Arc, Mutex};
use std::thread;
use std::time::{Duration, Instant};

fn main() {
    let world_start = Instant::now();
    println!("🌍 World: 🚀 starting");

    // We'll store fruit JoinHandles here to check their status later
    let fruits: Arc<Mutex<Vec<thread::JoinHandle<()>>>> = Arc::new(Mutex::new(Vec::new()));

    // Spawn Mammal branch
    let fruits_clone = fruits.clone();
    let mammal = thread::Builder::new()
        .name("Mammal".to_string())
        .spawn(move || {
            let mammal_start = Instant::now();
            println!("  🐾 Mammal: 🚀 started (child of World)");

            // Spawn Lion (child of Mammal)
            let lion = thread::Builder::new()
                .name("Lion".to_string())
                .spawn(|| {
                    let start = Instant::now();
                    println!("    🦁 Lion: 🚀 started (child of Mammal)");
                    thread::sleep(Duration::from_millis(100));
                    println!(
                        "    🦁 Lion: ✅ finished in {} ms",
                        start.elapsed().as_millis()
                    );
                })
                .unwrap();

            // Spawn Tiger (child of Mammal)
            let tiger = thread::Builder::new()
                .name("Tiger".to_string())
                .spawn(|| {
                    let start = Instant::now();
                    println!("    🐯 Tiger: 🚀 started (child of Mammal)");
                    thread::sleep(Duration::from_millis(100));
                    println!(
                        "    🐯 Tiger: ✅ finished in {} ms",
                        start.elapsed().as_millis()
                    );
                })
                .unwrap();

            // Spawn Bear (child of Mammal)
            let fruits_inner = fruits_clone.clone();
            let bear = thread::Builder::new()
                .name("Bear".to_string())
                .spawn(move || {
                    let bear_start = Instant::now();
                    println!("    🐻 Bear: 🚀 started (child of Mammal)");

                    // Fruits as sub-tasks (children of Bear)
                    let apple = thread::Builder::new()
                        .name("Apple".to_string())
                        .spawn(|| {
                            let start = Instant::now();
                            println!("      🍎 Apple: 🚀 started (child of Bear)");
                            thread::sleep(Duration::from_millis(550));
                            println!(
                                "      🍎 Apple: ✅ finished in {} ms",
                                start.elapsed().as_millis()
                            );
                        })
                        .unwrap();
                    let banana = thread::Builder::new()
                        .name("Banana".to_string())
                        .spawn(|| {
                            let start = Instant::now();
                            println!("      🍌 Banana: 🚀 started (child of Bear)");
                            thread::sleep(Duration::from_millis(150));
                            println!(
                                "      🍌 Banana: ✅ finished in {} ms",
                                start.elapsed().as_millis()
                            );
                        })
                        .unwrap();
                    let cherry = thread::Builder::new()
                        .name("Cherry".to_string())
                        .spawn(|| {
                            let start = Instant::now();
                            println!("      🍒 Cherry: 🚀 started (child of Bear)");
                            thread::sleep(Duration::from_millis(50));
                            println!(
                                "      🍒 Cherry: ✅ finished in {} ms",
                                start.elapsed().as_millis()
                            );
                        })
                        .unwrap();

                    // Store handles for later status check
                    fruits_inner.lock().unwrap().push(apple);
                    fruits_inner.lock().unwrap().push(banana);
                    fruits_inner.lock().unwrap().push(cherry);

                    thread::sleep(Duration::from_millis(100));

                    let fruits = fruits_inner.lock().unwrap();
                    println!(
                        "    🐻 Bear: 💥 panicking now! Fruits finished: 🍎{:?}, 🍌{:?}, 🍒{:?}",
                        fruits[0].is_finished(),
                        fruits[1].is_finished(),
                        fruits[2].is_finished()
                    );
                    drop(fruits);
                    panic!(
                        "Bear panicked after {} ms",
                        bear_start.elapsed().as_millis()
                    );
                })
                .unwrap();

            // Wait for Lion, Tiger, and Bear to finish
            let _ = lion.join();
            let _ = tiger.join();
            let bear_result = bear.join();
            match bear_result {
                Ok(_) => println!("    🐻 Bear: ✅ finished normally"),
                Err(_) => println!("    🐻 Bear: 💥 panicked and was joined"),
            }

            println!(
                "  🐾 Mammal: ✅ finished (all children done) in {} ms",
                mammal_start.elapsed().as_millis()
            );
        })
        .unwrap();

    // Spawn Bird branch
    let bird = thread::Builder::new()
        .name("Bird".to_string())
        .spawn(|| {
            let bird_start = Instant::now();
            println!("  🐦 Bird: 🚀 started (child of World)");

            // Spawn Eagle (child of Bird)
            let eagle = thread::Builder::new()
                .name("Eagle".to_string())
                .spawn(|| {
                    let start = Instant::now();
                    println!("    🦅 Eagle: 🚀 started (child of Bird)");
                    thread::sleep(Duration::from_millis(100));
                    println!(
                        "    🦅 Eagle: ✅ finished in {} ms",
                        start.elapsed().as_millis()
                    );
                })
                .unwrap();

            // Spawn Sparrow (child of Bird)
            let sparrow = thread::Builder::new()
                .name("Sparrow".to_string())
                .spawn(|| {
                    let sparrow_start = Instant::now();
                    println!("    🐦 Sparrow: 🚀 started (child of Bird)");

                    // Worm as a sub-task (child of Sparrow)
                    let worm = thread::Builder::new()
                        .name("Worm".to_string())
                        .spawn(|| {
                            let start = Instant::now();
                            println!("      🪱 Worm: 🚀 started (child of Sparrow)");
                            thread::sleep(Duration::from_millis(300));
                            println!(
                                "      🪱 Worm: ✅ finished in {} ms",
                                start.elapsed().as_millis()
                            );
                        })
                        .unwrap();

                    let _ = worm.join();
                    println!(
                        "    🐦 Sparrow: ✅ finished in {} ms",
                        sparrow_start.elapsed().as_millis()
                    );
                })
                .unwrap();

            let _ = eagle.join();
            let _ = sparrow.join();
            println!(
                "  🐦 Bird: ✅ finished (all children done) in {} ms",
                bird_start.elapsed().as_millis()
            );
        })
        .unwrap();

    // Wait for Mammal and Bird to finish
    let _ = mammal.join();
    let _ = bird.join();

    // Check which fruits never finished
    let fruits = fruits.lock().unwrap();
    let fruit_names = ["🍎 Apple", "🍌 Banana", "🍒 Cherry"];
    for (i, fruit) in fruits.iter().enumerate() {
        if !fruit.is_finished() {
            println!(
                "      {} never finished by Bear 🐻 in {} ms",
                fruit_names[i],
                world_start.elapsed().as_millis()
            );
        }
    }

    println!(
        "🌍 World: ✅ finished in {} ms",
        world_start.elapsed().as_millis()
    );
}
