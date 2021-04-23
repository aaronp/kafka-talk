package example

import java.util.concurrent.CountDownLatch
import scala.sys.ShutdownHookThread
import scala.util.control.NonFatal

object Main {
  def main(args: Array[String]) = {

    val latch = new CountDownLatch(1)
    val streams = WordCount()

    // attach shutdown handler to catch control-c
    ShutdownHookThread {
      streams.close
      latch.countDown
    }

    println("Running Work Count....")
    streams.start
    latch.await
    println("Done!")
  }
}
