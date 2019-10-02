package org.simple.bppassportgen.qrcodegen

import com.google.zxing.common.BitMatrix

interface QrCodeGenerator {

  fun generateQrCode(content: String, width: Int, height: Int): BitMatrix
}
