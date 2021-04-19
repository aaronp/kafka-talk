val scala3Version = "3.0.0-RC2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "kafka-talk",
    version := "0.0.1",
    scalaVersion := scala3Version,
		libraryDependencies ++= List(
			"org.scalatest"        %% "scalatest"               % "3.2.7"  % Test,
			"org.pegdown"          % "pegdown"                  % "1.6.0"  % Test,
			"junit"                % "junit"                    % "4.12"   % Test,
			"com.vladsch.flexmark" % "flexmark-profile-pegdown" % "0.36.8" % Test,
			"javax.xml.bind"       % "jaxb-api"                 % "2.3.0"  % "provided"
		)
  )
