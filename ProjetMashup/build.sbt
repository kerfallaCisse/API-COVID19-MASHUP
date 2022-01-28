lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """projet-mashup""",
    organization := "ch.unige",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )

resolvers += Resolver.bintrayRepo("jroper", "maven")

libraryDependencies += "au.id.jazzy" %% "play-geojson" % "1.7.0"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"

libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.3.17"

libraryDependencies += "com.softwaremill.sttp.client3" %% "play-json" % "3.3.17"

libraryDependencies += "com.lihaoyi" %% "upickle" % "1.4.3"

libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.8.0"

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.4.2"
