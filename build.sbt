name := """Netwemo"""

version := "2.0.0"

scalaVersion := "2.12.2"

val akkaVersion = "2.4.18"
val akkaHttpVersion = "10.0.7"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test"

libraryDependencies += "org.mockito" % "mockito-core" % "2.8.9"
