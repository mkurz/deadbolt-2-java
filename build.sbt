import play.PlayImport._

import PlayKeys._

organization := "be.objectify"

name := "deadbolt-java"

version := "2.3-RC1"

libraryDependencies ++= Seq(
  "be.objectify" %% "deadbolt-core" % "2.2.1-RC1",
  javaCore,
  cache
)

resolvers ++= Seq(
  Resolver.url("Objectify Play Repository", url("http://schaloner.github.io/releases/"))(Resolver.ivyStylePatterns),
  Resolver.url("Objectify Play Snapshot Repository", url("http://schaloner.github.io/snapshots/"))(Resolver.ivyStylePatterns)
)

lazy val root = (project in file(".")).addPlugins(PlayJava)
