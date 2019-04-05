package org.simple.bppassportgen

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.nio.file.FileSystems
import java.util.UUID
import java.util.logging.Logger
import javax.imageio.ImageIO

fun main() {
  App().run()
}

class App {

  val logger = Logger.getLogger("App")

  val uuid = UUID.fromString("89dd227d-8c78-4310-9e1f-5cf5e67de2d3")
  val shortCode = "892 2787"

  val black = 0xFF000000.toInt()
  val transparent = 0x00FFFFFF.toInt()

  fun run() {
    logger.info(ImageIO.getWriterFormatNames().joinToString())
    val qrCodeWriter = QRCodeWriter()
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q,
        EncodeHintType.MARGIN to 0
    )

    val bitMatrix = qrCodeWriter.encode(uuid.toString(), BarcodeFormat.QR_CODE, 256, 256, hints)
    MatrixToImageWriter.writeToPath(
        bitMatrix,
        "PNG",
        FileSystems.getDefault().getPath("./barcode.png"),
        MatrixToImageConfig(black, transparent)
    )
  }
}
