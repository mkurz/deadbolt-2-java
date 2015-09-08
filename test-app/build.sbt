name := """test-app"""

version := "2.4.2.1-SNAPSHOT"

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.11.6", "2.10.5")

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "be.objectify" %% "deadbolt-java" % "2.4.2.1-SNAPSHOT",
  "com.jayway.restassured" % "rest-assured" % "2.4.0" % "test",
  "org.dbunit" % "dbunit" % "2.5.0" % "test"
)

//lazy val deadboltJava = (project in file("./modules/deadbolt-java")).enablePlugins(PlayJava)

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

sbt.Keys.fork in (Test) := false

resolvers += Resolver.sonatypeRepo("snapshots")
