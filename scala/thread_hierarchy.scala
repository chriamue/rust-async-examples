/*
 * THREAD HIERARCHY AND COORDINATION IN SCALA
 * =========================================
 *
 * This example demonstrates how to create and manage a hierarchy of threads in Scala,
 * showing parent-child relationships and how to coordinate thread execution using join().
 *
 * KEY CONCEPTS:
 * - Thread Creation: Creating threads for concurrent execution
 * - Thread Hierarchy: Logical parent-child relationships between threads
 * - Thread Coordination: Ensuring parent threads wait for their children
 * - Join Pattern: Using join() to wait for thread completion
 *
 * SCENARIO:
 * We create a multi-level thread hierarchy to visualize relationships:
 * - Main thread (root)
 *   ├── thread_1 (simple child thread)
 *   └── thread_2 (parent thread with children)
 *       ├── thread_2_1 (simple grandchild thread)
 *       └── thread_2_2 (grandchild thread with sub-tasks)
 *           ├── task_a (great-grandchild thread)
 *           ├── task_b (great-grandchild thread)
 *           └── task_c (great-grandchild thread)
 */

object ThreadHierarchy {
  def main(args: Array[String]): Unit = {
    println("Main thread: starting")

    /*
     * FIRST LEVEL THREAD:
     * A simple thread that performs its work and exits
     */
    val t1 = new Thread(() => {
      println("  thread_1: started (child of main)")
      Thread.sleep(100) // Simulate some work
      println("  thread_1: finished")
    })

    /*
     * FIRST LEVEL THREAD WITH CHILDREN:
     * This thread creates and manages its own child threads
     */
    val t2 = new Thread(() => {
      println("  thread_2: started (child of main)")

      /*
       * SECOND LEVEL THREAD:
       * Simple grandchild thread that performs work and exits
       */
      val t2_1 = new Thread(() => {
        println("    thread_2_1: started (child of thread_2)")
        Thread.sleep(100) // Simulate some work
        println("    thread_2_1: finished")
      })

      /*
       * SECOND LEVEL THREAD WITH SUB-TASKS:
       * This grandchild thread creates and manages its own child threads (tasks)
       */
      val t2_2 = new Thread(() => {
        println("    thread_2_2: started (child of thread_2)")

        // Create several sub-tasks as threads
        val task_a = new Thread(() => {
          println("      task_a: started (child of thread_2_2)")
          Thread.sleep(50) // Simulate work
          println("      task_a: finished")
        })
        val task_b = new Thread(() => {
          println("      task_b: started (child of thread_2_2)")
          Thread.sleep(50) // Simulate work
          println("      task_b: finished")
        })
        val task_c = new Thread(() => {
          println("      task_c: started (child of thread_2_2)")
          Thread.sleep(50) // Simulate work
          println("      task_c: finished")
        })

        /*
         * THREAD COORDINATION:
         * Start all tasks, then wait for them to complete before proceeding
         */
        // Start all child tasks
        task_a.start()
        task_b.start()
        task_c.start()

        // Wait for all tasks to finish before thread_2_2 can finish
        task_a.join() // Block until task_a completes
        task_b.join() // Block until task_b completes
        task_c.join() // Block until task_c completes

        println("    thread_2_2: finished (all tasks done)")
      })

      /*
       * THREAD COORDINATION:
       * Start both child threads, then wait for them to complete
       */
      // Start the second-level threads
      t2_1.start()
      t2_2.start()

      // Wait for second-level threads to finish before thread_2 can finish
      t2_1.join() // Block until t2_1 completes
      t2_2.join() // Block until t2_2 completes

      println("  thread_2: finished (all children done)")
    })

    /*
     * ROOT-LEVEL THREAD COORDINATION:
     * Start both main child threads, then wait for their completion
     */
    // Start the first-level threads
    t1.start()
    t2.start()

    // Wait for first-level threads to finish before main thread can exit
    t1.join() // Block until t1 completes
    t2.join() // Block until t2 completes

    println("Main thread: finished")
  }

  /*
   * KEY LEARNING POINTS:
   *
   * 1. THREAD HIERARCHY MANAGEMENT:
   *    - Java/Scala threads don't have built-in parent-child relationships
   *    - We create logical hierarchies through code structure and join() calls
   *    - Each level is responsible for starting and joining its child threads
   *
   * 2. COORDINATION WITH JOIN():
   *    - join() blocks the current thread until the target thread completes
   *    - This ensures proper sequencing and prevents premature termination
   *    - Without join(), parent threads might finish before their children
   *
   * 3. STRUCTURED CONCURRENCY:
   *    - The pattern shown implements "structured concurrency" principles
   *    - Parent threads have clear ownership of their child threads
   *    - Resources and lifetimes are properly managed through nesting
   *
   * 4. PRACTICAL APPLICATIONS:
   *    - Task decomposition: Breaking complex tasks into subtasks
   *    - Resource management: Ensuring all threads finish before releasing resources
   *    - Fan-out/fan-in patterns: Distributing work and collecting results
   *    - Graceful shutdown: Ensuring all child threads complete before parent exits
   */
}
