addSbtPlugin("com.typesafe.play" % "sbt-plugin" % System.getProperty("playTestVersion", "2.7.0"))

resolvers += Resolver.sonatypeRepo("snapshots")
