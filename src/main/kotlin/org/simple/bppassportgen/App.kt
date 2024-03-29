package org.simple.bppassportgen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.spotify.mobius.rx2.RxMobius
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.simple.bppassportgen.generator.GeneratePassportsButtonClicked
import org.simple.bppassportgen.generator.GeneratorTypeChanged
import org.simple.bppassportgen.generator.NumberOfPassportsChanged
import org.simple.bppassportgen.generator.PageSizeChanged
import org.simple.bppassportgen.generator.PassportsGeneratorEffectHandler
import org.simple.bppassportgen.generator.PassportsGeneratorModel
import org.simple.bppassportgen.generator.PassportsGeneratorUpdate
import org.simple.bppassportgen.generator.TemplateFileSelected
import org.simple.bppassportgen.generator.ui.AppTheme
import org.simple.bppassportgen.generator.ui.GeneratorWindow
import org.simple.bppassportgen.generator.ui.body2Bold
import org.simple.bppassportgen.generator.ui.toolbarPrimary
import org.simple.bppassportgen.renderable.RenderSpecProviderImpl
import org.simple.bppassportgen.scheduler.RealSchedulersProvider
import java.awt.Desktop
import java.net.URI
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun main() = application {
  val windowState = rememberWindowState(
    width = 400.dp,
    height = 610.dp,
    position = WindowPosition.Aligned(Alignment.Center)
  )

  Window(
    title = "QR Code Generator",
    state = windowState,
    resizable = false,
    undecorated = true,
    transparent = true,
    onCloseRequest = ::exitApplication
  ) {
    val defaultModel = PassportsGeneratorModel.create()
    var model by remember { mutableStateOf(defaultModel) }
    val metropolisFontId = "Metropolis-Medium"
    val blackCmykId = "cmyk_black"

    val fonts = mapOf(metropolisFontId to "Metropolis-Medium.ttf")
    val colors = mapOf(
      blackCmykId to PDColor(
        floatArrayOf(0F, 0F, 0F, 1F),
        COSName.DEVICECMYK,
        PDDeviceCMYK.INSTANCE
      )
    )
    val loop = RxMobius
      .loop(
        PassportsGeneratorUpdate(),
        PassportsGeneratorEffectHandler(
          passportsGenerator = PassportsGenerator(
            renderSpecProvider = RenderSpecProviderImpl(),
            fonts = fonts,
            colorMap = colors
          ),
          schedulersProvider = RealSchedulersProvider()
        ).build()
      )
      .startFrom(defaultModel)

    loop.observe { updatedModel ->
      model = updatedModel
    }

    AppTheme {
      Scaffold(
        modifier = Modifier
          .background(MaterialTheme.colors.surface, MaterialTheme.shapes.small)
          .clip(MaterialTheme.shapes.small),
        backgroundColor = Color.Unspecified,
        topBar = {
          AppBar(
            onCloseClick = ::exitApplication,
            onMinimizeClick = {
              windowState.isMinimized = true
            }
          )
        },
        content = {
          GeneratorWindow(
            model = model,
            onGeneratorTypeChanged = { generatorType ->
              loop.dispatchEvent(GeneratorTypeChanged(generatorType))
            },
            onNumberOfQrCodesChanged = { numberOfQrCodes ->
              loop.dispatchEvent(NumberOfPassportsChanged(numberOfQrCodes))
            },
            onPageSizeChanged = { pageSize ->
              loop.dispatchEvent(PageSizeChanged(pageSize))
            },
            onOpenTemplateFileClick = {
              val templateFilePath = openFilePicker(
                currentDirectory = model.templateFilePath,
                filterExtensions = "pdf",
                fileSelectionMode = JFileChooser.FILES_ONLY
              )
              loop.dispatchEvent(TemplateFileSelected(templateFilePath.orEmpty()))
            }
          )
        },
        bottomBar = {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp)
              .background(Color.White)
          ) {
            if (model.isInProgress) {
              CircularProgressIndicator(
                modifier = Modifier
                  .align(Alignment.Center)
                  .size(44.dp)
              )
            } else {
              GenerateButton(
                enabled = model.isRequiredDataFilled
              ) {
                val outputDirectoryPath = openFilePicker(
                  currentDirectory = null,
                  fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                )

                if (outputDirectoryPath.isNullOrBlank().not()) {
                  loop.dispatchEvent(GeneratePassportsButtonClicked(outputDirectoryPath!!))
                }
              }
            }
          }
        },
      )
    }

    DisposableEffect(Unit) {
      onDispose {
        loop.dispose()
      }
    }
  }
}

@Composable
private fun FrameWindowScope.AppBar(
  modifier: Modifier = Modifier,
  onMinimizeClick: () -> Unit,
  onCloseClick: () -> Unit
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .background(MaterialTheme.colors.toolbarPrimary)
  ) {
    WindowDraggableArea(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          painter = painterResource("drawable/ic_simple_logo.svg"),
          contentDescription = null,
          tint = Color.Unspecified
        )

        Spacer(Modifier.width(8.dp))

        Text(
          modifier = Modifier.weight(2f),
          text = "QR Code Generator",
          style = MaterialTheme.typography.body2Bold,
          color = MaterialTheme.colors.onPrimary
        )

        WindowActionIconButton(
          imageVector = Icons.Filled.Minimize,
          onClick = onMinimizeClick
        )

        WindowActionIconButton(
          imageVector = Icons.Filled.Close,
          onClick = onCloseClick
        )
      }
    }

    ExtendedAppBar()
  }
}

@Composable
private fun ExtendedAppBar(
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalArrangement = Arrangement.End
  ) {
    AppBarActionButton(
      text = "Template",
      imageVector = Icons.Filled.Download
    ) {
      openLink("https://drive.google.com/drive/folders/1-V94ufms5k4ZeoMzaF-EQPYPjhaQ9wze?usp=sharing")
    }

    Spacer(Modifier.width(16.dp))

    AppBarActionButton(
      text = "Help",
      imageVector = Icons.Filled.HelpOutline
    ) {
      openLink("https://docs.google.com/document/d/1tpns-PgoPsXSDRyVrFfU2bJ98ZyYq1YCzMhj2MHJhiE/edit#")
    }
  }
}

@Composable
private fun AppBarActionButton(
  modifier: Modifier = Modifier,
  text: String,
  imageVector: ImageVector,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .background(
        color = Color.Black.copy(alpha = 0.24f),
        shape = RoundedCornerShape(4.dp)
      )
      .clickable { onClick.invoke() }
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = imageVector,
      contentDescription = null,
      tint = MaterialTheme.colors.onPrimary,
      modifier = Modifier.size(16.dp)
    )
    Spacer(Modifier.width(4.dp))
    Text(
      text = text.uppercase(),
      style = MaterialTheme.typography.button,
      color = MaterialTheme.colors.onPrimary
    )
  }
}

@Composable
private fun WindowActionIconButton(
  modifier: Modifier = Modifier,
  imageVector: ImageVector,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .size(40.dp)
      .clickable(
        onClick = onClick,
        role = Role.Button,
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(bounded = true)
      ),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = imageVector,
      contentDescription = null,
      tint = MaterialTheme.colors.onPrimary
    )
  }
}

@Composable
private fun BoxScope.GenerateButton(
  modifier: Modifier = Modifier,
  enabled: Boolean,
  onClick: () -> Unit
) {
  Button(
    modifier = modifier
      .fillMaxWidth()
      .height(44.dp)
      .align(Alignment.Center),
    enabled = enabled,
    onClick = onClick
  ) {
    Text("GENERATE")
  }
}

private fun openFilePicker(
  currentDirectory: String?,
  fileSelectionMode: Int,
  filterExtensions: String? = null
): String? {
  val chooser = JFileChooser(currentDirectory).apply {
    this.fileSelectionMode = fileSelectionMode

    if (filterExtensions != null) {
      val fileNameExtensionFilter = FileNameExtensionFilter(
        "*.$filterExtensions",
        filterExtensions
      )
      fileFilter = fileNameExtensionFilter
    }

    isAcceptAllFileFilterUsed = false
  }

  return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
    chooser.selectedFile.path
  } else {
    null
  }
}

private fun openLink(link: String) {
  val desktop = Desktop.getDesktop()
  desktop.browse(URI.create(link))
}
