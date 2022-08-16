package org.simple.bppassportgen.qrcodegen

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

class QrCodeGeneratorImpl(
  errorCorrectionLevel: ErrorCorrectionLevel,
  margin: Int
) : QrCodeGenerator {

  private val qrCodeWriter = QRCodeWriter()
  private val hints = mapOf(
    EncodeHintType.ERROR_CORRECTION to errorCorrectionLevel,
    EncodeHintType.MARGIN to margin
  )

  override fun generateQrCode(content: String, width: Int, height: Int): BitMatrix {
    return qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
  }
}
