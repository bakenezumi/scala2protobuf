package scala2protobuf

import org.scalatest.FunSuite
import descriptor.scala.{Field, ScalaPackage, Method, Service}
import scala.meta._

import scala.meta.parsers.Parse

class Scala2ProtobufTestSuite extends FunSuite {
  val dialect: Dialect = dialects.Scala212

  test("case class to Message") {
    val sourceString = """
package io.grpc.examples.helloworld
case class HelloRequest(name: String)
case class HelloReply(message: String)
class NotCaseClass {}
    """
    val source = Parse.parseSource(Input.String(sourceString), dialect).get
    val ret =
      Scala2Protobuf.collectScalaDescriptor(ScalaPackage(""), source.stats)
    assert(
      ret == Seq(
        descriptor.scala.Message(ScalaPackage("io.grpc.examples.helloworld"),
                                 "HelloRequest",
                                 Seq(
                                   Field(isOptional = false,
                                         isRepeated = false,
                                         descriptor.scala.ScalaType.STRING,
                                         "name"))),
        descriptor.scala.Message(ScalaPackage("io.grpc.examples.helloworld"),
                                 "HelloReply",
                                 Seq(
                                   Field(isOptional = false,
                                         isRepeated = false,
                                         descriptor.scala.ScalaType.STRING,
                                         "message")))
      ))
  }

  test("empty target") {
    val sourceString = """
package io.grpc.examples.helloworld
class NotCaseClass {}
case object TopLevelObject
    """
    val source = Parse.parseSource(Input.String(sourceString), dialect).get
    val ret =
      Scala2Protobuf.collectScalaDescriptor(ScalaPackage(""), source.stats)
    assert(ret == Nil)
  }

  test("trait to Service") {
    val sourceString =
      """
package io.grpc.examples.helloworld
trait Greeter {
  def sayHello(request: HelloRequest): Future[HelloReply]
  def sayHelloStream(request: StreamObserver[HelloRequest]): StreamObserver[HelloReply]
}
    """
    val source = Parse.parseSource(Input.String(sourceString), dialect).get
    val ret =
      Scala2Protobuf.collectScalaDescriptor(ScalaPackage(""), source.stats)
    assert(
      ret == Seq(Service(
        ScalaPackage("io.grpc.examples.helloworld"),
        "Greeter",
        Seq(
          Method(
            "sayHello",
            isStreamInput = false,
            inputType =
              descriptor.scala.ScalaType.ENUM_OR_MESSAGE("HelloRequest"),
            isStreamOutput = false,
            outputType =
              descriptor.scala.ScalaType.ENUM_OR_MESSAGE("HelloReply")
          ),
          Method(
            "sayHelloStream",
            isStreamInput = true,
            inputType =
              descriptor.scala.ScalaType.ENUM_OR_MESSAGE("HelloRequest"),
            isStreamOutput = true,
            outputType =
              descriptor.scala.ScalaType.ENUM_OR_MESSAGE("HelloReply")
          )
        )
      )))

  }

  test("package object to ScalaDescriptors") {
    val sourceString = """
package io.grpc.examples

import scala.concurrent.Future

package object helloworld {

  trait Greeter {
    def sayHello(request: HelloRequest): Future[HelloReply]
  }

  case class HelloRequest(name: String)

  case class HelloReply(message: String)

  class Omit {
    def dummy(): Unit = ()
  }

  object OmitObject {
    def dummy(): Unit = ()
  }

}
    """
    val source = Parse.parseSource(Input.String(sourceString), dialect).get
    val ret =
      Scala2Protobuf.collectScalaDescriptor(ScalaPackage(""), source.stats)
    assert(
      ret == Seq(
        Service(
          ScalaPackage("io.grpc.examples.helloworld"),
          "Greeter",
          Seq(
            Method(
              "sayHello",
              isStreamInput = false,
              inputType =
                descriptor.scala.ScalaType.ENUM_OR_MESSAGE("HelloRequest"),
              isStreamOutput = false,
              outputType =
                descriptor.scala.ScalaType.ENUM_OR_MESSAGE("HelloReply")
            )
          )
        ),
        descriptor.scala.Message(ScalaPackage("io.grpc.examples.helloworld"),
                                 "HelloRequest",
                                 Seq(
                                   Field(isOptional = false,
                                         isRepeated = false,
                                         descriptor.scala.ScalaType.STRING,
                                         "name"))),
        descriptor.scala.Message(ScalaPackage("io.grpc.examples.helloworld"),
                                 "HelloReply",
                                 Seq(
                                   Field(isOptional = false,
                                         isRepeated = false,
                                         descriptor.scala.ScalaType.STRING,
                                         "message")))
      ))
  }

}
