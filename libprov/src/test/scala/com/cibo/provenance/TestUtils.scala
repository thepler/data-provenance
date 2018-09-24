package com.cibo.provenance

import com.cibo.io.s3.SyncablePath
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FileUtils
import org.scalatest.Matchers
import com.cibo.aws.AWSClient.Implicits.s3SyncClient
import com.cibo.io.s3.SyncablePathBaseDir.Implicits.default

object TestUtils extends LazyLogging with Matchers {
  import java.io.File
  import java.nio.file.{Files, Paths}

  import com.cibo.io.Shell.getOutputAsBytes

  // The this is the BuildInfo _object_ for this library.
  // Not to be confused with the BuildInfo base trait use for all apps with provenance tracking.
  val libBuildInfo: BuildInfo = com.cibo.provenance.internal.BuildInfo

  // Use the scala version for the library in this test, cross-compiled tests can run in parallel.
  val subdir =
    sys.env.getOrElse("USER", "anonymous-" + com.cibo.provenance.internal.BuildInfo.buildId.toString) +
      f"/data-provenance-test-output-${libBuildInfo.scalaVersion}"

  val localTestOutputBaseDir: String =
    f"/tmp/" + subdir

  val remoteTestOutputBaseDir: String =
    f"s3://com-cibo-user/" + subdir

  // NOTE: Switch to the remote version to manually test on S3. ;)
  val testOutputBaseDir = localTestOutputBaseDir

  def diffOutputSubdir(subdir: String) = {
    val version =
      if (libBuildInfo.scalaVersion.startsWith("2.11"))
        "2.11"
      else if (libBuildInfo.scalaVersion.startsWith("2.12"))
        "2.12"
      else
        throw new RuntimeException(f"Unexpected scala version $libBuildInfo.scalaVersion")

    val actualOutputDirPath = SyncablePath(f"$testOutputBaseDir/$subdir")
    if (actualOutputDirPath.isRemote) {
      val f = actualOutputDirPath.toFile
      FileUtils.deleteDirectory(f)
      actualOutputDirPath.syncFromS3()
    }

    val actualOutputLocalPath = actualOutputDirPath.localPath

    val newManifestBytes =
      getOutputAsBytes(s"cd $actualOutputLocalPath && wc -c `find . -type file | sort | grep -v codecs`")
    val newManifestString = new String(newManifestBytes)

    val rootSubdir = "src/test/resources/expected-output"

    val expectedDataRoot =
      if (new File(rootSubdir).exists)
        rootSubdir
      else if (new File(f"libprov/$rootSubdir").exists)
        f"libprov/$rootSubdir"
      else
        throw new RuntimeException(f"Failed to find $rootSubdir under the current directory or provenance subdir!")

    val expectedManifestFile = new File(f"$expectedDataRoot/scala-$version/$subdir.manifest")

    try {

      if (!new File(actualOutputLocalPath).exists)
        throw new RuntimeException(s"Failed to find $actualOutputLocalPath!")

      val expectedManifestString =
        if (!expectedManifestFile.exists) {
          logger.warn(s"Failed to find $expectedManifestFile!")
          ""
        } else {
          val expectedManifestBytes = Files.readAllBytes(Paths.get(expectedManifestFile.getAbsolutePath))
          new String(expectedManifestBytes)
        }

      newManifestString shouldBe expectedManifestString

    } catch {
      case e: Exception =>
        // For any failure, replace the test content.  This will show up in git status, and it can be committed or not.
        expectedManifestFile.getParentFile.mkdirs()
        logger.error(f"Writing $expectedManifestFile to put in source control.  Reverse this if the change is not intentional.")
        Files.write(Paths.get(expectedManifestFile.getAbsolutePath), newManifestBytes)
        throw e
    }
  }
}

