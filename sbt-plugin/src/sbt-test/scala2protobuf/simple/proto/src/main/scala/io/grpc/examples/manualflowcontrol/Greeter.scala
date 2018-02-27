package io.grpc.examples.manualflowcontrol

import io.grpc.stub.StreamObserver

trait Greeter {
  def sayHello(
      request: StreamObserver[HelloRequest]): StreamObserver[HelloReply]
}
