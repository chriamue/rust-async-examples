use std::thread;
use std::time::Duration;

fn main() {
    println!("Main thread: starting");

    // Spawn thread_1
    let t1 = thread::spawn(|| {
        println!("  thread_1: started (child of main)");
        thread::sleep(Duration::from_millis(100));
        println!("  thread_1: finished");
    });

    // Spawn thread_2
    let t2 = thread::spawn(|| {
        println!("  thread_2: started (child of main)");

        // Spawn thread_2_1
        let t2_1 = thread::spawn(|| {
            println!("    thread_2_1: started (child of thread_2)");
            thread::sleep(Duration::from_millis(100));
            println!("    thread_2_1: finished");
        });

        // Spawn thread_2_2
        let t2_2 = thread::spawn(|| {
            println!("    thread_2_2: started (child of thread_2)");

            // Simulate tasks as threads for demonstration
            let task_a = thread::spawn(|| {
                println!("      task_a: started (child of thread_2_2)");
                thread::sleep(Duration::from_millis(50));
                println!("      task_a: finished");
            });
            let task_b = thread::spawn(|| {
                println!("      task_b: started (child of thread_2_2)");
                thread::sleep(Duration::from_millis(50));
                println!("      task_b: finished");
            });
            let task_c = thread::spawn(|| {
                println!("      task_c: started (child of thread_2_2)");
                thread::sleep(Duration::from_millis(50));
                println!("      task_c: finished");
            });

            // Wait for tasks to finish
            task_a.join().unwrap();
            task_b.join().unwrap();
            task_c.join().unwrap();

            println!("    thread_2_2: finished (all tasks done)");
        });

        // Wait for thread_2_1 and thread_2_2 to finish
        t2_1.join().unwrap();
        t2_2.join().unwrap();

        println!("  thread_2: finished (all children done)");
    });

    // Wait for thread_1 and thread_2 to finish
    t1.join().unwrap();
    t2.join().unwrap();

    println!("Main thread: finished");
}
