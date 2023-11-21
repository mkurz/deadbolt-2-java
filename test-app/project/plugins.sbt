addSbtPlugin("com.typesafe.play" % "sbt-plugin" % System.getProperty("playTestVersion", "2.9.0"))

resolvers ++= Resolver.sonatypeOssRepos("snapshots")
