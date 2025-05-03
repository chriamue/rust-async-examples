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
                thread::sleep(Duration::from_millis(250));
                println!("      task_a: finished");
            });
            let task_b = thread::spawn(|| {
                println!("      task_b: started (child of thread_2_2)");
                thread::sleep(Duration::from_millis(150));
                println!("      task_b: finished");
            });
            let task_c = thread::spawn(|| {
                println!("      task_c: started (child of thread_2_2)");
                thread::sleep(Duration::from_millis(50));
                println!("      task_c: finished");
            });
            thread::sleep(Duration::from_millis(100));

            println!(
                "    thread_2_2: panicking now! Tasks finished: {:?},{:?},{:?}",
                task_a.is_finished(),
                task_b.is_finished(),
                task_c.is_finished()
            );
            panic!("thread_2_2 panicked");
            // If you join here, tasks will always finish:
            // let _ = task_a.join();
            // let _ = task_b.join();
            // let _ = task_c.join();
        });

        // Wait for thread_2_1 and thread_2_2 to finish
        let _ = t2_1.join();
        let t2_2_result = t2_2.join();
        thread::sleep(Duration::from_millis(250));
        match t2_2_result {
            Ok(_) => println!("    thread_2_2: finished normally"),
            Err(_) => println!("    thread_2_2: panicked and was joined"),
        }

        println!("  thread_2: finished (all children done)");
    });

    // Wait for thread_1 and thread_2 to finish
    let _ = t1.join();
    let _ = t2.join();

    println!("Main thread: finished");
}
