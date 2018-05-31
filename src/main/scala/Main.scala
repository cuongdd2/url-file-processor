import java.nio.file.{Files, Path, Paths}

import dispatch.Defaults._
import dispatch._

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util.Try

object Main {
  var RealData = true
  lazy val dirName: String =
    if (RealData) "src/main/resources/inputData"
    else "src/test/resources/testData"
  val OK = 200
  var BatchSize = 5000
  private val timeout = 500.millis
  var total, done = 0

  def main(args: Array[String]): Unit = {
    Http.defaultClientBuilder.setIoThreadsCount(4)

    val startTime = System.currentTimeMillis()

    val files = Files.list(Paths.get(dirName))
    files.forEach(file => consumeStream(processFile(file)))

    val endTime = (System.currentTimeMillis() - startTime) / 1000.0
    println(f"\ntime: $endTime%.2f sec, ${total / endTime}%.0f op/s")

    Http.default.shutdown()
  }

  def processFile(path: Path): Stream[String] = {
    Try(Source.fromFile(path.toUri, "utf-8").getLines.toStream) getOrElse Stream()
  }

  @tailrec
  def consumeStream(s: Stream[String]): Unit = {
    if (s.nonEmpty) {
      val (head, tail) = s.splitAt(BatchSize)
      asyncBatch(head)
      //      syncBatch(head)
      consumeStream(tail)
    }
  }

  def asyncBatch(seq: Seq[String]): Unit = {
    val countF = Future.traverse(seq)(asyncHttp).map(_.count(_ == OK))
    val count = Try(Await.result(countF, timeout)).getOrElse(0)
    report(seq.size, count)
  }

  def syncBatch(seq: Seq[String]): Unit = {
    val statusCodes = seq.par.map(syncHttp)
    val count = statusCodes.count(_ == OK)
    report(seq.size, count)
  }

  def report(size: Int, success: Int): Unit = {
    total += size
    done += success
    val error = total - done
    println(f"sum: $total%7s     ok: $done%7s     err: $error%5s")
  }

  @inline
  def asyncHttp(s: String): Future[Int] = Http.default(url(s)).map(_.getStatusCode)

  def syncHttp(s: String): Int = {
    val res = Http.default.client.prepareGet(s).execute().toCompletableFuture.get
    res.getStatusCode
  }
}
