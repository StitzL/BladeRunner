lazy val root = (project in file(".")).
  settings(
    name := "bladerunner",
    version := "1.1",
    scalaVersion := "2.12.8",
	
	libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.23",
	libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.23",
	libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.144-R12",
	
	resolvers += Opts.resolver.sonatypeSnapshots 
  )