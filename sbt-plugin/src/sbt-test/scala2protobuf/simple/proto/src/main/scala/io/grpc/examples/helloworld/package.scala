package io.grpc.examples

import scala.concurrent.Future

package object helloworld {

  trait Greeter {
    def sayHello(request: HelloRequest): Future[HelloReply]
  }

  case class HelloRequest(name: String)

  case class HelloReply(message: String)

  sealed trait Corpus
  object Corpus {
    object UNIVERSAL extends Corpus
    object WEB extends Corpus
    object IMAGES extends Corpus
    object LOCAL extends Corpus
    object NEWS extends Corpus
    object PRODUCTS extends Corpus
    object VIDEO extends Corpus
  }

}
