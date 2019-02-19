name := "testing-without-mocking"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-language:higherKinds",
  "-Ypartial-unification",
)

val compilerPlugins = Seq(
  compilerPlugin("io.tryp" % "splain" % "0.3.5" cross CrossVersion.patch),
  compilerPlugin("com.softwaremill.clippy" %% "plugin" % "0.5.3" classifier "bundle"),
  compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8"),
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")
)

val cats = Seq(
  "org.typelevel" %% "cats-core" % "1.5.0",
  "org.typelevel" %% "cats-mtl-core" % "0.4.0",
  "org.typelevel" %% "cats-effect" % "1.2.0",
  "org.typelevel" %% "cats-tagless-macros" % "0.1.0",
  "com.olegpy" %% "meow-mtl" % "0.2.0",
)

val scalatest = Seq(
  "org.scalactic" %% "scalactic" % "3.0.4",
  "org.scalatest" %% "scalatest" % "3.0.5",
  "com.ironcorelabs" %% "cats-scalatest" % "2.3.1"
).map(_ % Test)

libraryDependencies ++= compilerPlugins ++ cats ++ scalatest
