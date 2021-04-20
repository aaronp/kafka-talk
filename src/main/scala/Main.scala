@main def hello: Unit = {
    println("Hello world!")
    println(scala.util.Try(msg))
}

def msg = "I was compiled by Scala 3. :)"
