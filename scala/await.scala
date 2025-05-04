/*
 * EAGER VS LAZY ASYNC EXECUTION IN SCALA
 * =====================================
 *
 * This example contrasts Scala's eager Future execution model with
 * a lazy execution approach more similar to Rust's async/await.
 *
 * KEY CONCEPTS:
 * - Eager Execution: Tasks start immediately when created
 * - Lazy Execution: Tasks start only when explicitly triggered
 * - Awaiting Results: Blocking until asynchronous work completes
 * - Function Composition: Using functions to delay execution
 *
 * COMPARISON WITH RUST:
 * - Scala Futures are eager (start immediately when created)
 * - Rust futures are lazy (only start when awaited)
 * - This fundamental difference affects how you structure async code
 */

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

object AwaitDemo {
  def main(args: Array[String]): Unit = {
    /*
     * PART 1: EAGER FUTURES (SCALA DEFAULT)
     *
     * In Scala, a Future starts running as soon as it is created.
     * This is fundamentally different from Rust's async model.
     */

    // Define animal functions that return immediately-executing Futures
    def lionRuns(): Future[Unit] = Future {
      println("🦁 Lion is running!")
    }

    def foxRuns(): Future[Unit] = Future {
      println("🦊 Fox is running!")
    }

    def rabbitRuns(): Future[Unit] = Future {
      println("🐇 Rabbit is running!")
    }

    // Creating the Future starts execution immediately!
    println("🦁 lionRuns() is called.")
    val lionFuture = lionRuns() // Execution begins here!
    println("🦊 foxRuns() is called.")
    val foxFuture = foxRuns()   // Execution begins here!
    println("🐇 rabbitRuns() is called.")
    val rabbitFuture = rabbitRuns() // Execution begins here!
    println("(All animals are running already in Scala Future!)")

    // We only await some of the futures
    Await.result(foxFuture, 2.seconds)
    Await.result(lionFuture, 2.seconds)

    /*
     * KEY POINT:
     * We never await the rabbit's future, but in Scala,
     * the rabbit still runs because Futures start eagerly!
     *
     * This differs from Rust, where not awaiting means no execution.
     */
    println("All animals have finished running (even the rabbit 🐇, because Scala Future is eager)!")

    /*
     * PART 2: SIMULATING LAZY ASYNC (LIKE RUST)
     *
     * To mimic Rust's lazy async model in Scala, we can:
     * 1. Create functions that RETURN Futures rather than creating Futures directly
     * 2. Only call these functions when we want to start execution
     */

    // Define functions that RETURN functions that create Futures
    def lazyLionRuns(): () => Future[Unit] = () => Future { println("🦁 Lion is running!") }
    def lazyFoxRuns(): () => Future[Unit] = () => Future { println("🦊 Fox is running!") }
    def lazyRabbitRuns(): () => Future[Unit] = () => Future { println("🐇 Rabbit is running!") }

    println("\n--- Lazy version ---")
    println("🦁 lazyLionRuns() is called.")
    val lazyLion = lazyLionRuns() // Just returns a function - nothing runs yet!
    println("🦊 lazyFoxRuns() is called.")
    val lazyFox = lazyFoxRuns()   // Just returns a function - nothing runs yet!
    println("🐇 lazyRabbitRuns() is called.")
    val lazyRabbit = lazyRabbitRuns() // Just returns a function - nothing runs yet!
    println("(No animals are running yet...)")

    /*
     * Only when we call the returned function does execution begin.
     * This is similar to how Rust's .await works.
     */

    // Now we start the fox (similar to .await in Rust)
    val foxFut = lazyFox()  // NOW the fox starts running
    Await.result(foxFut, 2.seconds)

    // Now we start the lion (similar to .await in Rust)
    val lionFut = lazyLion() // NOW the lion starts running
    Await.result(lionFut, 2.seconds)

    /*
     * KEY POINT:
     * We never call lazyRabbit(), so the rabbit never runs.
     * This is similar to Rust's behavior when you don't .await a future.
     */
    println("All animals have finished running (except the rabbit 🐇, who never started)!")
  }

  /*
   * KEY LEARNING POINTS:
   *
   * 1. EAGER VS LAZY EXECUTION:
   *    - Scala Futures: Eager (start executing immediately when created)
   *    - Rust Futures: Lazy (start executing only when awaited)
   *    - Simulated lazy execution in Scala requires wrapping in functions
   *
   * 2. PRACTICAL IMPLICATIONS:
   *    - Eager: Ensures work begins ASAP, but can waste resources on unneeded tasks
   *    - Lazy: Gives more control over when work starts, but requires explicit triggering
   *    - Eager model makes error handling across futures more complex
   *
   * 3. DESIGN PATTERNS:
   *    - With eager futures, consider carefully when you create them
   *    - For maximum control, wrap Futures in functions (lazy approach)
   *    - Always be consistent about your approach to avoid confusion
   *
   * 4. WAITING FOR RESULTS:
   *    - Await.result in Scala is similar to .await in Rust
   *    - Both block execution of the current async context
   *    - Scala provides alternatives like callbacks and combinators (map/flatMap)
   */
}
