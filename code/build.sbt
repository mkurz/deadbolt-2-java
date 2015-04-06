name := "deadbolt-java"

version := "2.4.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(play.PlayJava)

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.11.5", "2.10.4")

organization := "be.objectify"

libraryDependencies ++= Seq(
  cache,
  "be.objectify" %% "deadbolt-core" % "2.4.0-SNAPSHOT",
  "org.mockito" % "mockito-all" % "1.10.19" % "test"
)

resolvers += Resolver.sonatypeRepo("snapshots") 

