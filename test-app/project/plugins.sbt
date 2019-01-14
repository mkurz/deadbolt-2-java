addSbtPlugin("com.typesafe.play" % "sbt-plugin" % System.getProperty("playTestVersion", "2.7.0-RC9"))

resolvers += Resolver.sonatypeRepo("snapshots")
