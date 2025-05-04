object ThreadHierarchy {
  def main(args: Array[String]): Unit = {
    println("Main thread: starting")

    // Spawn thread_1
    val t1 = new Thread(() => {
      println("  thread_1: started (child of main)")
      Thread.sleep(100)
      println("  thread_1: finished")
    })

    // Spawn thread_2
    val t2 = new Thread(() => {
      println("  thread_2: started (child of main)")

      // Spawn thread_2_1
      val t2_1 = new Thread(() => {
        println("    thread_2_1: started (child of thread_2)")
        Thread.sleep(100)
        println("    thread_2_1: finished")
      })

      // Spawn thread_2_2
      val t2_2 = new Thread(() => {
        println("    thread_2_2: started (child of thread_2)")

        // Simulate tasks as threads for demonstration
        val task_a = new Thread(() => {
          println("      task_a: started (child of thread_2_2)")
          Thread.sleep(50)
          println("      task_a: finished")
        })
        val task_b = new Thread(() => {
          println("      task_b: started (child of thread_2_2)")
          Thread.sleep(50)
          println("      task_b: finished")
        })
        val task_c = new Thread(() => {
          println("      task_c: started (child of thread_2_2)")
          Thread.sleep(50)
          println("      task_c: finished")
        })

        // Start tasks
        task_a.start()
        task_b.start()
        task_c.start()

        // Wait for tasks to finish
        task_a.join()
        task_b.join()
        task_c.join()

        println("    thread_2_2: finished (all tasks done)")
      })

      // Start threads
      t2_1.start()
      t2_2.start()

      // Wait for threads to finish
      t2_1.join()
      t2_2.join()

      println("  thread_2: finished (all children done)")
    })

    // Start threads
    t1.start()
    t2.start()

    // Wait for threads to finish
    t1.join()
    t2.join()

    println("Main thread: finished")
  }
}
