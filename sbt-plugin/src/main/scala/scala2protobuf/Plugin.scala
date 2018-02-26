package scala2protobuf

import java.io.File

import sbt._
import sbt.Keys._
import sbt.internal.util.ManagedLogger

object Plugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    object SCALA2PB {
      val generate =
        TaskKey[Unit]("scala2protobuf",
                      "Generate .proto from Scala source files")
      val sources =
        TaskKey[Seq[File]]("scala2protobuf-sources",
                           "Scala Sources of protobuf schema")
      val target =
        SettingKey[File]("scala2protobuf-target",
                         "directory to generate .proto")
    }
  }

  import autoImport.SCALA2PB._

  def generateTask = Def.task {
    val log: ManagedLogger = streams.value.log
    GeneratorRunner(sources.value, target.value, log)
  }

  import autoImport.SCALA2PB

  def scala2protobufSettings = Seq(
    SCALA2PB.generate := generateTask.value,
    SCALA2PB.sources := (unmanagedSources in Compile).value,
    SCALA2PB.target := (resourceManaged in Compile).value / "protobuf"
  )

  override def projectSettings: Seq[Def.Setting[_]] =
    scala2protobufSettings
}

object GeneratorRunner {
  def apply(input: Seq[File],
            targetDirectory: File,
            log: ManagedLogger): Seq[File] = {
    val protobufDescriptors = Scala2Protobuf.generate(input)
    val dir = targetDirectory
    protobufDescriptors.map { f =>
      val file = dir / f.filename
      val content = f.toProto
      IO.write(file, content.getBytes(java.nio.charset.StandardCharsets.UTF_8))
      log.info(s"$file is generated")
      file
    }
  }
}
