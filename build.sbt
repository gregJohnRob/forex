name := "forex"
version := "1.0.0"

scalaVersion := "2.12.4"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-Ypartial-unification",
  "-language:experimental.macros",
  "-language:implicitConversions"
)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

val akkaVersion = "2.5.19"
val circeVersion = "0.9.1"
val effVersion = "5.3.0"
val catsVersion = "1.1.0"

libraryDependencies ++= Seq(
  "com.github.pureconfig"          %% "pureconfig"           % "0.7.2",
  "com.softwaremill.quicklens"     %% "quicklens"            % "1.4.11",
  "com.typesafe.akka"              %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka"              %% "akka-stream"          % akkaVersion,
  "com.typesafe.akka"              %% "akka-actor-typed"     % akkaVersion,
  "com.typesafe.akka"              %% "akka-http"            % "10.1.0",
  "de.heikoseeberger"              %% "akka-http-circe"      % "1.20.0",
  "io.circe"                       %% "circe-core"           % circeVersion,
  "io.circe"                       %% "circe-generic"        % circeVersion,
  "io.circe"                       %% "circe-generic-extras" % circeVersion,
  "io.circe"                       %% "circe-java8"          % circeVersion,
  "io.circe"                       %% "circe-jawn"           % circeVersion,
  "org.atnos"                      %% "eff"                  % effVersion,
  "org.atnos"                      %% "eff-monix"            % effVersion,
  "org.typelevel"                  %% "cats-core"            % catsVersion,
  "org.zalando"                    %% "grafter"              % "2.6.0",
  "ch.qos.logback"                 %  "logback-classic"      % "1.2.3",
  "com.typesafe.scala-logging"     %% "scala-logging"        % "3.7.2",
  compilerPlugin("org.spire-math"  %% "kind-projector"       % "0.9.4"),
  compilerPlugin("org.scalamacros" %% "paradise"             % "2.1.1" cross CrossVersion.full)
)
