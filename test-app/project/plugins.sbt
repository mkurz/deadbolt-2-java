addSbtPlugin("com.typesafe.play" % "sbt-plugin" % System.getProperty("playTestVersion", "2.7.0-M1"))

resolvers += Resolver.sonatypeRepo("snapshots")
