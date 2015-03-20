name := "deadbolt-java"

version := "2.3.3-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.11.5", "2.10.4")

organization := "be.objectify"

libraryDependencies ++= Seq(
  cache,
  "be.objectify" %% "deadbolt-core" % "2.3.3-SNAPSHOT"
)

resolvers += Resolver.sonatypeRepo("snapshots") 
