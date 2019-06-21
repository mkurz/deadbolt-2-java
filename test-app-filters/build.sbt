name := """test-app-filters"""

version := "2.7.1-SNAPSHOT"

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0")

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

javacOptions += "-Xlint:deprecation"
scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  guice,
  "be.objectify" %% "deadbolt-java" % "2.7.1-SNAPSHOT",
  "com.jayway.restassured" % "rest-assured" % "2.4.0" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayJava)

resolvers += Resolver.sonatypeRepo("snapshots")

sbt.Keys.fork in Test := true // can be removed with sbt 1.3.0, see https://github.com/sbt/sbt/issues/4609

// Workaround until omnidoc gets published for Scala 2.13
// http://central.maven.org/maven2/com/typesafe/play/play-omnidoc_2.13/
PlayKeys.playOmnidoc := false
