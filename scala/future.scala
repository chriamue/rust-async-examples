/*
 * FUTURE BEST PRACTICES IN SCALA
 * =============================
 *
 * This example demonstrates recommended patterns for working with Scala's Futures,
 * focusing on proper error handling, composition, and resource management.
 *
 * KEY CONCEPTS:
 * - Future[Either[Error, Success]]: Combining futures with Either for clean error handling
 * - Future composition: Using for-yield for readable async sequences
 * - Error recovery: Handling exceptions and failures gracefully
 * - Non-blocking delays: Using proper techniques for delays without blocking threads
 * - Resource management: Proper cleanup with transformations
 */

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
import java.time.Instant
import java.util.{Timer, TimerTask}

// Domain models - animal kingdom data we want to process
case class Animal(name: String, species: String)
case class Diet(foodType: String, dailyAmount: Int)
case class Habitat(location: String, climate: String)
case class AnimalProfile(animal: Animal, diet: Diet, habitat: Habitat)

// Error model - a sealed trait hierarchy for type-safe error handling
sealed trait ZooError {
  def message: String
}
case class DatabaseError(message: String) extends ZooError
case class NetworkError(message: String) extends ZooError
case class ValidationError(message: String) extends ZooError
case class TimeoutError(message: String) extends ZooError

object FutureBestPractices {

  /**
   * Helper object for non-blocking delays in Futures
   * This is better than Thread.sleep() as it doesn't block a thread
   */
  object DelayedFuture {
    def by[T](duration: Duration)(block: => T): Future[T] = {
      val promise = Promise[T]()

      val timer = new Timer(true) // Use daemon thread
      val task = new TimerTask {
        override def run(): Unit = {
          // Execute the block and complete the promise
          try {
            promise.success(block)
          } catch {
            case e: Throwable => promise.failure(e)
          }
        }
      }

      timer.schedule(task, duration.toMillis)
      promise.future
    }
  }

  // Animal service - simulates database operations
  object AnimalDatabase {
    // Returns Future[Either[ZooError, Animal]] - this pattern combines async with error handling
    def findAnimal(name: String): Future[Either[ZooError, Animal]] = {
      println(s"🔍 Searching for animal: $name")

      // Use non-blocking delay to simulate database lookup time
      DelayedFuture.by(50.millis) {
        // Simulate database lookup with potential errors
        name match {
          case "lion" => Right(Animal("Leo", "Lion"))
          case "tiger" => Right(Animal("Tigra", "Tiger"))
          case "bear" => Right(Animal("Bruno", "Bear"))
          case "error" => Left(DatabaseError("Database connection failed"))
          case _ => Left(ValidationError(s"Unknown animal: $name"))
        }
      }
    }
  }

  // Diet service - simulates network API calls
  object DietApi {
    // Returns Future[Either[ZooError, Diet]]
    def getDiet(animal: Animal): Future[Either[ZooError, Diet]] = {
      println(s"🥩 Fetching diet for: ${animal.name} the ${animal.species}")

      // Use non-blocking delay to simulate network latency
      DelayedFuture.by(100.millis) {
        animal.species match {
          case "Lion" => Right(Diet("Meat", 10))
          case "Tiger" => Right(Diet("Meat", 8))
          case "Bear" =>
            if (Instant.now().getNano % 2 == 0) { // Random failure
              Left(NetworkError("API timeout fetching bear diet"))
            } else {
              Right(Diet("Omnivore", 12))
            }
          case _ => Left(ValidationError(s"No diet information for ${animal.species}"))
        }
      }
    }
  }

  // Habitat service - another API
  object HabitatApi {
    // Returns Future[Either[ZooError, Habitat]]
    def getHabitat(animal: Animal): Future[Either[ZooError, Habitat]] = {
      println(s"🌳 Fetching habitat for: ${animal.name} the ${animal.species}")

      // Use non-blocking delay to simulate longer network latency
      DelayedFuture.by(150.millis) {
        animal.species match {
          case "Lion" => Right(Habitat("Savanna", "Hot"))
          case "Tiger" => Right(Habitat("Jungle", "Tropical"))
          case "Bear" => Right(Habitat("Forest", "Temperate"))
          case _ => Left(ValidationError(s"No habitat information for ${animal.species}"))
        }
      }
    }
  }

  /*
   * ZOO SERVICE - Combines all operations with proper error handling
   */
  object ZooService {
    // Timeout duration for operations
    val defaultTimeout = 1.second

    /*
     * USING FOR-YIELD WITH FUTURES AND EITHER
     * This is a clean pattern to sequentially compose futures while handling errors
     */
    def getAnimalProfile(animalName: String): Future[Either[ZooError, AnimalProfile]] = {
      // Start with a for-comprehension for sequential composition
      val result = for {
        // Step 1: Find the animal
        animalResult <- AnimalDatabase.findAnimal(animalName)

        // Step 2: If animal found, get its diet (otherwise short-circuit)
        dietResult <- animalResult match {
          case Right(animal) => DietApi.getDiet(animal).map(diet => diet.map(d => (animal, d)))
          case Left(error) => Future.successful(Left(error))
        }

        // Step 3: If diet found, get habitat (otherwise short-circuit)
        habitatResult <- dietResult match {
          case Right((animal, diet)) =>
            HabitatApi.getHabitat(animal)
              .map(habitat => habitat.map(h => (animal, diet, h)))
              // Add timeout without blocking
              .recover {
                case _: TimeoutException => Left(TimeoutError("Habitat information request timed out"))
              }
          case Left(error) => Future.successful(Left(error))
        }
      } yield {
        // Final step: Construct the profile if all data was retrieved
        habitatResult match {
          case Right((animal, diet, habitat)) =>
            println(s"✅ Successfully created profile for ${animal.name}")
            Right(AnimalProfile(animal, diet, habitat))
          case Left(error) => Left(error)
        }
      }

      // Add global error recovery
      result.recover {
        case e: Exception =>
          println(s"💥 Unexpected error: ${e.getMessage}")
          Left(NetworkError(s"Unexpected error: ${e.getMessage}"))
      }
    }
  }

  /*
   * ALTERNATIVE APPROACH USING FLATMAP
   * This demonstrates the same logic without for-comprehension
   */
  object ZooServiceAlt {
    def getAnimalProfile(animalName: String): Future[Either[ZooError, AnimalProfile]] = {
      AnimalDatabase.findAnimal(animalName).flatMap {
        case Left(error) =>
          Future.successful(Left(error))

        case Right(animal) =>
          DietApi.getDiet(animal).flatMap {
            case Left(error) =>
              Future.successful(Left(error))

            case Right(diet) =>
              HabitatApi.getHabitat(animal).map {
                case Left(error) =>
                  Left(error)

                case Right(habitat) =>
                  Right(AnimalProfile(animal, diet, habitat))
              }
          }
      }.recover {
        case e: Exception => Left(NetworkError(s"Unexpected error: ${e.getMessage}"))
      }
    }
  }

  // Demo execution
  def main(args: Array[String]): Unit = {
    println("🦁 Starting Future Best Practices Demo")

    // Example 1: Successful request
    println("\n=== Testing Lion (should succeed) ===")
    val lionProfile = ZooService.getAnimalProfile("lion")
    try {
      val result = Await.result(lionProfile, 2.seconds)
      result match {
        case Right(profile) =>
          println(s"Lion profile retrieved: ${profile.animal.name} eats ${profile.diet.foodType} " +
                 s"and lives in a ${profile.habitat.climate} ${profile.habitat.location}")
        case Left(error) =>
          println(s"Error retrieving lion profile: ${error.message}")
      }
    } catch {
      case e: Exception => println(s"Exception: ${e.getMessage}")
    }

    // Example 2: Error handling
    println("\n=== Testing 'error' (should return DatabaseError) ===")
    val errorProfile = ZooService.getAnimalProfile("error")
    try {
      val result = Await.result(errorProfile, 2.seconds)
      result match {
        case Right(profile) =>
          println(s"Profile retrieved (unexpected): ${profile.animal.name}")
        case Left(error) =>
          println(s"Error correctly handled: ${error.message}")
      }
    } catch {
      case e: Exception => println(s"Exception: ${e.getMessage}")
    }

    // Example 3: Unknown animal
    println("\n=== Testing 'unicorn' (should return ValidationError) ===")
    val unicornProfile = ZooService.getAnimalProfile("unicorn")
    try {
      val result = Await.result(unicornProfile, 2.seconds)
      result match {
        case Right(profile) =>
          println(s"Profile retrieved (unexpected): ${profile.animal.name}")
        case Left(error) =>
          println(s"Error correctly handled: ${error.message}")
      }
    } catch {
      case e: Exception => println(s"Exception: ${e.getMessage}")
    }

    // Example 4: Bear (might succeed or fail randomly)
    println("\n=== Testing Bear (random success/failure) ===")
    val bearProfile = ZooService.getAnimalProfile("bear")
    try {
      val result = Await.result(bearProfile, 2.seconds)
      result match {
        case Right(profile) =>
          println(s"Bear profile retrieved: ${profile.animal.name} eats ${profile.diet.foodType}")
        case Left(error) =>
          println(s"Bear profile error (expected sometimes): ${error.message}")
      }
    } catch {
      case e: Exception => println(s"Exception: ${e.getMessage}")
    }

    println("\n🏁 Future Best Practices Demo Completed")
  }

  /*
   * KEY LEARNING POINTS:
   *
   * 1. FUTURE WITH EITHER PATTERN:
   *    - Return Future[Either[Error, Success]] from functions
   *    - This separates expected business errors from unexpected exceptions
   *    - The Left side represents expected errors that should be handled
   *    - The Right side contains the successful result
   *
   * 2. COMPOSITION STRATEGIES:
   *    - For-yield provides clean sequential composition with proper control flow
   *    - FlatMap chains are an alternative when more complex branching is needed
   *    - Pattern matching on Either inside the composition handles error short-circuiting
   *
   * 3. ERROR HANDLING:
   *    - Type-safe errors with sealed traits/case classes
   *    - Separate business logic errors (Either) from technical exceptions (recover)
   *    - Global error recovery with .recover to handle exceptions
   *
   * 4. NON-BLOCKING DELAYS:
   *    - Never use Thread.sleep() in Future computations - it blocks threads
   *    - Use non-blocking delay mechanisms like the one demonstrated
   *    - Properly handle timeouts with Future.recover instead of blocking with Await
   *
   * 5. BEST PRACTICES:
   *    - Be explicit about error types and handling
   *    - Use descriptive error messages
   *    - Avoid blocking operations in Future executions
   *    - Consider using a more advanced library like Cats Effect or ZIO for complex apps
   */
}
