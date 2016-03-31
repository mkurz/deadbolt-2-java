import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "deadbolt-java"
  val appVersion      = "2.2.1-SNAPSHOT"

  val appDependencies = Seq(
    javaCore,
    cache,
    "be.objectify" %% "deadbolt-core" % "2.2.0"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    organization := "be.objectify"
  )
}
