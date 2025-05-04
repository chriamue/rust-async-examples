import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import java.util.concurrent.locks.ReentrantLock

object ThreadDeadlock {
  def main(args: Array[String]): Unit = {
    val meat = new ReentrantLock() // 🍖
    val cheese = new ReentrantLock() // 🧀

    // Create a Runnable for the lion
    val lionRunnable = new Runnable {
      override def run(): Unit = {
        meat.lock()
        println("🦁 Lion grabs the 🍖 Meat!")
        Thread.sleep(100)
        println("🦁 Lion wants the 🧀 Cheese...")
        cheese.lock()
        println("🦁 Lion got the 🧀 Cheese too!")
        cheese.unlock()
        meat.unlock()
      }
    }

    // Create a Runnable for the fox
    val foxRunnable = new Runnable {
      override def run(): Unit = {
        cheese.lock()
        println("🦊 Fox grabs the 🧀 Cheese!")
        Thread.sleep(100)
        println("🦊 Fox wants the 🍖 Meat...")
        meat.lock()
        println("🦊 Fox got the 🍖 Meat too!")
        meat.unlock()
        cheese.unlock()
      }
    }

    // Create threads with the explicit Runnable implementations
    val lion = new Thread(lionRunnable)
    val fox = new Thread(foxRunnable)

    // Make them daemon threads so they don't prevent JVM shutdown
    lion.setDaemon(true)
    fox.setDaemon(true)

    // Start the threads
    lion.start()
    fox.start()

    // Create a promise to signal when both threads are done
    val promise = Promise[Unit]()

    // Monitor thread to detect completion
    val monitorRunnable = new Runnable {
      override def run(): Unit = {
        try {
          lion.join()
          fox.join()
          promise.success(())
        } catch {
          case e: Exception => promise.failure(e)
        }
      }
    }
    val monitor = new Thread(monitorRunnable)
    monitor.setDaemon(true)  // Also make the monitor a daemon thread
    monitor.start()

    // Wait for completion with timeout
    try {
      Await.result(promise.future, 5.seconds)
      println("All animals are happy! (No deadlock 🐾)")
    } catch {
      case _: java.util.concurrent.TimeoutException =>
        println("⏰ Timeout! The animals are deadlocked and still waiting for each other's food! 🦁🍖🧀🦊")
        // Optionally, try to interrupt the threads (though this won't break locks)
        lion.interrupt()
        fox.interrupt()
        println("Program will exit now.")
    }
  }
}
