name := "deadbolt-java"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

crossScalaVersions := Seq("2.11.11", "2.12.2")

organization := "be.objectify"

libraryDependencies ++= Seq(
  "org.mockito" % "mockito-all" % "1.10.19" % "test"
)

releasePublishArtifactsAction := PgpKeys.publishSigned.value

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

