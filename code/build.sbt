name := "deadbolt-java"

lazy val root = (project in file(".")).enablePlugins(PlayJava).disablePlugins(PlayFilters)

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0-M5")

organization := "be.objectify"

libraryDependencies ++= Seq(
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.awaitility" % "awaitility" % "3.0.0" % "test"
)

releasePublishArtifactsAction := PgpKeys.publishSigned.value

// workaround for scaladoc error https://github.com/scala/scala-dev/issues/249
// also see http://www.scala-lang.org/news/2.12.0/#scaladoc-can-be-used-to-document-java-sources
scalacOptions in (Compile, doc) ++= {
  if (scalaBinaryVersion.value == "2.11") Nil else Seq("-no-java-comments")
}

javacOptions += "-Xlint:deprecation"
scalacOptions += "-deprecation"

resolvers += Resolver.sonatypeRepo("snapshots")

// Workaround until omnidoc gets published for Scala 2.13
// http://central.maven.org/maven2/com/typesafe/play/play-omnidoc_2.13/
PlayKeys.playOmnidoc := false
