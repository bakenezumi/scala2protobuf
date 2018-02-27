package scala2protobuf

import java.io.File

import sbt.io._
import scala2protobuf.descriptor.scala.{
  Field,
  Message,
  Method,
  ScalaDescriptor,
  ScalaPackage,
  ScalaType,
  Service
}
import scala2protobuf.descriptor.{ConvertHelper, protobuf}

import scala.meta.inputs.Input
import scala.meta.parsers.Parse
import scala.meta._

object Scala2Protobuf {
  def generate(input: Seq[File],
               dialect: Dialect = dialects.Scala212): Seq[protobuf.File] = {
    input
      .map(file => (IO.read(file, IO.utf8), file.lastModified))
      .map {
        case (source, lastModified) =>
          (Parse.parseSource(Input.String(source), dialect).get, lastModified)
      }
      .flatMap {
        case (source, lastModified) =>
          collectScalaDescriptor(ScalaPackage(""), source.stats, lastModified)
      }
      .groupBy(_.pkg)
      .map { t =>
        toProtobufDescriptor(t._1, t._2.map(_.lastModified).max, t._2)
      }
      .toSeq
  }

  def collectScalaDescriptor(scalaPackage: ScalaPackage,
                             stats: Seq[Stat],
                             lastModified: Long): Seq[ScalaDescriptor] = {
    stats.collect {
      case Pkg(pkg, pkgStats) =>
        collectScalaDescriptor(ScalaPackage(pkg.syntax.trim), pkgStats.collect {
          case s: Stat => s
        }, lastModified)
      case obj: Pkg.Object =>
        val basePackage =
          if (scalaPackage.name.isEmpty) "" else scalaPackage.name + "."
        collectScalaDescriptor(ScalaPackage(basePackage + obj.name.value),
                               obj.templ.children.collect {
                                 case s: Stat => s
                               },
                               lastModified)
      case clazz: Defn.Class if isCaseClass(clazz) =>
        Seq(toMessage(scalaPackage, clazz, lastModified))
      case trt: Defn.Trait => Seq(toService(scalaPackage, trt, lastModified))
    }.flatten
  }

  def isCaseClass(clazz: Defn.Class): Boolean =
    clazz.mods.exists {
      case _: Mod.Case => true
    }

  def toMessage(scalaPackage: ScalaPackage,
                clazz: Defn.Class,
                lastModified: Long): Message = {
    Message(scalaPackage,
            lastModified,
            clazz.name.value,
            clazz.ctor.paramss.head.map(toField))
  }

  def toField(param: Term.Param): Field = {
    Types.of(param.decltpe.get) match {
      case Types.Single(t) =>
        Field(isOptional = false,
              isRepeated = false,
              tpe = t,
              name = param.name.syntax)
      case Types.Option(Types.Single(t)) =>
        Field(isOptional = true,
              isRepeated = false,
              tpe = t,
              name = param.name.value)
      case Types.Seq(Types.Single(t)) =>
        Field(isOptional = false,
              isRepeated = true,
              tpe = t,
              name = param.name.syntax)
      case _ =>
        throw new RuntimeException(
          s"${param.decltpe.get} can not be used for the field type of Message")
    }
  }

  def toService(scalaPackage: ScalaPackage,
                trt: Defn.Trait,
                lastModified: Long): Service = {
    Service(scalaPackage,
            lastModified,
            trt.name.value,
            trt.templ.stats.collect {
              case method: Decl.Def => toMethod(method)
            })
  }

  def toMethod(method: Decl.Def): Method = {
    val inputParam = method.paramss.flatten.headOption.getOrElse(
      throw new RuntimeException(s"Input parameter is missing")
    )
    if (method.paramss.flatten.size > 1) {
      throw new RuntimeException(s"Must be only one parameter")
    }
    (Types.of(inputParam.decltpe.get), Types.of(method.decltpe)) match {
      case (Types.Single(in), Types.Future(Types.Single(out))) =>
        Method(name = method.name.syntax,
               isStreamInput = false,
               inputType = in,
               isStreamOutput = false,
               outputType = out)
      case (Types.StreamObserver(Types.Single(in)),
            Types.Future(Types.Single(out))) =>
        Method(name = method.name.syntax,
               isStreamInput = true,
               inputType = in,
               isStreamOutput = false,
               outputType = out)
      case (Types.Single(in), Types.StreamObserver(Types.Single(out))) =>
        Method(name = method.name.syntax,
               isStreamInput = false,
               inputType = in,
               isStreamOutput = true,
               outputType = out)
      case (Types.StreamObserver(Types.Single(in)),
            Types.StreamObserver(Types.Single(out))) =>
        Method(name = method.name.syntax,
               isStreamInput = true,
               inputType = in,
               isStreamOutput = true,
               outputType = out)
      case _ =>
        throw new RuntimeException(
          s"${inputParam.decltpe.get} => ${method.decltpe} can not be used for service type ")
    }
  }

  def toProtobufDescriptor(
      pkg: ScalaPackage,
      lastModified: Long,
      scalaDescriptors: Seq[ScalaDescriptor]): protobuf.File = {

    val messages = scalaDescriptors.collect {
      case Message(_, _, messageName, fields: Seq[Field]) =>
        protobuf.Message(
          messageName,
          fields.zipWithIndex.map {
            case (Field(isOptional, isRepeated, tpe: ScalaType, name), index) =>
              protobuf.Field(isOptional,
                             isRepeated,
                             tpe.protobufType,
                             name,
                             index + 1)
          }
        )
    }

    val services = scalaDescriptors.collect {
      case Service(_, _, serviceName, methods: Seq[Method]) =>
        protobuf.Service(
          serviceName,
          methods.map {
            case Method(name,
                        isStreamInput,
                        inputType: ScalaType,
                        isStreamOutput,
                        outputType: ScalaType) =>
              protobuf.Method(name,
                              isStreamInput,
                              inputType.protobufType,
                              isStreamOutput,
                              outputType.protobufType)
          }
        )
    }

    protobuf.File(
      ConvertHelper.defaultFileNameConverter(pkg.name),
      protobuf.Syntax.PROTO3,
      ConvertHelper.defaultPackageConverter(pkg.name),
      ConvertHelper.defaultFileOptionConverter(pkg.name),
      messages,
      services,
      lastModified
    )
  }

}
