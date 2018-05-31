
object CheckJVM extends App {
  val consoleStream = Runtime.getRuntime.exec(Array[String]("bash", "-c", "ulimit -a")).getInputStream
  var byteData = 0
  while ({byteData = consoleStream.read(); byteData != -1})
    System.out.write(byteData)
}
