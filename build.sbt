import sbt._

val scalametaVersion = "3.7.4"

val unusedWarnings =
  "-Ywarn-unused" ::
  "-Ywarn-unused-import" ::
  Nil

val commonSettings = Seq(
  fork in Test := true,
  scalaVersion := "2.12.6",
  organization := "com.github.bakenezumi",
  version := "0.1.0-SNAPSHOT"
)

lazy val root = (project in file("."))
  .settings(
    publish := {},
    publishLocal := {},
    skip in publish := true
  ) aggregate plugin

lazy val plugin = (project in file("sbt-plugin"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "scala2protobuf-sbt",
    commonSettings,
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    moduleName := "scala2protobuf-sbt",
    libraryDependencies ++=
      "org.scalameta" %% "scalameta" % scalametaVersion ::
      "org.scalameta" %% "contrib" % scalametaVersion ::
      "org.scalatest" %% "scalatest" % "3.0.5" % Test ::
      Nil,
  )
