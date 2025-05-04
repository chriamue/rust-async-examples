import java.time.{Duration, Instant}
import java.util.concurrent.CopyOnWriteArrayList
import scala.collection.JavaConverters._

object ThreadHierarchyPanics {
  def main(args: Array[String]): Unit = {
    val worldStart = Instant.now()
    println("🌍 World: 🚀 starting")

    // We'll store fruit thread handles here to check their status later
    val fruits = new CopyOnWriteArrayList[Thread]()

    // Spawn Mammal branch
    val mammal = new Thread(() => {
      val mammalStart = Instant.now()
      println("  🐾 Mammal: 🚀 started (child of World)")

      // Spawn Lion (child of Mammal)
      val lion = new Thread(() => {
        val start = Instant.now()
        println("    🦁 Lion: 🚀 started (child of Mammal)")
        Thread.sleep(100)
        println(s"    🦁 Lion: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
      })

      // Spawn Tiger (child of Mammal)
      val tiger = new Thread(() => {
        val start = Instant.now()
        println("    🐯 Tiger: 🚀 started (child of Mammal)")
        Thread.sleep(100)
        println(s"    🐯 Tiger: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
      })

      // Spawn Bear (child of Mammal)
      val bear = new Thread(() => {
        val bearStart = Instant.now()
        println("    🐻 Bear: 🚀 started (child of Mammal)")

        // Fruits as sub-tasks (children of Bear)
        val apple = new Thread(() => {
          val start = Instant.now()
          println("      🍎 Apple: 🚀 started (child of Bear)")
          Thread.sleep(550)
          println(s"      🍎 Apple: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
        })
        val banana = new Thread(() => {
          val start = Instant.now()
          println("      🍌 Banana: 🚀 started (child of Bear)")
          Thread.sleep(150)
          println(s"      🍌 Banana: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
        })
        val cherry = new Thread(() => {
          val start = Instant.now()
          println("      🍒 Cherry: 🚀 started (child of Bear)")
          Thread.sleep(50)
          println(s"      🍒 Cherry: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
        })

        // Store handles for later status check
        fruits.add(apple)
        fruits.add(banana)
        fruits.add(cherry)

        // Start fruit threads
        apple.start()
        banana.start()
        cherry.start()

        Thread.sleep(100)

        println(s"    🐻 Bear: 💥 panicking now! Fruits finished: 🍎${!apple.isAlive}, 🍌${!banana.isAlive}, 🍒${!cherry.isAlive}")
        throw new RuntimeException(s"Bear panicked after ${Duration.between(bearStart, Instant.now()).toMillis} ms")
      })

      // Start mammal threads
      lion.start()
      tiger.start()
      bear.start()

      // Wait for threads to finish
      lion.join()
      tiger.join()
      try {
        bear.join()
        println("    🐻 Bear: ✅ finished normally")
      } catch {
        case e: Exception => println("    🐻 Bear: 💥 panicked and was joined")
      }

      println(s"  🐾 Mammal: ✅ finished (all children done) in ${Duration.between(mammalStart, Instant.now()).toMillis} ms")
    })

    // Spawn Bird branch
    val bird = new Thread(() => {
      val birdStart = Instant.now()
      println("  🐦 Bird: 🚀 started (child of World)")

      // Spawn Eagle (child of Bird)
      val eagle = new Thread(() => {
        val start = Instant.now()
        println("    🦅 Eagle: 🚀 started (child of Bird)")
        Thread.sleep(100)
        println(s"    🦅 Eagle: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
      })

      // Spawn Sparrow (child of Bird)
      val sparrow = new Thread(() => {
        val sparrowStart = Instant.now()
        println("    🐦 Sparrow: 🚀 started (child of Bird)")

        // Worm as a sub-task (child of Sparrow)
        val worm = new Thread(() => {
          val start = Instant.now()
          println("      🪱 Worm: 🚀 started (child of Sparrow)")
          Thread.sleep(300)
          println(s"      🪱 Worm: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
        })

        worm.start()
        worm.join()
        println(s"    🐦 Sparrow: ✅ finished in ${Duration.between(sparrowStart, Instant.now()).toMillis} ms")
      })

      eagle.start()
      sparrow.start()

      eagle.join()
      sparrow.join()
      println(s"  🐦 Bird: ✅ finished (all children done) in ${Duration.between(birdStart, Instant.now()).toMillis} ms")
    })

    // Start world threads
    mammal.start()
    bird.start()

    // Wait for threads to finish
    mammal.join()
    bird.join()

    // Check which fruits never finished
    val fruitNames = Array("🍎 Apple", "🍌 Banana", "🍒 Cherry")
    for (i <- 0 until fruits.size()) {
      val fruit = fruits.get(i)
      if (fruit.isAlive) {
        println(s"      ${fruitNames(i)} never finished by Bear 🐻 in ${Duration.between(worldStart, Instant.now()).toMillis} ms")
      }
    }

    println(s"🌍 World: ✅ finished in ${Duration.between(worldStart, Instant.now()).toMillis} ms")
  }
}
