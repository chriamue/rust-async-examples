import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets

object AsyncHttpPost {
  // Define a simple HTTP client using Java's HttpClient
  object SimpleHttpClient {
    def post(url: String, body: String): Future[String] = Future {
      val client = HttpClient.newBuilder().build()
      val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build()

      val response = client.send(request, HttpResponse.BodyHandlers.ofString())
      response.body()
    }
  }

  def main(args: Array[String]): Unit = {
    val testBody = "Hello from World!"

    // Execute the HTTP POST
    val responseFuture = SimpleHttpClient.post("http://httpbin.org/post", testBody)

    try {
      // Wait for the response
      val response = Await.result(responseFuture, 10.seconds)
      println(s"POST response: $response")
      assert(response.contains(testBody))
      println("Success! Response contains the test body.")
    } catch {
      case e: Exception =>
        println(s"Error during HTTP POST: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}
