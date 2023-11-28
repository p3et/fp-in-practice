ThisBuild / scalaVersion := "2.13.11"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.jambit"
ThisBuild / organizationName := "jambit"

assembly / mainClass := Some("com.jambit.fpinpractice.MainApp")
scalacOptions ++= Seq(          
  "-encoding", "utf8",          
  "-feature",                   
  "-language:implicitConversions",
  "-language:existentials",
  "-unchecked",
  "-Werror",
  "-Xlint",
  "-deprecation",
  "-Xfatal-warnings"
)

lazy val root = (project in file("."))
  .settings(
    name := "fpinpractice",
    libraryDependencies ++= Seq(
      "com.beachape" %% "enumeratum" % "1.7.2",
      "dev.zio" %% "zio" % "2.0.16",
      "dev.zio" %% "zio-json" % "0.6.1",
      "dev.zio" %% "zio-http" % "3.0.0-RC2",
      "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M4",
      "dev.zio" %% "zio-test" % "2.0.16" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.16" % Test,
      "dev.zio" %% "zio-test-magnolia" % "2.0.16" % Test,
      "net.ruippeixotog" %% "scala-scraper" % "3.1.0",
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
