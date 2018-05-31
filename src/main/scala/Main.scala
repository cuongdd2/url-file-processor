import java.nio.file.{Files, Path, Paths}

import dispatch.Defaults._
import dispatch._

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util.Try

object Main extends App {
  val dirName = "src/main/resources/inputData"
  val OK = 200
  var BatchSize = 10000
  val timeout = 30.second
  var total, done = 0

  Http.defaultClientBuilder.setIoThreadsCount(4)

  val start = System.currentTimeMillis()

  Files.list(Paths.get(dirName)).forEach(processFile)

  val end = (System.currentTimeMillis() - start) / 1000.0
  println(f"\ntime: $end%.2f sec, ${total / end}%.0f op/s")

  Http.default.shutdown()

  def processFile(path: Path): Unit = {
    try {
      val stream = Source.fromFile(path.toUri, "utf-8").getLines.toStream
      consumeStream(stream)
    } catch {
      case e: Exception => println(e)
    }
  }

  @tailrec
  def consumeStream(s: Stream[String]): Unit = {
    if (s.nonEmpty) {
      val (head, tail) = s.splitAt(BatchSize)
      batchProcess(head.toVector)
      consumeStream(tail)
    }
  }

  def batchProcess(s: Seq[String]): Unit = {
    val f = Future.traverse(s)(asyncHttp).map(_.count(_ == OK))
    val count = Await.result(f, timeout) // TODO: switch to async when "Too many open files" bug fixed
    total += s.size
    done += count
    val error = total - done
    println(f"sum: $total%7s     ok: $done%7s     err: $error%5s")
  }

  @inline
  def asyncHttp(s: String): Future[Int] = Http.default(url(s)).map(_.getStatusCode)
}
