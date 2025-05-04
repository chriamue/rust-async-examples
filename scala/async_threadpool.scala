/*
 * THREAD POOL SIZE EFFECTS IN SCALA
 * =================================
 *
 * This example demonstrates how the size of a thread pool affects the execution
 * of CPU-bound tasks in Scala's Future-based concurrency model.
 *
 * KEY CONCEPTS:
 * - Thread Pool: A collection of worker threads that execute tasks concurrently
 * - CPU-bound Tasks: Tasks that primarily use CPU resources (vs I/O-bound tasks)
 * - ExecutionContext: Scala's abstraction for running asynchronous computations
 * - Parallelism vs Concurrency: Thread pool size limits true parallelism
 *
 * EXPERIMENT:
 * Run this program with different thread pool sizes to observe the effect:
 * - With 1 thread: SCALA_WORKER_THREADS=1 scala async_threadpool.scala
 * - With 3 threads: SCALA_WORKER_THREADS=3 scala async_threadpool.scala
 */

import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.Executors
import java.time.Instant

object AsyncThreadPool {
  def main(args: Array[String]): Unit = {
    // Get worker thread count from environment variable or default to 1
    val workerThreads: Int = sys.env.getOrElse("SCALA_WORKER_THREADS", "1").toInt

    /*
     * THREAD POOL CREATION:
     * Create a fixed-size pool with a controlled number of threads.
     * This limits how many CPU-bound tasks can execute in parallel.
     */
    val executorService = Executors.newFixedThreadPool(workerThreads)
    val executionContext = ExecutionContext.fromExecutorService(executorService)

    try {
      // Run the main async function with our custom ExecutionContext
      Await.result(asyncMain(workerThreads)(executionContext), 10.seconds)
    } finally {
      // Always shut down thread pools when done
      executorService.shutdown()
    }
  }

  def asyncMain(workerThreads: Int)(implicit ec: ExecutionContext): Future[Unit] = {
    val start = Instant.now()

    println(s"Starting 3 animals with a thread pool of $workerThreads worker threads...")

    /*
     * FUTURES WITH CPU-BOUND TASKS:
     * Each Future immediately starts executing its CPU-bound task.
     * - With 1 thread: Tasks run sequentially (one after another)
     * - With 3+ threads: Tasks run in parallel (all at once)
     *
     * Note that with Scala's eager Futures, execution begins immediately
     * when the Future is created, not when it's awaited.
     */
    val lion = Future {
      println("🦁 Lion starts running!")
      cpuBoundTask("🦁 Lion")
      println("🦁 Lion finished running!")
    }

    val fox = Future {
      println("🦊 Fox starts running!")
      cpuBoundTask("🦊 Fox")
      println("🦊 Fox finished running!")
    }

    val rabbit = Future {
      println("🐇 Rabbit starts running!")
      cpuBoundTask("🐇 Rabbit")
      println("🐇 Rabbit finished running!")
    }

    // Wait for all animals to finish and report timing
    for {
      _ <- lion
      _ <- fox
      _ <- rabbit
    } yield {
      val duration = java.time.Duration.between(start, Instant.now()).toMillis / 1000.0
      println(s"All animals finished! Total time: $duration seconds")
    }
  }

  /*
   * A CPU-BOUND TASK:
   * This function simulates a CPU-intensive operation by sleeping for 1 second.
   * In real applications, this would be computation, not sleeping.
   *
   * The key point: While this thread is busy, it cannot work on other tasks.
   */
  def cpuBoundTask(name: String): Unit = {
    Thread.sleep(1000) // Simulate CPU-bound work
    println(s"$name is running!")
  }

  /*
   * KEY LEARNING POINTS:
   *
   * 1. THREAD POOL SIZE MATTERS FOR CPU-BOUND WORK:
   *    - With 1 thread: ~3 seconds total (sequential execution)
   *    - With 3 threads: ~1 second total (parallel execution)
   *
   * 2. SCALA FUTURES ARE EAGER:
   *    - They begin execution as soon as they're created
   *    - This differs from lazy async models in some other languages
   *
   * 3. TASK CHARACTERIZATION IS IMPORTANT:
   *    - CPU-bound tasks benefit from more threads (up to CPU core count)
   *    - I/O-bound tasks can efficiently share fewer threads
   *    - Mixed workloads often need separate thread pools
   *
   * 4. SCALA SPECIFICS:
   *    - ExecutionContext is Scala's thread pool abstraction
   *    - The global ExecutionContext uses ForkJoinPool
   *    - Custom thread pools give more control over resource usage
   *    - Always shutdown() custom thread pools when done
   */
}
