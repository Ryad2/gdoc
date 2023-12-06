lazy val copyTestReportsTask = TaskKey[Unit]("copyTestReportsTask", "Copy test reports files to root target directory")


// defines gdocJVM and gdocJS
lazy val gdoc = crossProject(JSPlatform, JVMPlatform).in(file("."))
  .settings(
    name := "gdoc",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "3.3.1",
    scalacOptions ++= Seq("-Xfatal-warnings", "-deprecation"),
  ).jvmSettings(
    libraryDependencies += "com.lihaoyi" %% "upickle" % "3.1.3",
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies += "org.java-websocket" % "Java-WebSocket" % "1.5.4",
    fork := true,

    copyTestReportsTask := {
      println("[info] Copying test reports to lab root directory…")
      val inDir = baseDirectory.value / "target/test-reports/"
      val outDir = baseDirectory.value / "../target/test-reports/"
      Seq(
        "TEST-cs214.gdoc.server.CRDTServerTest.xml",
        "TEST-cs214.gdoc.common.codec.CodecsTest.xml",
        "TEST-cs214.gdoc.HoursTest.xml",
        "TEST-cs214.gdoc.common.TextCRDTTest.xml"
      ) map { p => (inDir / p, outDir / p) } foreach { f => IO.copyFile(f._1, f._2) }
    },

    Test / testOnly := {
      (Test / testOnly).parsed.doFinally(copyTestReportsTask.taskValue).value
    }
  )
  .jsSettings(
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "3.1.3",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0",
    libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.12.0",
    scalaJSUseMainModuleInitializer := true,
    // Overriding test commands to prevent them from running on the JS project
    Test / test := {
      println("No tests available for the JS project. Please use the gdocJVM/test.")
    },
    Test / testOnly := Def.inputTaskDyn {
      val args: Seq[String] = Def.spaceDelimited().parsed
      println("No tests available for the JS project.      (driverJVM / Test / test Please use the gdocJVM/test.")
      Def.task(())
    }.evaluated,
    Test / testQuick := {
      println("No tests available for the JS project. Please use the gdocJVM/test.")
    }
  )

lazy val gdocJVM = gdoc.jvm
