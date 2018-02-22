val scalafx = "org.scalafx" %% "scalafx" % "8.0.144-R12"

lazy val root = (project in file("."))
  .settings(
    name := "Tynooc",
    scalaVersion := "2.12.4",
    libraryDependencies += scalafx
  )
