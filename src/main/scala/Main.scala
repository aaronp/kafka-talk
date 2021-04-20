@main def hello: Unit = {
    val latch = new CountDownLatch(1)

    val streams = WordCount()
    // attach shutdown handler to catch control-c
    Runtime.getRuntime.addShutdownHook(new Thread("streams-shutdown-hook") {
        @Override def run(): Unit = {
            streams.close
            latch.countDown
        }
    })

    try {
        streams.start
        latch.await
    } catch {
        case NonFatal(_) => System.exit(1)
    }
}

def msg = "I was compiled by Scala 3. :)"
