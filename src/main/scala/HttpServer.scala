import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import scala.concurrent.Future
import org.json4s._
import org.json4s.jackson.JsonMethods._

case class ResponseItem(tags: List[String], is_answered: Boolean)

case class CountResult(total: Int, answered: Int)

object HttpServer extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val format = DefaultFormats

  def request2StackOverflow(tag: String) = Get(Uri("https://api.stackexchange.com/2.2/search").withQuery(Query(
    "pagesize" -> "100", "order" -> "desc", "sort" -> "creation", "tagged" -> tag, "site" -> "stackoverflow")))

  def loadData(tags: Seq[String]): Future[Seq[ResponseItem]] =
    Source.fromIterator(() => tags.iterator)
      .map(request2StackOverflow)
      .mapAsync(1) { request =>
        for {
          responce <- Http().singleRequest(request)
          text <- responce.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
          decoded <- Gzip.decode(text)
        } yield (parse(decoded.utf8String) \ "items").extract[Seq[ResponseItem]]
      }.runFold(Seq.empty[ResponseItem])(_ ++ _)

  def countTags: Seq[ResponseItem] => Map[String, (Int, Int)] =
    _.flatMap(x => x.tags.map(_ -> x.is_answered))
      .groupBy(_._1).mapValues(x => x.size -> x.count(_._2))

  def render(data: Map[String, (Int, Int)]) =
    s"{\n  ${
      data.toSeq.sortBy(-_._2._2).map {
        case (k, (v1, v2)) => s""""$k": { "total": $v1, "answered": $v2}"""
      }.mkString(",\n  ")
    }\n}"

  val route =
    path("search") {
      get {
        parameterSeq { params =>
          onSuccess(loadData(params.collect { case ("tag", v) => v }.distinct)) { result =>
            complete(render(countTags(result)))
          }
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
}
