package scala2protobuf

import scala.meta._

sealed trait Types

object Types {
  case class Seq(typeDesc: Types) extends Types
  case class Option(typeDesc: Types) extends Types
  case class Single(typeDesc: descriptor.scala.ScalaType) extends Types
  case class Future(typeDesc: Types) extends Types
  case class StreamObserver(typeDesc: Types) extends Types

  def of(tpe: Type): Types = {
    //noinspection ScalaUnusedSymbol
    tpe match {
      case t"Seq[$elementTpe]" => Types.Seq(of(elementTpe))
      case t"Option[$elementTpe]" =>
        Types.Option(of(elementTpe))
      case t"Future[$elementTpe]" =>
        Types.Future(of(elementTpe))
      case t"StreamObserver[$elementTpe]" =>
        Types.StreamObserver(of(elementTpe))
      case t"BigDecimal" =>
        Types.Single(descriptor.scala.ScalaType.BYTE_STRING)
      case t"BigInt" =>
        Types.Single(descriptor.scala.ScalaType.BYTE_STRING)
      case t"Int" | t"Integer" =>
        Types.Single(descriptor.scala.ScalaType.INT)
      case t"Array[Byte]" =>
        Types.Single(
          descriptor.scala.ScalaType.BYTE_STRING
        )
      case t"Long" =>
        Types.Single(
          descriptor.scala.ScalaType.LONG
        )
      case t"Double" =>
        Types.Single(
          descriptor.scala.ScalaType.DOUBLE
        )
      case t"Boolean" =>
        Types.Single(
          descriptor.scala.ScalaType.BOOLEAN
        )
      case t"Float" =>
        Types.Single(
          descriptor.scala.ScalaType.FLOAT
        )
      case t"String" =>
        Types.Single(
          descriptor.scala.ScalaType.STRING
        )
      case t"java.math.BigDecimal" =>
        Types.Single(
          descriptor.scala.ScalaType.STRING
        )
      case t"BigInteger" | t"java.math.BigInteger" =>
        Types.Single(
          descriptor.scala.ScalaType.STRING
        )
      case t"TimeStamp" | t"com.google.protobuf.Timestamp" =>
        Types.Single(
          descriptor.scala.ScalaType
            .ENUM_OR_MESSAGE("google.protobuf.Timestamp")
        )
      case _ =>
        Types.Single(descriptor.scala.ScalaType.ENUM_OR_MESSAGE(tpe.syntax))
    }
  }

}
