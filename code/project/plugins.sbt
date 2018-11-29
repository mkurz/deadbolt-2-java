addSbtPlugin("com.typesafe.play" % "sbt-plugin" % Option(System.getProperty("play.version")).getOrElse("2.7.0-RC8"))
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.2-1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.10")

resolvers += Resolver.sonatypeRepo("snapshots")
