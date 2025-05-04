/*
 * DEADLOCK DEMONSTRATION IN SCALA
 * ==============================
 *
 * This example demonstrates a classic deadlock scenario where two threads
 * each hold a resource the other needs to proceed, creating a circular
 * dependency that causes both threads to wait indefinitely.
 *
 * KEY CONCEPTS:
 * - Deadlock: A situation where two or more threads are blocked forever,
 *   waiting for each other.
 * - Resource Contention: Multiple threads competing for exclusive access to resources.
 * - Circular Wait: Each thread holds a resource while waiting for another.
 *
 * SCENARIO:
 * - The Lion holds the Meat and wants the Cheese
 * - The Fox holds the Cheese and wants the Meat
 * - Neither can proceed, creating an unresolvable deadlock
 */

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import java.util.concurrent.locks.ReentrantLock

object ThreadDeadlock {
  def main(args: Array[String]): Unit = {
    // The two resources our animals will compete for
    val meat = new ReentrantLock() // 🍖
    val cheese = new ReentrantLock() // 🧀

    // Lion thread - grabs meat first, then tries to get cheese
    val lionRunnable = new Runnable {
      override def run(): Unit = {
        meat.lock()
        println("🦁 Lion grabs the 🍖 Meat!")
        Thread.sleep(100) // Simulate doing something with the meat
        println("🦁 Lion wants the 🧀 Cheese...")
        cheese.lock() // This will block if the Fox already has the cheese
        println("🦁 Lion got the 🧀 Cheese too!")
        cheese.unlock()
        meat.unlock()
      }
    }

    // Fox thread - grabs cheese first, then tries to get meat
    val foxRunnable = new Runnable {
      override def run(): Unit = {
        cheese.lock()
        println("🦊 Fox grabs the 🧀 Cheese!")
        Thread.sleep(100) // Simulate doing something with the cheese
        println("🦊 Fox wants the 🍖 Meat...")
        meat.lock() // This will block if the Lion already has the meat
        println("🦊 Fox got the 🍖 Meat too!")
        meat.unlock()
        cheese.unlock()
      }
    }

    /*
     * DEADLOCK CREATION STEP:
     * By starting both threads, we create the conditions for deadlock:
     * 1. Lion grabs meat, Fox grabs cheese
     * 2. Lion tries to grab cheese (held by Fox)
     * 3. Fox tries to grab meat (held by Lion)
     * 4. Both wait indefinitely - deadlock!
     */
    val lion = new Thread(lionRunnable)
    val fox = new Thread(foxRunnable)

    // Make them daemon threads so they don't prevent JVM shutdown
    lion.setDaemon(true)
    fox.setDaemon(true)

    // Start the threads
    lion.start()
    fox.start()

    // DEADLOCK DETECTION:
    // Using a Promise and timeout to detect when deadlock occurs
    val promise = Promise[Unit]()

    // Monitor thread to detect completion (which won't happen in deadlock)
    val monitorRunnable = new Runnable {
      override def run(): Unit = {
        try {
          // This will wait indefinitely if threads deadlock
          lion.join()
          fox.join()
          promise.success(()) // Will only reach here if no deadlock
        } catch {
          case e: Exception => promise.failure(e)
        }
      }
    }
    val monitor = new Thread(monitorRunnable)
    monitor.setDaemon(true)
    monitor.start()

    /*
     * DEADLOCK HANDLING:
     * Since deadlock occurs, the timeout will be triggered
     * In real systems, deadlock prevention is better than detection!
     */
    try {
      Await.result(promise.future, 5.seconds)
      println("All animals are happy! (No deadlock 🐾)")
    } catch {
      case _: java.util.concurrent.TimeoutException =>
        println("⏰ Timeout! The animals are deadlocked and still waiting for each other's food! 🦁🍖🧀🦊")
        // Interrupt threads (though this won't break locks)
        lion.interrupt()
        fox.interrupt()
        println("Program will exit now.")
    }

    /*
     * KEY LEARNING POINTS:
     *
     * 1. DEADLOCK CONDITIONS (all must be present):
     *    - Mutual Exclusion: Only one thread can use a resource at a time
     *    - Hold and Wait: Threads hold resources while waiting for others
     *    - No Preemption: Resources cannot be forcibly taken from threads
     *    - Circular Wait: Circular chain of threads waiting for each other
     *
     * 2. PREVENTION STRATEGIES:
     *    - Resource Ordering: Always acquire locks in the same order
     *    - Timeout-based Locks: Use tryLock() with timeout instead of lock()
     *    - Deadlock Detection: Use monitoring and recovery mechanisms
     *    - Lock Hierarchy: Design a clear hierarchy for lock acquisition
     *
     * 3. SCALA-SPECIFIC NOTES:
     *    - Use higher-level concurrency primitives when possible
     *    - Consider java.util.concurrent tools for complex scenarios
     *    - Prefer immutable data and message-passing concurrency (Akka)
     *    - Use Scala's Future and Promise for async operations
     */
  }
}
