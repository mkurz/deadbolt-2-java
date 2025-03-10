name := """test-app"""

crossScalaVersions := Seq("2.13.16", "3.3.5")

scalaVersion := crossScalaVersions.value.head

// sync this setting with the one main build.sbt
ThisBuild / dynverVTagPrefix := false

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

javacOptions += "-Xlint:deprecation"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  guice,
  "be.objectify" %% "deadbolt-java" % version.value,
  "io.rest-assured" % "rest-assured" % "5.5.1" % Test
)

lazy val root = (project in file(".")).enablePlugins(PlayJava)
