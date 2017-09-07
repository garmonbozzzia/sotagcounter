import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.duration._
import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.testkit.TestDuration


class CounterTest extends WordSpec with Matchers with ScalatestRouteTest {
  implicit val format = DefaultFormats
  implicit val timeout = RouteTestTimeout(5.seconds dilated)
  "Подсчет вопросов с тегом scala находит ровно 100 элементов" in {
    Get("/search?tag=scala") ~> HttpServer.route ~> check {
      (parse(responseAs[String]) \ "scala" \ "total").extract[Int] shouldEqual 100
    }
  }
}
