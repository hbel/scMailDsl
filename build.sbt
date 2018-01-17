import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.hbel",
      scalaVersion := "2.12.4",
      version := "0.1.1"
    )),
    name := "scMailDsl",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      scalaCheck % Test,
      logback,
      scalaLogging,
      commonsEmail,
      xml
    )
  )
