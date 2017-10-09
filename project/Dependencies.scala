import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "[3.1.0,)"
  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "[1.13.5,)"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "[1.2.3,)"
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "[3.7.2,)"
  lazy val commonsEmail = "org.apache.commons" % "commons-email" % "[1.5,)"
  lazy val xml = "org.scala-lang.modules" %% "scala-xml" % "[1.0.6,)"
}
