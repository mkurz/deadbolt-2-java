name := """test-app-filters"""

version := "2.7.0-SNAPSHOT"

scalaVersion := "2.12.5"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

libraryDependencies ++= Seq(
  guice,
  "be.objectify" %% "deadbolt-java" % "2.7.0-SNAPSHOT",
  "com.jayway.restassured" % "rest-assured" % "2.4.0" % "test"
)

resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file(".")).enablePlugins(PlayJava)

sbt.Keys.fork in Test := false
