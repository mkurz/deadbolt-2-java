addSbtPlugin("com.typesafe.play" % "sbt-plugin" % System.getProperty("playTestVersion", "2.8.0-RC5"))

resolvers += Resolver.sonatypeRepo("snapshots")
