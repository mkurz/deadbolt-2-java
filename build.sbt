name := "deadbolt-java"

version := "2.3.1"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.11.1", "2.10.4")

organization := "be.objectify"

libraryDependencies ++= Seq(
  cache,
  "be.objectify" %% "deadbolt-core" % "2.3.1"
)

resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.io/releases/"))(Resolver.ivyStylePatterns)
