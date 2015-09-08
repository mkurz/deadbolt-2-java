name := "deadbolt-java"

version := "2.4.2.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(play.PlayJava)

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.11.6", "2.10.5")

organization := "be.objectify"

libraryDependencies ++= Seq(
  cache,
  "be.objectify" %% "deadbolt-core" % "2.4.2",
  "org.mockito" % "mockito-all" % "1.10.19" % "test"
)

resolvers += Resolver.sonatypeRepo("snapshots") 
