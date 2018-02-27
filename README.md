Scala2Protobuf
======================

Generate .proto schema from scala definetions.

This library is provided as SBT plugin, and generate a .proto schema for all case classes and traits of a project that has plugin enabled.

Implementation of serialization to protocol buffer is left to other libraries, please use [ScalaPB](https://github.com/scalapb/ScalaPB) etc.


### Example

- Input
```scala
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

```

- Output

```protobuf
// Generated by scala2protobuf at 2018/...
syntax = "proto3";
option java_multiple_files = false;
option java_package = "io.grpc.examples";
option java_outer_classname = "RouteguideProto";

package routeguide;

service RouteGuide {
  rpc getFeature (Point) returns (Feature) {}
  rpc listFeatures (Rectangle) returns (stream Feature) {}
  rpc recordRoute (stream Point) returns (RouteSummary) {}
  rpc routeChat (stream RouteNote) returns (stream RouteNote) {}
}

message Point {
  int32 latitude = 1;
  int32 longitude = 2;
}
message Rectangle {
  Point lo = 1;
  Point hi = 2;
}
message Feature {
  string name = 1;
  Point location = 2;
}
message FeatureDatabase {
  repeated Feature feature = 1;
}
message RouteNote {
  Point location = 1;
  string message = 2;
}
message RouteSummary {
  int32 point_count = 1;
  int32 feature_count = 2;
  int32 distance = 3;
  int32 elapsed_time = 4;
}
```

### Setup & Usage


```sh
$ sbt publishLocal
```

edit `project/plugins.sbt`

```scala
addSbtPlugin("com.github.bakenezumi" % "scala2protobuf-sbt" % "0.1.0-SNAPSHOT")
```

edit `build.sbt`

```scala
enablePlugins(scala2protobuf.Scala2ProtobufPlugin)
```

generate run 

```sh
$ sbt scala2protobuf
```

### Example build
```sh
$ cd sbt-plugin/src/sbt-test/scala2protobuf/simple
$ sbt

sbt:sbt-test> scala2protobuf
```

see `sbt-plugin/src/sbt-test/scala2protobuf/simple/proto/target/scala-2.12/resource_managed/main/protobuf`


```sh
$ cd sbt-plugin/src/sbt-test/scala2protobuf/simple
$ sbt

sbt:sbt-test> scala2protobuf
```

It can use for [ScalaPB](https://github.com/scalapb/ScalaPB)

```sh
sbt:sbt-test> compile
```

see `sbt-plugin/src/sbt-test/scala2protobuf/simple/proto/target/scala-2.12/src_managed`


License
--------
Apache License, Version 2.0
