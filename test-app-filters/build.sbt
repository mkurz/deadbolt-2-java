name := """test-app-filters"""

version := "2.5.6-SNAPSHOT"

scalaVersion := "2.11.7"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

libraryDependencies ++= Seq(
  javaJdbc,
//  cache,
  javaWs,
  "be.objectify" %% "deadbolt-java" % "2.5.6-SNAPSHOT",
  "com.jayway.restassured" % "rest-assured" % "2.4.0" % "test",
  "org.dbunit" % "dbunit" % "2.5.0" % "test"
)

//lazy val deadboltJava = (project in file("./modules/deadbolt-java")).enablePlugins(PlayJava)

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

sbt.Keys.fork in Test := false

resolvers += Resolver.sonatypeRepo("snapshots")

routesGenerator := InjectedRoutesGenerator
