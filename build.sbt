lazy val root = (project in file(".")).
  settings(
    name := "bladerunner",
    version := "1.0",
    scalaVersion := "2.11.7",
	
	libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.2",
	//libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.4.2",
	libraryDependencies += "com.typesafe.akka" % "akka-stream-experimental_2.11" % "2.0.3",
	libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.60-R9"
  )