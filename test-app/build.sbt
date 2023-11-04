name := """test-app"""

version := "2.9.0-SNAPSHOT"

crossScalaVersions := Seq("2.13.12", "3.3.1")

scalaVersion := crossScalaVersions.value.head

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

javacOptions += "-Xlint:deprecation"
scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  guice,
  "be.objectify" %% "deadbolt-java" % "2.9.0",
  "com.jayway.restassured" % "rest-assured" % "2.9.0" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayJava)

resolvers ++= Resolver.sonatypeOssRepos("snapshots")
