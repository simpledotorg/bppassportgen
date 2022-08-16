package org.simple.clinic.bppassportgen.util

import org.approvaltests.Approvals
import org.approvaltests.approvers.ApprovalApprover
import org.approvaltests.core.ApprovalFailureReporter
import org.approvaltests.core.ApprovalReporterWithCleanUp
import org.approvaltests.core.ApprovalWriter
import org.approvaltests.namer.ApprovalNamer
import org.approvaltests.writers.ImageApprovalWriter
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ImageApprover(
  namer: ApprovalNamer,
  private val writer: ApprovalWriter,
  private val minimumSimilarity: Double
) : ApprovalApprover {

  private val baseFilePath = "${namer.sourceFilePath}${namer.approvalName}"

  private val received: File = File(writer.getReceivedFilename(baseFilePath))
  private val approved: File = File(writer.getApprovalFilename(baseFilePath))

  companion object {
    fun create(image: BufferedImage, minimumSimilarity: Double): ImageApprover {
      return ImageApprover(
        namer = Approvals.createApprovalNamer(),
        writer = ImageApprovalWriter(image),
        minimumSimilarity = minimumSimilarity
      )
    }
  }

  override fun cleanUpAfterSuccess(reporter: ApprovalFailureReporter) {
    received.delete()
    (reporter as? ApprovalReporterWithCleanUp)?.cleanUp(received.absolutePath, approved.absolutePath)
  }

  override fun approve(): Boolean {
    writer.writeReceivedFile(received.absolutePath)

    if (!approved.exists() || !received.exists()) {
      return false
    }

    val receivedImage = ImageIO.read(received)
    val approvedImage = ImageIO.read(approved)

    val similarity = ImageSimilarity.similarity(receivedImage, approvedImage)
    return similarity >= minimumSimilarity
  }

  override fun reportFailure(reporter: ApprovalFailureReporter) {
    reporter.report(received.absolutePath, approved.absolutePath)
  }

  override fun fail() {
    throw Error("Failed Approval\n  Approved:${approved.absolutePath}\n  Received:${received.absolutePath}")
  }
}
