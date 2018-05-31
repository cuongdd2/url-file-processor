import java.nio.file.Paths

import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._


class TestSuite extends FunSuite with BeforeAndAfter {

  val url = "https://www.google.com"
  val badUrl = "https://www.google.com/badurl"
  val urls = Stream(url, badUrl)
  val path = Paths.get("src/test/resources/testData/url")
  val badPath = Paths.get("src/test/resources/testData/url2")

  before {
    Main.total = 0
    Main.done = 0
  }

  after {
    Main.total = 0
    Main.done = 0
  }

  test("syncHttp success") {
    val code = Main.syncHttp(url)
    assert(code === 200)
  }

  test("syncHttp fail") {
    val code = Main.syncHttp(badUrl)
    assert(code !== 200)
  }

  test("asyncHttp success") {
    val codeF = Main.asyncHttp(url)
    val code = Await.result(codeF, 1 minute)
    assert(code === 200)
  }

  test("asyncHttp fail") {
    val codeF = Main.asyncHttp(badUrl)
    val code = Await.result(codeF, 1 minute)
    assert(code !== 200)
  }

  test("syncBatch") {
    Main.syncBatch(urls)
    assert(Main.total === 2)
    assert(Main.done === 1)
  }

  test("asyncBatch") {
    Main.asyncBatch(urls)
    assert(Main.total === 2)
    assert(Main.done === 1)
  }

  test("consumeStream") {
    Main.consumeStream(urls)
    assert(Main.total === 2)
    assert(Main.done === 1)
  }

  test("processFile success") {
    val stream = Main.processFile(path)
    assert(stream.size === 2)
    assert(stream.isInstanceOf[Stream[String]])
  }

  test("processFile fail") {
    val stream = Main.processFile(badPath)
    assert(stream.size === 0)
    assert(stream.isInstanceOf[Stream[String]])
  }

  test("main") {
    Main.RealData = false
    Main.main(Array())
    assert(Main.total === 2)
    assert(Main.done === 1)
  }

}
