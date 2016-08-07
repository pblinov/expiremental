name := """akka-test"""

version := "1.0"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

// Uncomment to use Akka
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test"
libraryDependencies += "junit" % "junit" % "4.11" % "test"
libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"




