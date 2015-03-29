name := """test-app"""

version := "2.3.3-SNAPSHOT"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.11.1", "2.10.4")

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "com.jayway.restassured" % "rest-assured" % "1.8.1" % "test",
  "org.dbunit" % "dbunit" % "2.4.9" % "test"
)

lazy val deadboltJava = (project in file("modules/deadbolt-java")).enablePlugins(PlayJava)

lazy val root = (project in file(".")).enablePlugins(PlayJava).dependsOn(deadboltJava).aggregate(deadboltJava)

sbt.Keys.fork in (Test) := false

resolvers += Resolver.sonatypeRepo("snapshots")
