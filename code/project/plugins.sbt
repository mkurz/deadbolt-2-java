addSbtPlugin("com.typesafe.play" % "sbt-plugin" % Option(System.getProperty("play.version")).getOrElse("2.8.0-RC5"))
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.2-1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")

resolvers += Resolver.sonatypeRepo("snapshots")
