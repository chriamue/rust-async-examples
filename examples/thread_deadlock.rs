use std::sync::{mpsc, Arc, Mutex};
use std::thread;
use std::time::Duration;

fn main() {
    let meat = Arc::new(Mutex::new(())); // 🍖
    let cheese = Arc::new(Mutex::new(())); // 🧀

    let meat_for_lion = Arc::clone(&meat);
    let cheese_for_lion = Arc::clone(&cheese);

    let lion = thread::spawn(move || {
        let _meat = meat_for_lion.lock().unwrap();
        println!("🦁 Lion grabs the 🍖 Meat!");
        thread::sleep(Duration::from_millis(100));
        println!("🦁 Lion wants the 🧀 Cheese...");
        let _cheese = cheese_for_lion.lock().unwrap();
        println!("🦁 Lion got the 🧀 Cheese too!");
    });

    let meat_for_fox = Arc::clone(&meat);
    let cheese_for_fox = Arc::clone(&cheese);

    let fox = thread::spawn(move || {
        let _cheese = cheese_for_fox.lock().unwrap();
        println!("🦊 Fox grabs the 🧀 Cheese!");
        thread::sleep(Duration::from_millis(100));
        println!("🦊 Fox wants the 🍖 Meat...");
        let _meat = meat_for_fox.lock().unwrap();
        println!("🦊 Fox got the 🍖 Meat too!");
    });

    // Channel to notify when both threads are done
    let (tx, rx) = mpsc::channel();

    thread::spawn(move || {
        let _ = lion.join();
        let _ = fox.join();
        let _ = tx.send(());
    });

    let timeout = Duration::from_secs(5);
    if rx.recv_timeout(timeout).is_ok() {
        println!("All animals are happy! (No deadlock 🐾)");
    } else {
        println!("⏰ Timeout! The animals are deadlocked and still waiting for each other's food! 🦁🍖🧀🦊");
    }
}
