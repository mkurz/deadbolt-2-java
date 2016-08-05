name := "deadbolt-java"

version := "2.5.2-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

organization := "be.objectify"

libraryDependencies ++= Seq(
  cache,
  "org.mockito" % "mockito-all" % "1.10.19" % "test"
)

resolvers += Resolver.sonatypeRepo("snapshots")

