import java.nio.file.{Files, Path, Paths}

import org.asynchttpclient.Dsl._

import scala.annotation.tailrec
import scala.compat.java8.FutureConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

object V1Run extends App {
  val dirName = "src/main/resources/inputData"
  val OK = 200
  val Cores = 4
  val Threshold = 2500
  val timeout = 1.minute
  var total, error, done = 0
  val httpClient = asyncHttpClient()

  val start = System.currentTimeMillis()

  Files.list(Paths.get(dirName)).forEach(s => readFile(s))

  val end = (System.currentTimeMillis() - start) / 1000.0
  println(f"time: $end%.2fs, ${total / end}%.0fops")

  httpClient.close()

  def readFile(path: Path): Unit = {
    try {
      val stream = Source.fromFile(path.toUri, "utf-8").getLines.toStream
      parallel(stream)
      println(s"complete $path")
    } catch {
      case e: Exception => println(e)
    }
  }

  @tailrec
  def parallel(s: Stream[String]): Unit = {
    if (s.nonEmpty) {
      val (head, tail) = s.splitAt(Threshold)
      process(head)
      parallel(tail)
    }
  }

  def process(s: Stream[String]): Unit = {
    val f = Future.sequence(s.map(asyncHttp)).map(_.reduceLeft((a, b) => (a._1 + b._1, a._2 + b._2)))
    val (a, b) = Await.result(f, timeout)
    done += a
    error += b
    total = done + error
    if (total % 10000 == 0) println(s"sum: $total\t\tok: $done\t\terr: $error")
  }

  def asyncHttp(url: String) =
    httpClient.prepareGet(url).execute().
      toCompletableFuture.toScala.
      map(res =>
        if (res.getStatusCode == OK) (1, 0)
        else (0, 1)
      )

  def syncHttp(url: String) = {
    val res = httpClient.prepareGet(url).execute().get
    if (res.getStatusCode == OK) (1, 0)
    else (0, 1)
  }
}
