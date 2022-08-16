package org.simple.bppassportgen.generator.ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AppTheme(
  content: @Composable () -> Unit
) {
  val typography = Typography(
    subtitle1 = TextStyle(
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      letterSpacing = 0.2.sp,
      lineHeight = 24.sp
    ),
    button = TextStyle(
      fontWeight = FontWeight.Medium,
      fontSize = 14.sp,
      letterSpacing = 1.sp,
      lineHeight = 16.sp
    )
  )

  val colors = lightColors(
    primary = Color(0xFF0075EB),
    primaryVariant = Color(0xFFE0F0FF)
  )

  MaterialTheme(
    typography = typography,
    colors = colors,
    content = content
  )
}

val Typography.subtitle1Medium: TextStyle
  get() = subtitle1.copy(
    fontWeight = FontWeight.Medium
  )

val Typography.body2Bold: TextStyle
  get() = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    letterSpacing = 0.2.sp,
    lineHeight = 20.sp
  )

val Colors.toolbarPrimary: Color
  get() = Color(0xFF0C3966)

val Colors.grey1: Color
  get() = Color(0xAB2F363D)

val Colors.grey2: Color
  get() = Color(0x572F363D)

val Colors.grey3: Color
  get() = Color(0x112F363D)
