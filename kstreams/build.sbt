import java.nio.file.Path

val scala3Version = "3.0.0-RC2"

lazy val cucumberDeps = List(
  "io.cucumber" % "cucumber-core" % "4.3.0" % "test",
  "io.cucumber" %% "cucumber-scala" % "4.3.0" % "test",
  "io.cucumber" % "cucumber-jvm" % "4.3.0" % "test",
  "io.cucumber" % "cucumber-junit" % "4.3.0" % "test",
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "kafka-talk",
    version := "0.0.1",
    scalaVersion := scala3Version,
    libraryDependencies ++= List(
      "org.apache.avro" % "avro" % "1.10.1",
      "org.apache.kafka" % "kafka-streams" % "2.7.0",
      "org.apache.kafka" % "kafka-streams-test-utils" % "2.0.0" % Test,
      "org.scalatest" %% "scalatest" % "3.2.7" % Test,
      "org.pegdown" % "pegdown" % "1.6.0" % Test,
      "com.vladsch.flexmark" % "flexmark-profile-pegdown" % "0.36.8" % Test,
      "javax.xml.bind" % "jaxb-api" % "2.3.0" % "provided"
    )
  )


lazy val startKafka = taskKey[Boolean]("Ensures Kafka is started/running locally (returns a boolean which answers the question 'was kafka already running before we started it?')").withRank(KeyRanks.APlusTask)

def kafka = dockerenv.kafka()

startKafka := {
  val wasRunning = kafka.isRunning()
  streams.value.log.info("\uD83D\uDE80 Starting Kafka.... \uD83D\uDE80")
  kafka.start()
  wasRunning
}

lazy val stopKafka = taskKey[Unit]("Ensures Kafka is stopped").withRank(KeyRanks.APlusTask)

stopKafka := {
  streams.value.log.info("Stopping Kafka....")
  kafka.stop()
}

lazy val testLocal = taskKey[Unit]("Runs integration tests locally").withRank(KeyRanks.APlusTask)
testLocal := stopKafka.dependsOn((Test / test).dependsOn(startKafka)).value