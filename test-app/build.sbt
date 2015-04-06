name := """test-app"""

version := "2.4.0-SNAPSHOT"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.11.5", "2.10.4")

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "com.jayway.restassured" % "rest-assured" % "2.4.0" % "test",
  "org.dbunit" % "dbunit" % "2.5.0" % "test"
)

lazy val deadboltJava = (project in file("modules/deadbolt-java")).enablePlugins(PlayJava)

lazy val root = (project in file(".")).enablePlugins(PlayJava).dependsOn(deadboltJava).aggregate(deadboltJava)

sbt.Keys.fork in (Test) := false

resolvers += Resolver.sonatypeRepo("snapshots")
