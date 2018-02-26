package io.grpc.examples

import scala.concurrent.Future

package object helloworld {

  trait Greeter {
    def sayHello(request: HelloRequest): Future[HelloReply]
  }

  case class HelloRequest(name: String)

  case class HelloReply(message: String)

}
