package scala2protobuf.descriptor

import scala2protobuf.descriptor.protobuf.Type

package object scala {

  case class ScalaFile(name: String,
                       contents: String,
                       lastModified: Long,
                       path: String)

  case class ScalaPackage(name: String)

  sealed trait ScalaDescriptor {
    val pkg: ScalaPackage
    val file: ScalaFile
    val name: String
  }
  case class Message(override val pkg: ScalaPackage,
                     override val file: ScalaFile,
                     override val name: String,
                     Fields: Seq[Field])
      extends ScalaDescriptor

  case class Enum(override val pkg: ScalaPackage,
                  override val file: ScalaFile,
                  override val name: String,
                  values: Seq[String])
      extends ScalaDescriptor

  case class Service(override val pkg: ScalaPackage,
                     override val file: ScalaFile,
                     override val name: String,
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

  case class Method(name: String,
                    isStreamInput: Boolean,
                    inputType: ScalaType,
                    isStreamOutput: Boolean,
                    outputType: ScalaType)

}
