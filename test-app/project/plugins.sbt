addSbtPlugin("com.typesafe.play" % "sbt-plugin" % System.getProperty("playTestVersion", "2.7.0-2018-03-11-191cdf0-SNAPSHOT"))

resolvers += Resolver.sonatypeRepo("snapshots")
