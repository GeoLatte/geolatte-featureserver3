lazy val commonSettings = Seq(
  organization := "org.geolatte",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.12.8",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-feature",
    "-Ypartial-unification",
    "-Xfatal-warnings"
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector"     % "0.9.6"),
  addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.2.4")
)
lazy val core = (project in file("core"))
  .settings(
    name := "featureserver-core",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel"  %% "cats-effect"    % catsEffectVersion withSources () withJavadoc (),
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    )
  )

lazy val http = (project in file("http"))
  .settings(
    name := "featureserver-http",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel"  %% "cats-effect"         % catsEffectVersion withSources () withJavadoc (),
      "org.http4s"     %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"     %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"     %% "http4s-circe"        % Http4sVersion,
      "org.http4s"     %% "http4s-dsl"          % Http4sVersion withSources () withJavadoc (),
      "io.circe"       %% "circe-generic"       % CirceVersion,
      "org.specs2"     %% "specs2-core"         % Specs2Version % "test" withJavadoc (),
      "org.specs2"     %% "specs2-cats"         % Specs2Version % "test" withJavadoc (),
      "ch.qos.logback" % "logback-classic"      % LogbackVersion
    )
  )

lazy val query = (project in file("query"))
  .settings(
    name := "featureserver-query",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel"  %% "cats-effect"    % catsEffectVersion withSources () withJavadoc (),
      "org.specs2"     %% "specs2-core"    % Specs2Version % "test" withJavadoc (),
      "org.specs2"     %% "specs2-cats"    % Specs2Version % "test" withJavadoc (),
      "org.parboiled"  %% "parboiled"      % parboiledVersion withJavadoc (),
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    )
  ).dependsOn(core)

lazy val root = (project in file("."))
  .settings(
    name := "featureserver3",
    commonSettings
  )
  .aggregate(core, query, http)

val Http4sVersion     = "0.20.3"
val CirceVersion      = "0.11.1"
val Specs2Version     = "4.5.1"
val LogbackVersion    = "1.2.3"
val catsEffectVersion = "1.3.1"
val parboiledVersion  = "2.1.7"
