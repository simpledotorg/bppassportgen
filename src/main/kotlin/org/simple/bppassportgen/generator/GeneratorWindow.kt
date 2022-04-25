package org.simple.bppassportgen.generator.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonDefaults.OutlinedBorderSize
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.simple.bppassportgen.generator.GeneratorType
import org.simple.bppassportgen.generator.PageSize
import org.simple.bppassportgen.generator.PassportsGeneratorModel
import org.simple.bppassportgen.ui.OutlinedTextField
import java.io.File

@Composable
fun GeneratorWindow(
  modifier: Modifier = Modifier,
  model: PassportsGeneratorModel,
  onGeneratorTypeChanged: (GeneratorType) -> Unit,
  onNumberOfQrCodesChanged: (Int) -> Unit,
  onPageSizeChanged: (PageSize) -> Unit,
  onOpenTemplateFileClick: () -> Unit
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(
        start = 16.dp,
        top = 24.dp,
        end = 16.dp,
        bottom = 80.dp
      )
  ) {
    QRCodeTypeSelector(
      generatorType = model.generatorType,
      onTypeChanged = onGeneratorTypeChanged
    )

    Divider(
      startIndent = 0.dp,
      color = Color.Black.copy(alpha = 0.14f)
    )

    Spacer(Modifier.height(16.dp))

    TemplateFileSelector(
      templateFilePath = model.templateFilePath,
      openFileClicked = onOpenTemplateFileClick
    )

    Spacer(Modifier.height(16.dp))

    TotalNumberOfQrCodes(
      numberOfQrCodes = model.numberOfPassports,
      generatorType = model.generatorType,
      onNumberOfQrCodesChanged = onNumberOfQrCodesChanged
    )

    Spacer(Modifier.height(16.dp))

    PageSizeDropDown(
      generatorType = model.generatorType,
      pageSize = model.pageSize,
      onPageSizeChanged = onPageSizeChanged
    )

    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun QRCodeTypeSelector(
  modifier: Modifier = Modifier,
  generatorType: GeneratorType,
  onTypeChanged: (GeneratorType) -> Unit
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = "What do you want to generate?",
      style = MaterialTheme.typography.subtitle1Medium
    )

    Spacer(modifier = Modifier.height(16.dp))

    GeneratorTypeSelectorButtonGroup(
      generatorType = generatorType,
      onTypeChanged = onTypeChanged
    )

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
private fun GeneratorTypeSelectorButtonGroup(
  modifier: Modifier = Modifier,
  generatorType: GeneratorType,
  onTypeChanged: (GeneratorType) -> Unit
) {
  val selectedColor = MaterialTheme.colors.primary
  val unselectedColor = MaterialTheme.colors.grey2

  val selectedBorder = BorderStroke(
    width = OutlinedBorderSize,
    color = selectedColor
  )

  val unselectedBorder = BorderStroke(
    width = OutlinedBorderSize,
    color = unselectedColor
  )

  val bpPassportBorder = if (generatorType == GeneratorType.Passport) {
    selectedBorder
  } else {
    unselectedBorder
  }

  val stickerBorder = if (generatorType == GeneratorType.Sticker) {
    selectedBorder
  } else {
    unselectedBorder
  }

  val bpPassportTextColor = if (generatorType == GeneratorType.Passport) {
    selectedColor
  } else {
    unselectedColor
  }

  val stickerTextColor = if (generatorType == GeneratorType.Sticker) {
    selectedColor
  } else {
    unselectedColor
  }

  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceEvenly
  ) {
    OutlinedButton(
      modifier = Modifier.weight(1f),
      onClick = {
        onTypeChanged(GeneratorType.Passport)
      },
      border = bpPassportBorder,
      colors = ButtonDefaults.outlinedButtonColors(contentColor = bpPassportTextColor)
    ) {
      Icon(
        modifier = Modifier.size(16.dp),
        painter = painterResource("drawable/ic_passport.svg"),
        contentDescription = null
      )
      Spacer(Modifier.width(8.dp))
      Text("BP PASSPORT")
    }

    Spacer(Modifier.width(8.dp))

    OutlinedButton(
      modifier = Modifier.weight(1f),
      onClick = {
        onTypeChanged(GeneratorType.Sticker)
      },
      border = stickerBorder,
      colors = ButtonDefaults.outlinedButtonColors(contentColor = stickerTextColor)
    ) {
      Icon(
        modifier = Modifier.size(16.dp),
        painter = painterResource("drawable/ic_qr_code.svg"),
        contentDescription = null
      )
      Spacer(Modifier.width(8.dp))
      Text("STICKER")
    }
  }
}

@Composable
private fun TemplateFileSelector(
  modifier: Modifier = Modifier,
  templateFilePath: String?,
  openFileClicked: () -> Unit
) {
  Column(
    modifier = modifier.fillMaxWidth()
  ) {
    Text("Template file", style = MaterialTheme.typography.subtitle1Medium)

    Spacer(modifier = Modifier.height(8.dp))

    val fileName = if (templateFilePath.isNullOrBlank()) {
      templateFilePath.orEmpty()
    } else {
      File(templateFilePath).name
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        modifier = Modifier.weight(2f),
        value = fileName,
        placeholder = {
          Text(
            text = "Open file...",
            style = MaterialTheme.typography.subtitle1,
          )
        },
        onValueChange = { /*no-op*/ },
        textStyle = MaterialTheme.typography.subtitle1,
        readOnly = true
      )

      Spacer(modifier = Modifier.width(8.dp))

      TextButton(
        modifier = Modifier.weight(0.75f),
        onClick = openFileClicked,
      ) {
        Icon(Icons.Filled.Folder, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text = "OPEN")
      }
    }
  }
}

@Composable
private fun TotalNumberOfQrCodes(
  modifier: Modifier = Modifier,
  numberOfQrCodes: Int?,
  generatorType: GeneratorType,
  onNumberOfQrCodesChanged: (Int) -> Unit
) {
  val title = when (generatorType) {
    GeneratorType.Passport -> "Total passports"
    GeneratorType.Sticker -> "Total stickers"
  }

  Column(
    modifier = modifier.fillMaxWidth()
  ) {
    Text(title, style = MaterialTheme.typography.subtitle1Medium)

    Spacer(modifier = Modifier.height(8.dp))

    val textFieldValue = if (numberOfQrCodes != null && numberOfQrCodes > 0) {
      numberOfQrCodes.toString()
    } else {
      ""
    }

    OutlinedTextField(
      modifier = Modifier.fillMaxWidth(),
      value = textFieldValue,
      placeholder = {
        Text(
          "0",
          style = MaterialTheme.typography.subtitle1,
          color = MaterialTheme.colors.grey2
        )
      },
      onValueChange = { newValue ->
        onNumberOfQrCodesChanged.invoke(newValue.filter { it.isDigit() }.toIntOrNull() ?: 0)
      },
      textStyle = MaterialTheme.typography.subtitle1
    )
  }
}

@Composable
private fun PageSizeDropDown(
  modifier: Modifier = Modifier,
  generatorType: GeneratorType,
  pageSize: PageSize?,
  onPageSizeChanged: (PageSize) -> Unit
) {
  val title = when (generatorType) {
    GeneratorType.Passport -> "Print paper size"
    GeneratorType.Sticker -> "Sticker sheet size"
  }
  var menuExpanded by remember { mutableStateOf(false) }

  Column(
    modifier = modifier.fillMaxWidth()
  ) {
    Text(title, style = MaterialTheme.typography.subtitle1Medium)

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentWidth(align = Alignment.Start)
    ) {
      OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
          menuExpanded = !menuExpanded
        },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
        border = BorderStroke(
          width = OutlinedBorderSize,
          color = MaterialTheme.colors.grey2
        )
      ) {
        val pageSizeLabel = if (pageSize != null) {
          "${pageSize.label} (${pageSize.labelWithCount})"
        } else {
          "Select paper size"
        }
        val pageSizeTextColor = if (pageSize != null) {
          Color.Black
        } else {
          MaterialTheme.colors.grey2
        }

        Text(
          modifier = Modifier.weight(2f),
          text = pageSizeLabel,
          style = MaterialTheme.typography.subtitle1,
          color = pageSizeTextColor
        )
        Icon(
          modifier = Modifier.weight(0.2f),
          imageVector = Icons.Filled.KeyboardArrowDown,
          contentDescription = null,
          tint = MaterialTheme.colors.primary
        )
      }

      val pageSizes = pageSizes(generatorType)
      DropdownMenu(
        expanded = menuExpanded,
        focusable = true,
        onDismissRequest = { menuExpanded = !menuExpanded }
      ) {
        pageSizes.map {
          PageSizeDropdownItem(
            pageSize = it,
            onPageSizeClicked = {
              onPageSizeChanged(it)
              menuExpanded = !menuExpanded
            }
          )
        }
      }
    }
  }
}

@Composable
private fun PageSizeDropdownItem(
  modifier: Modifier = Modifier,
  pageSize: PageSize,
  onPageSizeClicked: () -> Unit
) {
  DropdownMenuItem(
    modifier = modifier.fillMaxWidth(),
    onClick = onPageSizeClicked
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = pageSize.label,
      style = MaterialTheme.typography.body2
    )

    Text(
      modifier = Modifier.weight(2f),
      text = pageSize.labelWithCount,
      style = MaterialTheme.typography.body2,
      color = MaterialTheme.colors.grey1,
      textAlign = TextAlign.End
    )
  }
}

private fun pageSizes(generatorType: GeneratorType) = when (generatorType) {
  GeneratorType.Passport -> listOf(
    PageSize(
      rows = 1,
      columns = 2,
      label = "A4",
      labelWithCount = "2 passports per page"
    ),
    PageSize(
      rows = 2,
      columns = 2,
      label = "A3",
      labelWithCount = "4 passports per page"
    ),
    PageSize(
      rows = 2,
      columns = 4,
      label = "A2",
      labelWithCount = "8 passports per page"
    ),
    PageSize(
      rows = 4,
      columns = 4,
      label = "A1",
      labelWithCount = "16 passports per page"
    ),
    PageSize(
      rows = 4,
      columns = 8,
      label = "A0",
      labelWithCount = "32 passports per page"
    )
  )
  GeneratorType.Sticker -> listOf(
    PageSize(
      rows = 6,
      columns = 5,
      label = "A5",
      labelWithCount = "30 stickers per page"
    ),
    PageSize(
      rows = 6,
      columns = 10,
      label = "A4",
      labelWithCount = "60 stickers per page"
    )
  )
}

@Preview
@Composable
fun GeneratorWindowPreview() {
  AppTheme {
    GeneratorWindow(
      modifier = Modifier
        .size(width = 400.dp, height = 600.dp)
        .border(2.dp, Color.Black),
      model = PassportsGeneratorModel.create(),
      onGeneratorTypeChanged = {},
      onNumberOfQrCodesChanged = {},
      onPageSizeChanged = {},
      onOpenTemplateFileClick = {}
    )
  }
}
