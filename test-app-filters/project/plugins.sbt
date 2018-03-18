addSbtPlugin("com.typesafe.play" % "sbt-plugin" % System.getProperty("playTestVersion", "2.7.0-2018-03-18-683e8c0-SNAPSHOT"))

resolvers += Resolver.sonatypeRepo("snapshots")
