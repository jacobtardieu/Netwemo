name := """Netwemo"""

version := "2.0.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.11"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.21"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % "test"

libraryDependencies += "org.mockito" % "mockito-core" % "2.2.9"
