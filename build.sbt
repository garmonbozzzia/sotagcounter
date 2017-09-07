name := "sotagcounter"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.10"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.4" // or whatever the latest version is
libraryDependencies += "com.typesafe.akka" %% "akka-actor"  % "2.5.4" // or whatever the latest version is
libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.3"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.5.3"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.0.10"