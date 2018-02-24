lazy val scalafx = "org.scalafx" %% "scalafx" % "8.0.144-R12"

lazy val root = (project in file("."))
  .settings(
    scalacOptions += "-deprecation",
    name := "Tynooc",
    scalaVersion := "2.12.4",
    fork := true, // avoids double initialisation problems on run
    libraryDependencies += scalafx
  )
