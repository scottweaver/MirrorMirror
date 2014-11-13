name := "MirrorMirror"

version := "alpha-0.1"

scalaVersion := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  Seq(
    "org.scala-lang"           %    "scala-reflect" % "2.11.2",
    "org.scala-lang"           %    "scala-compiler"   % "2.11.2",
    "org.scalatest"            %    "scalatest_2.11" % "2.2.1" % "test"
  )
}
    