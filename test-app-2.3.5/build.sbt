name := """test-app-2.3.5"""

version := "2.3.3-SNAPSHOT"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.11.1", "2.10.4")

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

lazy val deadboltJava = (project in file("modules/deadbolt-java")).enablePlugins(PlayJava)

lazy val root = (project in file(".")).enablePlugins(PlayJava).dependsOn(deadboltJava).aggregate(deadboltJava)
