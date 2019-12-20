name := "deadbolt-java"

lazy val root = (project in file(".")).enablePlugins(PlayJava).disablePlugins(PlayFilters)

scalaVersion := "2.13.1"

crossScalaVersions := Seq("2.12.10", "2.13.1")

organization := "be.objectify"

libraryDependencies ++= Seq(
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.awaitility" % "awaitility" % "3.0.0" % "test"
)

releasePublishArtifactsAction := PgpKeys.publishSigned.value

javacOptions += "-Xlint:deprecation"
scalacOptions += "-deprecation"

resolvers += Resolver.sonatypeRepo("snapshots")
