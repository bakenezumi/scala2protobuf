package scala2protobuf.descriptor

import org.scalatest.FunSuite
import scala2protobuf.descriptor.protobuf.{FileOptions, Package}

class ConvertHelperTestSuite extends FunSuite {
  test("convert to FileOption from scala package") {
    val scalaPackageName = "foo.bar.baz"
    val fileOption =
      FileOptions.generate(ConvertHelper.defaultFileOptionConverter,
                           scalaPackageName)

    assert(fileOption.javaPackage == "foo.bar")
    assert(fileOption.javaOuterClassName == "BazProto")
  }

  test("convert to protobuf package from scala package") {
    val scalaPackageName = "foo.bar.baz"
    val pkg =
      Package.generate(ConvertHelper.defaultPackageConverter, scalaPackageName)

    assert(pkg == Package("baz"))
  }

}
