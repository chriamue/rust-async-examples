use std::sync::{Arc, Mutex};
use std::thread;
use std::time::Duration;

fn animal_find_food<F>(animal: &'static str, food: Arc<Mutex<i32>>, action: F)
where
    F: FnOnce(&mut i32) + Send + 'static,
{
    thread::spawn(move || {
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
    });
}

fn main() {
    let food = Arc::new(Mutex::new(0));

    // 🦁 Lion takes a bite
    let lion_food = Arc::clone(&food);
    let _lion = animal_find_food("🦁 Lion", lion_food, |food| {
        println!("🦁 Lion takes a bite!");
        *food += 1;
        thread::sleep(Duration::from_millis(100));
    });

    // 🐻 Bear panics while holding the mutex
    let bear_food = Arc::clone(&food);
    let _bear = animal_find_food("🐻 Bear", bear_food, |_food| {
        println!("🐻 Bear is about to panic!");
        panic!("🐻 Bear dropped the food (panicked)!");
    });

    // 🦊 Fox takes a bite (but will find the food poisoned)
    let fox_food = Arc::clone(&food);
    let _fox = animal_find_food("🦊 Fox", fox_food, |food| {
        println!("🦊 Fox takes a bite!");
        *food += 1;
        thread::sleep(Duration::from_millis(100));
    });

    // Wait for all threads to finish
    // (We can't join the threads directly since animal_find_food returns nothing,
    // so let's sleep a bit to let them finish for this demo.)
    thread::sleep(Duration::from_secs(1));
    println!("Main thread: done.");
}
