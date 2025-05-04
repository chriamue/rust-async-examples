import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._

object ThreadPoisoned {
  // Since Scala doesn't have a direct equivalent to Rust's poisoned mutex,
  // we'll simulate it with a ConcurrentHashMap to track poisoned state
  val food = new AtomicInteger(0)
  val poisonedState = new ConcurrentHashMap[String, Throwable]()

  def animalFindFood(animal: String, action: => Unit): Thread = {
    val thread = new Thread(() => {
      println(s"$animal tries to get the food...")

      if (poisonedState.isEmpty) {
        try {
          action
          println(s"$animal is done with the food.")
        } catch {
          case e: Throwable =>
            poisonedState.put("food", e)
            println(s"$animal dropped the food (panicked)!")
            throw e
        }
      } else {
        val e = poisonedState.values().asScala.head
        println(s"$animal found the food poisoned! Value: ${food.get()}")
      }
    })
    thread.start()
    thread
  }

  def main(args: Array[String]): Unit = {
    // 🦁 Lion takes a bite
    val lion = animalFindFood("🦁 Lion", {
      println("🦁 Lion takes a bite!")
      food.incrementAndGet()
      Thread.sleep(100)
    })

    // Wait a bit to ensure ordering
    Thread.sleep(50)

    // 🐻 Bear panics while holding the mutex
    val bear = animalFindFood("🐻 Bear", {
      println("🐻 Bear is about to panic!")
      throw new RuntimeException("🐻 Bear dropped the food (panicked)!")
    })

    // Wait a bit to ensure ordering
    Thread.sleep(50)

    // 🦊 Fox takes a bite (but will find the food poisoned)
    val fox = animalFindFood("🦊 Fox", {
      println("🦊 Fox takes a bite!")
      food.incrementAndGet()
      Thread.sleep(100)
    })

    // Wait for all threads to finish
    Thread.sleep(1000)
    println("Main thread: done.")
  }
}
