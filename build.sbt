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

val Versions = new {
  val akka          = "2.5.19"
  val akkaHttp      = "10.1.0"
  val circe         = "0.9.1"
  val eff           = "5.3.0"
  val cats          = "1.1.0"
  val akkaHttpCirce = "1.20.0"
  val pureConfig    = "0.7.2"
  val quicklens     = "1.4.11"
  val grafter       = "2.6.0"
  val logback       = "1.2.3"
  val logging       = "3.7.2"
  val kindProjector = "0.9.4"
  val paradise      = "2.1.1"
  val scalamock     = "4.4.0"
  val scalatest     = "3.0.6"
}

libraryDependencies ++= Seq(
  "com.github.pureconfig"          %% "pureconfig"           % Versions.pureConfig,
  "com.softwaremill.quicklens"     %% "quicklens"            % Versions.quicklens,
  "com.typesafe.akka"              %% "akka-actor"           % Versions.akka,
  "com.typesafe.akka"              %% "akka-stream"          % Versions.akka,
  "com.typesafe.akka"              %% "akka-actor-typed"     % Versions.akka,
  "com.typesafe.akka"              %% "akka-http"            % Versions.akkaHttp,
  "de.heikoseeberger"              %% "akka-http-circe"      % Versions.akkaHttpCirce,
  "io.circe"                       %% "circe-core"           % Versions.circe,
  "io.circe"                       %% "circe-generic"        % Versions.circe,
  "io.circe"                       %% "circe-generic-extras" % Versions.circe,
  "io.circe"                       %% "circe-java8"          % Versions.circe,
  "io.circe"                       %% "circe-jawn"           % Versions.circe,
  "io.circe"                       %% "circe-parser"         % Versions.circe,
  "org.atnos"                      %% "eff"                  % Versions.eff,
  "org.atnos"                      %% "eff-monix"            % Versions.eff,
  "org.typelevel"                  %% "cats-core"            % Versions.cats,
  "org.zalando"                    %% "grafter"              % Versions.grafter,
  "ch.qos.logback"                 %  "logback-classic"      % Versions.logback,
  "com.typesafe.scala-logging"     %% "scala-logging"        % Versions.logging,
  "org.scalamock" %% "scalamock" % "4.4.0" % Test,
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  compilerPlugin("org.spire-math"  %% "kind-projector"       % Versions.kindProjector),
  compilerPlugin("org.scalamacros" %% "paradise"             % Versions.paradise cross CrossVersion.full)
)
