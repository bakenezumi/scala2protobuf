val unusedWarnings =
  "-Ywarn-unused" ::
    "-Ywarn-unused-import" ::
    Nil

val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT"
)

lazy val root = (project in file(".")).settings(
  name := "sbt-test",
  commonSettings,
) dependsOn proto aggregate proto


import scala2protobuf._
import scalapb.compiler.Version.{grpcJavaVersion, protobufVersion, scalapbVersion}
import sbt._

lazy val proto = (project in file("proto")).enablePlugins(Scala2ProtobufPlugin).settings(
  name := "sbt-test-proto",
  commonSettings,
  libraryDependencies ++=
    "io.grpc" % "grpc-stub" % grpcJavaVersion ::
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion ::
      "io.grpc" % "grpc-netty" % grpcJavaVersion ::
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf" ::
      Nil,
  PB.protocVersion := "-v351",
  PB.protoSources in Compile += SCALA2PB.target.value,
  PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value),
  scalacOptions ++= unusedWarnings,
  Seq(Compile, Test).flatMap(c =>
    scalacOptions in (c, console) --= unusedWarnings
  )

)




