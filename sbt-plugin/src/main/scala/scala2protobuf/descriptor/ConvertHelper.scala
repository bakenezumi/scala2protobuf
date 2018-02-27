package scala2protobuf.descriptor

import scala2protobuf.descriptor.protobuf.FileOptions
import scala2protobuf.descriptor.protobuf.Package

object ConvertHelper {

  def lastPackageName(packageName: String): String =
    packageName.split("\\.").last

  def initPackageName(packageName: String): String =
    packageName.split("\\.").init.mkString(".")

  val defaultFileOptionConverter: String => FileOptions =
    (scalaPackageName: String) =>
      FileOptions(
        javaPackage = initPackageName(scalaPackageName),
        javaOuterClassName = lastPackageName(scalaPackageName).capitalize + "Proto"
    )

  val defaultPackageConverter: String => Package =
    (scalaPackageName: String) => Package(lastPackageName(scalaPackageName))

  val defaultFileNameConverter: String => String =
    (scalaPackageName: String) => lastPackageName(scalaPackageName) + ".proto"

}
