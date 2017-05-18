name := """test-app"""

version := "2.6.0-M5-SNAPSHOT"

scalaVersion := "2.11.11"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

libraryDependencies ++= Seq(
  "be.objectify" %% "deadbolt-java" % "2.6.0-M5-SNAPSHOT",
  "com.jayway.restassured" % "rest-assured" % "2.4.0" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayJava)

sbt.Keys.fork in (Test) := false
