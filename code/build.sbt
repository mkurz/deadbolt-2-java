name := "deadbolt-java"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

organization := "be.objectify"

libraryDependencies ++= Seq(
  "org.mockito" % "mockito-all" % "1.10.19" % "test"
)

releasePublishArtifactsAction := PgpKeys.publishSigned.value