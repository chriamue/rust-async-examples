import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.Executors
import java.time.Instant

object AsyncThreadPool {
  def main(args: Array[String]): Unit = {
    // Get worker thread count from environment variable or default to 1
    val workerThreads: Int = sys.env.getOrElse("SCALA_WORKER_THREADS", "1").toInt

    // Create a fixed size thread pool
    val executorService = Executors.newFixedThreadPool(workerThreads)
    val executionContext = ExecutionContext.fromExecutorService(executorService)

    try {
      // Run the main async function
      Await.result(asyncMain(workerThreads)(executionContext), 10.seconds)
    } finally {
      executorService.shutdown()
    }
  }

  def asyncMain(workerThreads: Int)(implicit ec: ExecutionContext): Future[Unit] = {
    val start = Instant.now()

    println(s"Starting 3 animals with a thread pool of $workerThreads worker threads...")

    val lion = Future {
      println("🦁 Lion starts running!")
      // Note that in Scala, this will run because Future is eager
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

    // Wait for all animals to finish
    for {
      _ <- lion
      _ <- fox
      _ <- rabbit
    } yield {
      val duration = java.time.Duration.between(start, Instant.now()).toMillis / 1000.0
      println(s"All animals finished! Total time: $duration seconds")
    }
  }

  // This function will busy-loop for about 1 second
  def cpuBoundTask(name: String): Unit = {
    Thread.sleep(1000)
    println(s"$name is running!")
  }
}
