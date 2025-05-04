import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

object AwaitDemo {
  def main(args: Array[String]): Unit = {
    // # Eager Futures (Scala default)
    // In Scala, a Future starts running as soon as it is created.

    def lionRuns(): Future[Unit] = Future {
      println("🦁 Lion is running!")
    }

    def foxRuns(): Future[Unit] = Future {
      println("🦊 Fox is running!")
    }

    def rabbitRuns(): Future[Unit] = Future {
      println("🐇 Rabbit is running!")
    }

    println("🦁 lionRuns() is called.")
    val lionFuture = lionRuns() // Actually starts immediately in Scala!
    println("🦊 foxRuns() is called.")
    val foxFuture = foxRuns()
    println("🐇 rabbitRuns() is called.")
    val rabbitFuture = rabbitRuns()
    println("(All animals are running already in Scala Future!)")

    // Let's wait for the fox and lion to finish
    Await.result(foxFuture, 2.seconds)
    Await.result(lionFuture, 2.seconds)

    // We never await the rabbit!
    // But in Scala, the rabbit still runs because Future starts eagerly.

    println("All animals have finished running (even the rabbit 🐇, because Scala Future is eager)!")

    // # Lazy "async" with functions returning Future
    // To mimic Rust's lazy async, use a function that returns a Future, and only call it when you want to start the work.

    def lazyLionRuns(): () => Future[Unit] = () => Future { println("🦁 Lion is running!") }
    def lazyFoxRuns(): () => Future[Unit] = () => Future { println("🦊 Fox is running!") }
    def lazyRabbitRuns(): () => Future[Unit] = () => Future { println("🐇 Rabbit is running!") }

    println("\n--- Lazy version ---")
    println("🦁 lazyLionRuns() is called.")
    val lazyLion = lazyLionRuns() // Not started yet!
    println("🦊 lazyFoxRuns() is called.")
    val lazyFox = lazyFoxRuns()
    println("🐇 lazyRabbitRuns() is called.")
    val lazyRabbit = lazyRabbitRuns()
    println("(No animals are running yet...)")

    // Now we start the fox
    val foxFut = lazyFox()
    Await.result(foxFut, 2.seconds)

    // Now we start the lion
    val lionFut = lazyLion()
    Await.result(lionFut, 2.seconds)

    // We never start the rabbit!
    println("All animals have finished running (except the rabbit 🐇, who never started)!")
  }
}
