name := """test-app-filters"""

crossScalaVersions := Seq("2.13.15", "3.3.3")

scalaVersion := crossScalaVersions.value.head

// sync this setting with the one main build.sbt
ThisBuild / dynverVTagPrefix := false

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

javacOptions += "-Xlint:deprecation"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  guice,
  "be.objectify" %% "deadbolt-java" % version.value,
  "io.rest-assured" % "rest-assured" % "5.5.0" % Test
)

lazy val root = (project in file(".")).enablePlugins(PlayJava)
