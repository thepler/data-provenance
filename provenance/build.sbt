organization := "com.cibo"
name         := "provenance"
licenses     += ("BSD Simplified", url("https://opensource.org/licenses/BSD-3-Clause"))

crossScalaVersions := Seq("2.12.4", "2.11.11")
scalaVersion := crossScalaVersions.value.head

lazy val provenance = project.in(file(".")).configs(IntegrationTest)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xfatal-warnings")


import de.heikoseeberger.sbtheader.HeaderPattern
headers := Map(
  "scala" -> (
    HeaderPattern.cStyleBlockComment,
    """/* Copyright (c) 2016, 2017 CiBO Technologies - All Rights Reserved
      | * You may use, distribute, and modify this code under the
      | * terms of the BSD 3-Clause license.
      | *
      | * A copy of the license can be found on the root of this repository,
      | * at https://github.com/cibotech/provenance/LICENSE.md,
      | * or at https://opensource.org/licenses/BSD-3-Clause
      | */
      |
      |""".stripMargin
    )
)


// Test config
fork in Test := true
testOptions in Test ++= Seq(Tests.Argument("-oDF"), Tests.Argument("-h", "target/unit-test-reports"))


// Integration test config
Defaults.itSettings
fork in IntegrationTest := true
testOptions in IntegrationTest ++= Seq(Tests.Argument("-oDF"))

scalaVersion in ThisBuild := "2.12.4"