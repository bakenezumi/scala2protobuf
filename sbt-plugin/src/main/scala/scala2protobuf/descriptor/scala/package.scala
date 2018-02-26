package scala2protobuf.descriptor

import scala2protobuf.descriptor.protobuf.Type

package object scala {

  case class ScalaPackage(name: String)

  sealed trait ScalaDescriptor {
    val pkg: ScalaPackage
  }
  case class Message(override val pkg: ScalaPackage,
                     name: String,
                     Fields: Seq[Field])
      extends ScalaDescriptor

  case class Service(override val pkg: ScalaPackage,
                     name: String,
                     methods: Seq[Method])
      extends ScalaDescriptor

  sealed abstract class ScalaType(val protobufType: Type)

  object ScalaType {
    case object INT extends ScalaType(Type.INT32)
    case object LONG extends ScalaType(Type.INT64)
    case object FLOAT extends ScalaType(Type.FLOAT)
    case object DOUBLE extends ScalaType(Type.DOUBLE)
    case object BOOLEAN extends ScalaType(Type.BOOL)
    case object STRING extends ScalaType(Type.STRING)
    case object BYTE_STRING extends ScalaType(Type.BYTES)
    case object TIMESTAMP extends ScalaType(Type.TIMESTAMP)
    case class ENUM_OR_MESSAGE(name: String)
        extends ScalaType(Type.ENUM_OR_MESSAGE(name))
  }

  case class Field(isOptional: Boolean,
                   isRepeated: Boolean,
                   tpe: ScalaType,
                   name: String)

  case class Enum(name: String, values: Seq[String])

  case class Method(name: String,
                    isStreamInput: Boolean,
                    inputType: ScalaType,
                    isStreamOutput: Boolean,
                    outputType: ScalaType)

}
