lazy val scalafx = "org.scalafx" %% "scalafx" % "8.0.144-R12"

lazy val root = (project in file("."))
  .settings(
    scalacOptions += "-deprecation",
    name := "Tynooc",
    scalaVersion := "2.12.4",
    fork := true, // avoids double initialisation problems on run
    libraryDependencies ++= Seq(
        scalafx,
        "com.fasterxml.jackson.core" % "jackson-core" % "2.9.4",
        "com.fasterxml.jackson.core" % "jackson-annotations" % "2.9.4",
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.4",
        "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % "2.9.4",
        "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.9.4",
        "net.jcazevedo" %% "moultingyaml" % "0.4.0"
    )
  )
