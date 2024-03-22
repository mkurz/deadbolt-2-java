name := "deadbolt-java"

lazy val root = (project in file(".")).enablePlugins(PlayWeb).disablePlugins(PlayFilters, PlayLogback, PlayAkkaHttpServer)

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
ThisBuild / dynverVTagPrefix := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

homepage := Some(url("https://github.com/mkurz/deadbolt-2-java")) // Some(url("http://deadbolt.ws"))
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
organization := "be.objectify"

crossScalaVersions := Seq("2.13.13", "3.3.1")

scalaVersion := crossScalaVersions.value.head

libraryDependencies := libraryDependencies.value.filterNot(m => m.name == "twirl-api" || m.name == "play-server") ++ Seq(
  "org.mockito" % "mockito-core" % "2.28.2" % "test",
  "org.awaitility" % "awaitility" % "3.1.6" % "test",
  playCore % "provided",
  javaCore % "test",
)

javacOptions ++= Seq("-Xlint:deprecation", "-encoding", "UTF-8", "--release", "11")
scalacOptions ++= Seq("-deprecation", "-release:11")

TwirlKeys.templateImports ++= Seq(
  "java.util.Optional"
)
Test / TwirlKeys.templateImports ++= Seq(
  "java.util.List",
  "play.core.j.PlayMagicForJava._",
)

developers ++= List(Developer(
    "mkurz",
    "Matthias Kurz",
    "m.kurz@irregular.at",
    url("https://github.com/mkurz")
  ),
  Developer(
    "schaloner",
    "Steve Chaloner",
    "john.doe@example.com",
    url("https://github.com/schaloner")
  ),
)
