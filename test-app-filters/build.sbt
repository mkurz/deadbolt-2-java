name := """test-app-filters"""

version := "2.8.2-SNAPSHOT"

scalaVersion := "2.13.1"

crossScalaVersions := Seq("2.12.10", "2.13.1")

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

javacOptions += "-Xlint:deprecation"
scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  guice,
  "be.objectify" %% "deadbolt-java" % "2.8.2-SNAPSHOT",
  "com.jayway.restassured" % "rest-assured" % "2.4.0" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayJava)

resolvers += Resolver.sonatypeRepo("snapshots")
