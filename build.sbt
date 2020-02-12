val commonResolvers = Seq(
  "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  )


lazy val commonSettings = Seq(
  organization := "org.geolatte",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.13.1",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-feature",
    "-Xfatal-warnings"
  ),
  resolvers ++= commonResolvers,
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.3.1")
)
lazy val core = (project in file("core"))
  .settings(
    name := "featureserver-core",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel"  %% "cats-effect"         % catsEffectVersion withSources () withJavadoc (),
      "org.geolatte"   %% "geolatte-geom-scala" % geomVersion withJavadoc () withSources (),
      "co.fs2"         %% "fs2-core"            % fs2Version withJavadoc (),
      "io.circe"       %% "circe-core"          % CirceVersion,
      "ch.qos.logback" % "logback-classic"      % LogbackVersion
    )
  )
  .disablePlugins(RevolverPlugin)

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
  .dependsOn(core)
  .disablePlugins(RevolverPlugin)

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
  )
  .dependsOn(core)
  .disablePlugins(RevolverPlugin)

lazy val postgres = (project in file("postgres"))
  .settings(
    name := "featureserver-postgres",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect"  % catsEffectVersion withSources () withJavadoc (),
      "org.specs2"    %% "specs2-core"  % Specs2Version % "test" withJavadoc (),
      "org.specs2"    %% "specs2-cats"  % Specs2Version % "test" withJavadoc (),
      "io.circe"      %% "circe-parser" % CirceVersion,
      "org.tpolecat" %% "doobie-core"     % doobieVersion withJavadoc () withSources,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion withJavadoc () withSources,
      "org.tpolecat"   %% "doobie-hikari"  % doobieVersion,
      "org.tpolecat"   %% "doobie-specs2"  % doobieVersion % "test",
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    )
  )
  .dependsOn(core, query % "test->compile")
  .disablePlugins(RevolverPlugin)

lazy val root = (project in file("."))
  .settings(
    name := "featureserver3",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect"         % catsEffectVersion withSources () withJavadoc (),
      "io.circe"      %% "circe-config"        % CirceConfigVersion,
      "org.http4s"    %% "http4s-blaze-server" % Http4sVersion,
      "org.specs2"    %% "specs2-core"         % Specs2Version % "test" withJavadoc (),
      "org.specs2"     %% "specs2-cats"         % Specs2Version % "test" withJavadoc (),
      "io.circe"      %% "circe-literal"        % CirceVersion % "test" withJavadoc ()
      )
  )
  .aggregate(core, query, http, postgres)
  .dependsOn(core, query, http, postgres)

val Http4sVersion      = "0.21.0"
val CirceVersion       = "0.12.2"
val Specs2Version      = "4.8.2"
val LogbackVersion     = "1.2.3"
val catsEffectVersion  = "2.1.1"
val parboiledVersion   = "2.1.7"
val geomVersion        = "1.5.0-SNAPSHOT"
val fs2Version         = "2.2.1"
val doobieVersion      = "0.8.8"
val CirceConfigVersion = "0.7.0"

//TODO -- use docker-compose to run integration tests such as in
// https://github.com/lloydmeta/http4s-doobie-docker-scratchpad/blob/master/build.sbt
