name := "deadbolt-java"

lazy val root = (project in file(".")).enablePlugins(PlayJava).disablePlugins(PlayFilters)

scalaVersion := "2.11.12"

crossScalaVersions := Seq("2.11.12", "2.12.4")

organization := "be.objectify"

libraryDependencies ++= Seq(
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.awaitility" % "awaitility" % "3.0.0" % "test"
)

releasePublishArtifactsAction := PgpKeys.publishSigned.value

// workaround for scaladoc error https://github.com/scala/scala-dev/issues/249
// also see http://www.scala-lang.org/news/2.12.0/#scaladoc-can-be-used-to-document-java-sources
scalacOptions in (Compile, doc) ++= {
  if (scalaBinaryVersion.value == "2.12") Seq("-no-java-comments") else Nil
}
