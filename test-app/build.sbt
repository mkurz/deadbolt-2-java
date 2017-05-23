name := """test-app"""

version := "2.6.0-RC1-SNAPSHOT"

scalaVersion := "2.11.11"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

libraryDependencies ++= Seq(
  guice,
  "be.objectify" %% "deadbolt-java" % "2.6.0-RC1-SNAPSHOT",
  "com.jayway.restassured" % "rest-assured" % "2.4.0" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayJava)

sbt.Keys.fork in (Test) := false
