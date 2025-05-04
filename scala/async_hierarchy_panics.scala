/*
 * FUTURE HIERARCHY AND EXCEPTION PROPAGATION IN SCALA
 * ==================================================
 *
 * This example demonstrates how exceptions affect the execution of hierarchical
 * Future tasks in Scala, and how orphaned tasks behave when their "parent" fails.
 *
 * KEY CONCEPTS:
 * - Future Hierarchy: Creating nested structures of asynchronous tasks
 * - Exception Propagation: How failures travel up the call chain
 * - Orphaned Tasks: Tasks that continue running after their parent fails
 * - Task Independence: Futures run independently once started
 *
 * SCENARIO:
 * - A world with mammals and birds (parallel branches)
 * - Animals within each branch (lion, tiger, bear, eagle, sparrow)
 * - The bear spawns fruit tasks, then fails before they complete
 */

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.time.{Duration, Instant}

object AsyncHierarchyPanics {
  def main(args: Array[String]): Unit = {
    val worldStart = Instant.now()
    println("🌍 World: 🚀 starting")

    /*
     * MAMMAL BRANCH:
     * Demonstrates task hierarchy and exception handling between tasks
     */
    val mammal = Future {
      val mammalStart = Instant.now()
      println("  🐾 Mammal: 🚀 started (child of World)")

      // Spawn animal tasks (children of Mammal)
      val lion = Future {
        val start = Instant.now()
        println("    🦁 Lion: 🚀 started (child of Mammal)")
        Thread.sleep(100)
        println(s"    🦁 Lion: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
      }

      val tiger = Future {
        val start = Instant.now()
        println("    🐯 Tiger: 🚀 started (child of Mammal)")
        Thread.sleep(100)
        println(s"    🐯 Tiger: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
      }

      /*
       * BEAR WITH PANIC:
       * This task spawns child tasks, then fails before they complete.
       * Key point: The child tasks continue execution even after parent fails!
       */
      val bear = Future {
        val bearStart = Instant.now()
        println("    🐻 Bear: 🚀 started (child of Mammal)")

        // Spawn fruit tasks (children of Bear)
        val apple = Future {
          val start = Instant.now()
          println("      🍎 Apple: 🚀 started (child of Bear)")
          Thread.sleep(550) // Takes a long time - will be orphaned
          println(s"      🍎 Apple: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
        }

        val banana = Future {
          val start = Instant.now()
          println("      🍌 Banana: 🚀 started (child of Bear)")
          Thread.sleep(150)
          println(s"      🍌 Banana: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
        }

        val cherry = Future {
          val start = Instant.now()
          println("      🍒 Cherry: 🚀 started (child of Bear)")
          Thread.sleep(50) // Quick task - might complete before bear panics
          println(s"      🍒 Cherry: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
        }

        // Sleep for a bit, then check task status before panicking
        Thread.sleep(100)

        println(s"    🐻 Bear: 💥 panicking now! Fruits finished: " +
                s"🍎${apple.isCompleted}, 🍌${banana.isCompleted}, 🍒${cherry.isCompleted}")

        // EXCEPTION IN TASK with custom message:
        // This simulates a "panic" or uncaught exception
        throw new RuntimeException("Bear panicked: bear hates apples!")

        // IMPORTANT: If we awaited child tasks, they would complete before the parent:
        // Await.result(apple, 1.second)
        // Await.result(banana, 1.second)
        // Await.result(cherry, 1.second)
      }

      // Wait for tasks to finish
      Await.result(lion, 1.second)
      Await.result(tiger, 1.second)

      // Handle bear's exception
      try {
        Await.result(bear, 1.second)
        println("    🐻 Bear: ✅ finished normally")
      } catch {
        case e: Exception =>
          println(s"    🐻 Bear: 💥 panicked and was joined - ${e.getMessage}")
      }

      println(s"  🐾 Mammal: ✅ finished (all children done) in ${Duration.between(mammalStart, Instant.now()).toMillis} ms")
    }

    /*
     * BIRD BRANCH:
     * Demonstrates a properly managed task hierarchy where all tasks complete
     */
    val bird = Future {
      val birdStart = Instant.now()
      println("  🐦 Bird: 🚀 started (child of World)")

      // Spawn eagle task
      val eagle = Future {
        val start = Instant.now()
        println("    🦅 Eagle: 🚀 started (child of Bird)")
        Thread.sleep(100)
        println(s"    🦅 Eagle: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
      }

      // Spawn sparrow task with child
      val sparrow = Future {
        val sparrowStart = Instant.now()
        println("    🐦 Sparrow: 🚀 started (child of Bird)")

        // Worm as a sub-task (child of Sparrow)
        val worm = Future {
          val start = Instant.now()
          println("      🪱 Worm: 🚀 started (child of Sparrow)")
          Thread.sleep(300)
          println(s"      🪱 Worm: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
        }

        // Sparrow properly awaits its child task
        Await.result(worm, 1.second)
        println(s"    🐦 Sparrow: ✅ finished in ${Duration.between(sparrowStart, Instant.now()).toMillis} ms")
      }

      // Bird properly awaits all its child tasks
      Await.result(eagle, 1.second)
      Await.result(sparrow, 1.second)
      println(s"  🐦 Bird: ✅ finished (all children done) in ${Duration.between(birdStart, Instant.now()).toMillis} ms")
    }

    // World awaits both main branches
    Await.result(mammal, Duration.ofSeconds(5).toMillis.millis)
    Await.result(bird, Duration.ofSeconds(5).toMillis.millis)

    // The Apple task will likely not have completed because it was orphaned when Bear panicked
    println(s"🍎 Apple never finished by Bear 🐻 in ${Duration.between(worldStart, Instant.now()).toMillis} ms")

    println(s"🌍 World: ✅ finished in ${Duration.between(worldStart, Instant.now()).toMillis} ms")
  }

  /*
   * KEY LEARNING POINTS:
   *
   * 1. TASK SUPERVISION IN SCALA:
   *    - Unlike some other frameworks (e.g., Akka), Scala Futures don't have built-in supervision
   *    - Parent futures don't automatically cancel child futures when they fail
   *    - Child futures continue running even if parent futures fail
   *
   * 2. ORPHANED TASKS:
   *    - Tasks that keep running after their logical parent has completed/failed
   *    - This can lead to resource leaks or unexpected behavior
   *    - Proper cleanup requires explicit handling
   *
   * 3. EXCEPTION HANDLING IN ASYNC HIERARCHIES:
   *    - Exceptions only propagate up when awaited with Await.result or handled with callbacks
   *    - Unhandled exceptions in futures don't affect other concurrent futures
   *    - Exceptions don't automatically terminate child tasks
   *
   * 4. BEST PRACTICES:
   *    - Always handle or propagate exceptions in futures
   *    - Use structured concurrency patterns:
   *      * Parent task should await or cancel all child tasks
   *      * Higher-level abstractions like ZIO, Cats Effect provide better supervision
   *    - Consider using a cancellable task model for complex hierarchies
   */
}
