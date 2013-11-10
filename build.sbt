name := "trivia-tools"

organization := "dk.reportsoft"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.0"

publishTo  := Some("ReportSoft Nexus" at "http://birt-development.dk:8081/nexus/content/repositories/snapshots")

credentials += Credentials(Path.userHome / ".m2" / "reportsoft.credentials")

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5"

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

//retrieveManaged := true