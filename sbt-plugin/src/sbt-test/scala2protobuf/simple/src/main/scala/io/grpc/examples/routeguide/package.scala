package io.grpc.examples

import io.grpc.stub.StreamObserver

import scala.concurrent.Future

package object routeguide {

  trait RouteGuide {
    def getFeature(request: Point): Future[Feature]

    def listFeatures(request: Rectangle): StreamObserver[Feature]

    def recordRoute(request: StreamObserver[Point]): Future[RouteSummary]

    def routeChat(request: StreamObserver[RouteNote]): StreamObserver[RouteNote]

  }

  case class Point(latitude: Int, longitude: Int)
  case class Rectangle(lo: Point, hi: Point)
  case class Feature(name: String, location: Point)
  case class FeatureDatabase(feature: Seq[Feature])
  case class RouteNote(location: Point, message: String)
  case class RouteSummary(point_count: Int,
                          feature_count: Int,
                          distance: Int,
                          elapsed_time: Int)

}
