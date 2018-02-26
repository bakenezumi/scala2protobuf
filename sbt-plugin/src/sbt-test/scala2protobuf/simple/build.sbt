import scalapb.compiler.Version.{grpcJavaVersion, protobufVersion, scalapbVersion}
import sbt._

name := "sbt-test"
version := "0.1.0-SNAPSHOT"

enablePlugins(scala2protobuf.Plugin)

libraryDependencies ++=
  "io.grpc" % "grpc-stub" % grpcJavaVersion ::
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion ::
  "io.grpc" % "grpc-netty" % grpcJavaVersion ::
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf" ::
  Nil

PB.protocVersion := "-v351"

PB.protoSources in Compile += SCALA2PB.target.value

PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value)

val unusedWarnings =
  "-Ywarn-unused" ::
    "-Ywarn-unused-import" ::
    Nil

scalacOptions ++= unusedWarnings

Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) --= unusedWarnings
)
