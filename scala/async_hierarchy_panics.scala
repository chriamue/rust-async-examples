import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.time.{Duration, Instant}

object AsyncHierarchyPanics {
  def main(args: Array[String]): Unit = {
    val worldStart = Instant.now()
    println("🌍 World: 🚀 starting")

    // Spawn Mammal branch
    val mammal = Future {
      val mammalStart = Instant.now()
      println("  🐾 Mammal: 🚀 started (child of World)")

      // Spawn Lion (child of Mammal)
      val lion = Future {
        val start = Instant.now()
        println("    🦁 Lion: 🚀 started (child of Mammal)")
        Thread.sleep(100)
        println(s"    🦁 Lion: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
      }

      // Spawn Tiger (child of Mammal)
      val tiger = Future {
        val start = Instant.now()
        println("    🐯 Tiger: 🚀 started (child of Mammal)")
        Thread.sleep(100)
        println(s"    🐯 Tiger: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
      }

      // Spawn Bear (child of Mammal)
      val bear = Future {
        val bearStart = Instant.now()
        println("    🐻 Bear: 🚀 started (child of Mammal)")

        // Fruits as sub-tasks (children of Bear)
        val apple = Future {
          val start = Instant.now()
          println("      🍎 Apple: 🚀 started (child of Bear)")
          Thread.sleep(550)
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
          Thread.sleep(50)
          println(s"      🍒 Cherry: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
        }

        Thread.sleep(100)

        println(s"    🐻 Bear: 💥 panicking now! Fruits finished: " +
                s"🍎${apple.isCompleted}, 🍌${banana.isCompleted}, 🍒${cherry.isCompleted}")

        // Throw exception (equivalent to panic in Rust)
        throw new RuntimeException(s"Bear panicked after ${Duration.between(bearStart, Instant.now()).toMillis} ms")

        // If we await here, fruits will always finish:
        // Await.result(apple, 1.second)
        // Await.result(banana, 1.second)
        // Await.result(cherry, 1.second)
      }

      // Wait for Lion, Tiger, and Bear to finish
      Await.result(lion, 1.second)
      Await.result(tiger, 1.second)

      try {
        Await.result(bear, 1.second)
        println("    🐻 Bear: ✅ finished normally")
      } catch {
        case e: Exception => println("    🐻 Bear: 💥 panicked and was joined")
      }

      println(s"  🐾 Mammal: ✅ finished (all children done) in ${Duration.between(mammalStart, Instant.now()).toMillis} ms")
    }

    // Spawn Bird branch
    val bird = Future {
      val birdStart = Instant.now()
      println("  🐦 Bird: 🚀 started (child of World)")

      // Spawn Eagle (child of Bird)
      val eagle = Future {
        val start = Instant.now()
        println("    🦅 Eagle: 🚀 started (child of Bird)")
        Thread.sleep(100)
        println(s"    🦅 Eagle: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
      }

      // Spawn Sparrow (child of Bird)
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

        Await.result(worm, 1.second)
        println(s"    🐦 Sparrow: ✅ finished in ${Duration.between(sparrowStart, Instant.now()).toMillis} ms")
      }

      Await.result(eagle, 1.second)
      Await.result(sparrow, 1.second)
      println(s"  🐦 Bird: ✅ finished (all children done) in ${Duration.between(birdStart, Instant.now()).toMillis} ms")
    }

    // Wait for Mammal and Bird to finish
    Await.result(mammal, Duration.ofSeconds(5).toMillis.millis)
    Await.result(bird, Duration.ofSeconds(5).toMillis.millis)

    println(s"🍎 Apple never finished by Bear 🐻 in ${Duration.between(worldStart, Instant.now()).toMillis} ms")

    println(s"🌍 World: ✅ finished in ${Duration.between(worldStart, Instant.now()).toMillis} ms")
  }
}
