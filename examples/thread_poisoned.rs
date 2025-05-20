use std::sync::{mpsc, Arc, Mutex};
use std::thread;
use std::time::Duration;

fn animal_find_food<F>(
    animal: &'static str,
    food: Arc<Mutex<i32>>,
    action: F,
) -> thread::JoinHandle<()>
where
    F: FnOnce(&mut i32) + Send + 'static,
{
    let thread_name = format!("{} thread", animal);
    thread::Builder::new()
        .name(thread_name.clone())
        .spawn(move || {
            println!("{} tries to get the food...", animal);
            match food.lock() {
                Ok(mut food) => {
                    action(&mut *food);
                    println!("{} is done with the food.", animal);
                }
                Err(poisoned) => {
                    println!(
                        "{} found the food poisoned! Value: {}",
                        animal,
                        *poisoned.get_ref()
                    );
                }
            }
        })
        .expect(&format!("Failed to spawn thread: {}", thread_name))
}

fn main() {
    let food = Arc::new(Mutex::new(0));

    // Channel to signal when Bear is about to panic
    let (tx, rx) = mpsc::channel();

    // 🦁 Lion takes a bite
    let lion_food = Arc::clone(&food);
    let lion = animal_find_food("🦁 Lion", lion_food, |food| {
        println!("🦁 Lion takes a bite!");
        *food += 1;
        thread::sleep(Duration::from_millis(100));
    });

    // 🐻 Bear panics while holding the mutex
    let bear_food = Arc::clone(&food);
    let tx_bear = tx.clone();
    let bear = animal_find_food("🐻 Bear", bear_food, move |food| {
        println!("🐻 Bear is about to panic!");
        // Notify Fox that Bear is about to panic
        *food += 1;
        tx_bear.send(()).unwrap();
        panic!("🐻 Bear dropped the food (panicked)!");
    });

    // 🦊 Fox waits for Bear's signal before trying to eat
    let fox_food = Arc::clone(&food);
    let fox = thread::Builder::new()
        .name("🦊 Fox thread".to_string())
        .spawn(move || {
            // Wait for Bear to panic
            rx.recv().unwrap();
            println!("🦊 Fox tries to get the food...");
            match fox_food.lock() {
                Ok(mut food) => {
                    println!("🦊 Fox takes a bite!");
                    *food += 1;
                    thread::sleep(Duration::from_millis(100));
                    println!("🦊 Fox is done with the food.");
                }
                Err(poisoned) => {
                    println!(
                        "🦊 Fox found the food poisoned! Value: {}",
                        *poisoned.get_ref()
                    );
                }
            }
        })
        .unwrap();

    // Wait for all threads to finish
    let _ = lion.join();
    let _ = bear.join();
    let _ = fox.join();

    println!("Main thread: done.");
}
