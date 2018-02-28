package scala2protobuf

import java.time.{Clock, Instant, ZoneId}

import org.scalatest.FunSuite
import descriptor.scala.{Field, Method, ScalaFile, ScalaPackage, Service}

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
      Scala2Protobuf().collectScalaDescriptor(ScalaPackage(""), source.stats, 0)
    assert(
      ret == Seq(
        descriptor.scala.Message(ScalaPackage("io.grpc.examples.helloworld"),
                                 0,
                                 "HelloRequest",
                                 Seq(
                                   Field(isOptional = false,
                                         isRepeated = false,
                                         descriptor.scala.ScalaType.STRING,
                                         "name"))),
        descriptor.scala.Message(ScalaPackage("io.grpc.examples.helloworld"),
                                 0,
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
      Scala2Protobuf().collectScalaDescriptor(ScalaPackage(""), source.stats, 0)
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
      Scala2Protobuf().collectScalaDescriptor(ScalaPackage(""), source.stats, 0)
    assert(
      ret == Seq(Service(
        ScalaPackage("io.grpc.examples.helloworld"),
        0,
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
      Scala2Protobuf().collectScalaDescriptor(ScalaPackage(""), source.stats, 0)
    assert(
      ret == Seq(
        Service(
          ScalaPackage("io.grpc.examples.helloworld"),
          0,
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
                                 0,
                                 "HelloRequest",
                                 Seq(
                                   Field(isOptional = false,
                                         isRepeated = false,
                                         descriptor.scala.ScalaType.STRING,
                                         "name"))),
        descriptor.scala.Message(ScalaPackage("io.grpc.examples.helloworld"),
                                 0,
                                 "HelloReply",
                                 Seq(
                                   Field(isOptional = false,
                                         isRepeated = false,
                                         descriptor.scala.ScalaType.STRING,
                                         "message")))
      ))
  }

  test("scala to protobuf schema") {
    val files = Seq(
      ScalaFile(
        "Greeter.scala",
        """
package io.grpc.examples.helloworld

import scala.concurrent.Future

trait Greeter {
  def sayHello(request: HelloRequest): Future[HelloReply]
}
      """,
        0
      ),
      ScalaFile(
        "HelloRequest.scala",
        """
package io.grpc.examples.helloworld

case class HelloRequest(name: String)
      """,
        0
      ),
      ScalaFile(
        "HelloReply.scala",
        """
package io.grpc.examples.helloworld

case class HelloReply(message: String)
      """,
        0
      )
    )

    val clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneId.of("UTC"))

    assert(
      Scala2Protobuf()
        .generateInternal(files.par)
        .map(_.toProto(clock))
        .seq == Seq("""// Generated by scala2protobuf at 1970-01-01T00:00Z[UTC]
syntax = "proto3";
option java_multiple_files = false;
option java_package = "io.grpc.examples";
option java_outer_classname = "HelloworldProto";

package helloworld;
service Greeter {
  rpc sayHello (HelloRequest) returns (HelloReply) {}
}
message HelloRequest {
  string name = 1;
}
message HelloReply {
  string message = 1;
}
"""))
  }

  test("import timestamp") {
    val files = Seq(
      ScalaFile(
        "Greeter.scala",
        """
package io.grpc.examples.helloworld

import com.google.protobuf.timestamp.Timestamp

case class HelloReply(message: String, time: Timestamp)
      """,
        0
      )
    )

    val clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneId.of("UTC"))

    assert(
      Scala2Protobuf()
        .generateInternal(files.par)
        .map(_.toProto(clock))
        .seq == Seq("""// Generated by scala2protobuf at 1970-01-01T00:00Z[UTC]
syntax = "proto3";
option java_multiple_files = false;
option java_package = "io.grpc.examples";
option java_outer_classname = "HelloworldProto";
import "google/protobuf/timestamp.proto";
package helloworld;

message HelloReply {
  string message = 1;
  google.protobuf.Timestamp time = 2;
}
"""))
  }

}
