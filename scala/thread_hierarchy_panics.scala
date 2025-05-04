/*
 * THREAD HIERARCHY AND EXCEPTION HANDLING IN SCALA
 * ===============================================
 *
 * This example demonstrates how exceptions affect thread hierarchies in Scala,
 * and how child threads behave when their parent thread encounters an exception.
 *
 * KEY CONCEPTS:
 * - Thread Hierarchies: Logical parent-child relationships between threads
 * - Exception Isolation: Exceptions in one thread don't automatically affect others
 * - Orphaned Threads: Child threads that continue after their parent terminates
 * - Thread Monitoring: Tracking and checking thread status
 *
 * SCENARIO:
 * - A world with mammals and birds (parallel branches)
 * - Animals within each branch (lion, tiger, bear, eagle, sparrow)
 * - The bear spawns fruit threads, then fails before they complete
 *
 * THREAD VS. ASYNC COMPARISON:
 * This example uses raw JVM threads, while the async_hierarchy_panics.scala example
 * uses Scala's Future-based concurrency. Key differences:
 *
 * 1. RESOURCE USAGE:
 *    - Threads: Each thread consumes OS resources (~1MB stack space per thread)
 *    - Futures: Many futures can share the same OS thread (more efficient)
 *
 * 2. CREATION OVERHEAD:
 *    - Threads: Expensive to create and destroy
 *    - Futures: Lightweight task objects with minimal creation cost
 *
 * 3. CONCURRENCY MODEL:
 *    - Threads: True parallel execution (if multiple CPU cores)
 *    - Futures: Cooperative multitasking with potential parallelism
 *
 * 4. EXECUTION START:
 *    - Threads: Must explicitly call .start() to begin execution
 *    - Futures: Begin execution immediately upon creation (eager evaluation)
 *
 * 5. EXCEPTION BEHAVIOR:
 *    - Both models: Child tasks continue after parent exceptions
 *    - Both require explicit handling to properly manage task hierarchies
 */

 import java.time.{Duration, Instant}
 import java.util.concurrent.CopyOnWriteArrayList
 import java.util.concurrent.atomic.AtomicReference

 object ThreadHierarchyPanics {
   def main(args: Array[String]): Unit = {
     val worldStart = Instant.now()
     println("🌍 World: 🚀 starting")

     // Thread-safe collection to track fruit threads
     val fruits = new CopyOnWriteArrayList[Thread]()

     // Reference to store the bear's exception
     val bearException = new AtomicReference[Throwable](null)

     // Mammal branch
     val mammalRunnable = new Runnable {
       override def run(): Unit = {
         val mammalStart = Instant.now()
         println("  🐾 Mammal: 🚀 started (child of World)")

         // Lion thread
         val lionRunnable = new Runnable {
           override def run(): Unit = {
             val start = Instant.now()
             println("    🦁 Lion: 🚀 started (child of Mammal)")
             Thread.sleep(100)
             println(s"    🦁 Lion: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
           }
         }
         val lion = new Thread(lionRunnable)

         // Tiger thread
         val tigerRunnable = new Runnable {
           override def run(): Unit = {
             val start = Instant.now()
             println("    🐯 Tiger: 🚀 started (child of Mammal)")
             Thread.sleep(100)
             println(s"    🐯 Tiger: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
           }
         }
         val tiger = new Thread(tigerRunnable)

         // Bear thread with panic
         val bearRunnable = new Runnable {
           override def run(): Unit = {
             val bearStart = Instant.now()
             println("    🐻 Bear: 🚀 started (child of Mammal)")

             try {
               // Apple thread
               val appleRunnable = new Runnable {
                 override def run(): Unit = {
                   val start = Instant.now()
                   println("      🍎 Apple: 🚀 started (child of Bear)")
                   Thread.sleep(550)
                   println(s"      🍎 Apple: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
                 }
               }
               val apple = new Thread(appleRunnable)

               // Banana thread
               val bananaRunnable = new Runnable {
                 override def run(): Unit = {
                   val start = Instant.now()
                   println("      🍌 Banana: 🚀 started (child of Bear)")
                   Thread.sleep(150)
                   println(s"      🍌 Banana: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
                 }
               }
               val banana = new Thread(bananaRunnable)

               // Cherry thread
               val cherryRunnable = new Runnable {
                 override def run(): Unit = {
                   val start = Instant.now()
                   println("      🍒 Cherry: 🚀 started (child of Bear)")
                   Thread.sleep(50)
                   println(s"      🍒 Cherry: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
                 }
               }
               val cherry = new Thread(cherryRunnable)

               // Store handles and start threads
               fruits.add(apple)
               fruits.add(banana)
               fruits.add(cherry)
               apple.start()
               banana.start()
               cherry.start()

               Thread.sleep(100)
               println(s"    🐻 Bear: 💥 panicking now! Fruits finished: 🍎${!apple.isAlive}, 🍌${!banana.isAlive}, 🍒${!cherry.isAlive}")

               // Throw an exception with a specific message
               throw new RuntimeException("Bear panicked: bear hates apples!")
             } catch {
               case e: Throwable =>
                 // Store the exception to be accessed by the parent thread
                 bearException.set(e)
                 throw e // Re-throw to terminate this thread
             }
           }
         }
         val bear = new Thread(bearRunnable)

         // Start and join all animal threads
         lion.start()
         tiger.start()
         bear.start()

         lion.join()
         tiger.join()
         bear.join() // Wait for bear to finish

         // Check if bear threw an exception and report it
         val exception = bearException.get()
         if (exception != null) {
           println(s"    🐻 Bear: 💥 panicked and was joined - ${exception.getMessage()}")
         } else {
           println("    🐻 Bear: ✅ finished normally")
         }

         println(s"  🐾 Mammal: ✅ finished (all children done) in ${Duration.between(mammalStart, Instant.now()).toMillis} ms")
       }
     }
     val mammal = new Thread(mammalRunnable)

     // Bird branch implementation stays the same
     val birdRunnable = new Runnable {
       override def run(): Unit = {
         // ... [keep existing implementation] ...
         val birdStart = Instant.now()
         println("  🐦 Bird: 🚀 started (child of World)")

         // Eagle thread
         val eagleRunnable = new Runnable {
           override def run(): Unit = {
             val start = Instant.now()
             println("    🦅 Eagle: 🚀 started (child of Bird)")
             Thread.sleep(100)
             println(s"    🦅 Eagle: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
           }
         }
         val eagle = new Thread(eagleRunnable)

         // Sparrow thread
         val sparrowRunnable = new Runnable {
           override def run(): Unit = {
             val sparrowStart = Instant.now()
             println("    🐦 Sparrow: 🚀 started (child of Bird)")

             // Worm thread
             val wormRunnable = new Runnable {
               override def run(): Unit = {
                 val start = Instant.now()
                 println("      🪱 Worm: 🚀 started (child of Sparrow)")
                 Thread.sleep(300)
                 println(s"      🪱 Worm: ✅ finished in ${Duration.between(start, Instant.now()).toMillis} ms")
               }
             }
             val worm = new Thread(wormRunnable)

             worm.start()
             worm.join()
             println(s"    🐦 Sparrow: ✅ finished in ${Duration.between(sparrowStart, Instant.now()).toMillis} ms")
           }
         }
         val sparrow = new Thread(sparrowRunnable)

         eagle.start()
         sparrow.start()
         eagle.join()
         sparrow.join()
         println(s"  🐦 Bird: ✅ finished (all children done) in ${Duration.between(birdStart, Instant.now()).toMillis} ms")
       }
     }
     val bird = new Thread(birdRunnable)

     // Start main threads and wait for completion
     mammal.start()
     bird.start()
     mammal.join()
     bird.join()

     // Check fruit status
     val fruitNames = Array("🍎 Apple", "🍌 Banana", "🍒 Cherry")
     for (i <- 0 until fruits.size()) {
       val fruit = fruits.get(i)
       if (fruit.isAlive) {
         println(s"      ${fruitNames(i)} never finished by Bear 🐻 in ${Duration.between(worldStart, Instant.now()).toMillis} ms")
       }
     }

     println(s"🌍 World: ✅ finished in ${Duration.between(worldStart, Instant.now()).toMillis} ms")
   }

  /*
   * KEY LEARNING POINTS:
   *
   * 1. THREAD ISOLATION:
   *    - Each thread has its own call stack and exception context
   *    - Exceptions in one thread don't automatically affect others
   *    - Parent threads must explicitly join() and handle exceptions from children
   *
   * 2. ORPHANED THREADS:
   *    - Child threads continue running even if their parent thread fails
   *    - No automatic supervision or cancellation in Java/Scala threads
   *    - The JVM won't exit until all non-daemon threads complete
   *
   * 3. JOINING BEHAVIOR:
   *    - join() waits for a thread to complete
   *    - If the thread terminated with an exception, join() re-throws it
   *    - Without join(), exceptions in other threads go unnoticed
   *
   * 4. COMPARED TO FUTURES:
   *    - Both: Child tasks can be orphaned by parent exceptions
   *    - Threads: More resource-intensive than Futures
   *    - Threads: Require explicit start() (vs. eager execution in Futures)
   *    - Threads: Lower-level, more direct control over execution
   *    - Futures: Better suited for asynchronous, non-blocking operations
   */

 }
