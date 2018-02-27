package scala2protobuf

import java.io.File

import sbt._
import sbt.Keys._
import sbt.internal.util.ManagedLogger

object Scala2ProtobufPlugin extends AutoPlugin {

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
                         "Directory to generate .proto")
      val replacementSources =
        TaskKey[Seq[File]]("scala2protobuf-replacement-sources",
                           "Exclude original source from classpath")
    }
  }

  import autoImport.SCALA2PB

  def generateTask = Def.task {
    val log: ManagedLogger = streams.value.log
    GeneratorRunner(SCALA2PB.sources.value, SCALA2PB.target.value, log)

  }

  def scala2protobufSettings = Seq(
    SCALA2PB.generate := generateTask.value,
    SCALA2PB.sources := (unmanagedSources in Compile).value,
    SCALA2PB.target := (resourceManaged in Compile).value / "protobuf",
    SCALA2PB.replacementSources := (unmanagedSources in Compile).value
  )

  override def projectSettings: Seq[Def.Setting[_]] =
    scala2protobufSettings ++
      Seq(
        (sources in Compile) :=
          ((sources in Compile).value.toSet[File] --
            SCALA2PB.replacementSources.value.toSet[File]).toSeq,
        (compile in Compile) := (compile in Compile)
          .dependsOn(SCALA2PB.generate)
          .value
      )
}

object GeneratorRunner {
  def apply(input: Seq[File],
            targetDirectory: File,
            log: ManagedLogger): Seq[File] = {
    val protobufDescriptors = Scala2Protobuf.generate(input)
    val dir = targetDirectory
    protobufDescriptors.map { f =>
      val file = dir / f.filename
      if (!file.exists || file.lastModified < f.lastModified) {
        val content = f.toProto
        log.info(s"Generating protobuf file to $file")
        IO.write(file,
                 content.getBytes(java.nio.charset.StandardCharsets.UTF_8))
      }
      file
    }
  }
}
