addSbtPlugin("com.typesafe.play" % "sbt-plugin" % Option(System.getProperty("play.version")).getOrElse("2.7.0-2018-03-11-191cdf0-SNAPSHOT"))
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.5")

resolvers += Resolver.sonatypeRepo("snapshots")
