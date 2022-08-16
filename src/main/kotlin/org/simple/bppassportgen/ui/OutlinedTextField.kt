package org.simple.bppassportgen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.MinWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import org.simple.bppassportgen.generator.ui.grey2
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun OutlinedTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = false,
  maxLines: Int = Int.MAX_VALUE,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  shape: Shape = MaterialTheme.shapes.small
) {
  var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
  val textFieldValue = textFieldValueState.copy(text = value)

  TextFieldImpl(
    enabled = enabled,
    readOnly = readOnly,
    value = textFieldValue,
    onValueChange = {
      textFieldValueState = it
      if (value != it.text) {
        onValueChange(it.text)
      }
    },
    modifier = modifier,
    singleLine = singleLine,
    textStyle = textStyle,
    label = label,
    placeholder = placeholder,
    leading = leadingIcon,
    trailing = trailingIcon,
    isError = isError,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    maxLines = maxLines,
    interactionSource = interactionSource,
    shape = shape,
    colors = TextFieldDefaults.outlinedTextFieldColors(
      placeholderColor = MaterialTheme.colors.grey2,
      disabledPlaceholderColor = MaterialTheme.colors.grey2,
      disabledBorderColor = MaterialTheme.colors.grey2,
      unfocusedBorderColor = MaterialTheme.colors.grey2,
      focusedBorderColor = MaterialTheme.colors.primary
    )
  )
}

@Composable
internal fun OutlinedTextFieldLayout(
  modifier: Modifier,
  value: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  enabled: Boolean,
  readOnly: Boolean,
  keyboardOptions: KeyboardOptions,
  keyboardActions: KeyboardActions,
  textStyle: TextStyle,
  singleLine: Boolean,
  maxLines: Int = Int.MAX_VALUE,
  visualTransformation: VisualTransformation,
  interactionSource: MutableInteractionSource,
  decoratedPlaceholder: @Composable ((Modifier) -> Unit)?,
  decoratedLabel: @Composable (() -> Unit)?,
  leading: @Composable (() -> Unit)?,
  trailing: @Composable (() -> Unit)?,
  leadingColor: Color,
  trailingColor: Color,
  labelProgress: Float,
  indicatorWidth: Dp,
  indicatorColor: Color,
  cursorColor: Color,
  backgroundColor: Color,
  shape: Shape
) {
  val labelSize = remember { mutableStateOf(Size.Zero) }
  BasicTextField(
    value = value,
    modifier = modifier
      .then(
        if (decoratedLabel != null) {
          Modifier.padding(top = OutlinedTextFieldTopPadding)
        } else {
          Modifier
        }
      )
      .defaultMinSize(
        minWidth = MinWidth,
        minHeight = TextFieldHeight
      )
      .background(backgroundColor, shape),
    onValueChange = onValueChange,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    cursorBrush = SolidColor(cursorColor),
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    interactionSource = interactionSource,
    singleLine = singleLine,
    maxLines = maxLines,
    decorationBox = @Composable { coreTextField ->
      // places leading icon, input field, label, placeholder, trailing icon
      IconsWithTextFieldLayout(
        textField = coreTextField,
        leading = leading,
        trailing = trailing,
        singleLine = singleLine,
        leadingColor = leadingColor,
        trailingColor = trailingColor,
        onLabelMeasured = {
          val labelWidth = it.width * labelProgress
          val labelHeight = it.height * labelProgress
          if (labelSize.value.width != labelWidth ||
            labelSize.value.height != labelHeight
          ) {
            labelSize.value = Size(labelWidth, labelHeight)
          }
        },
        animationProgress = labelProgress,
        placeholder = decoratedPlaceholder,
        label = decoratedLabel,
        shape = shape,
        borderWidth = indicatorWidth,
        borderColor = indicatorColor,
        labelSize = labelSize.value
      )
    }
  )
}

/**
 * Layout of the leading and trailing icons and the text field, label and placeholder in
 * [OutlinedTextField].
 * It doesn't use Row to position the icons and middle part because label should not be
 * positioned in the middle part.
\ */
@Composable
private fun IconsWithTextFieldLayout(
  textField: @Composable () -> Unit,
  placeholder: @Composable ((Modifier) -> Unit)?,
  label: @Composable (() -> Unit)?,
  leading: @Composable (() -> Unit)?,
  trailing: @Composable (() -> Unit)?,
  singleLine: Boolean,
  leadingColor: Color,
  trailingColor: Color,
  animationProgress: Float,
  onLabelMeasured: (Size) -> Unit,
  shape: Shape,
  borderWidth: Dp,
  borderColor: Color,
  labelSize: Size
) {
  val measurePolicy = remember(onLabelMeasured, singleLine, animationProgress) {
    OutlinedTextFieldMeasurePolicy(onLabelMeasured, singleLine, animationProgress)
  }
  Layout(
    content = {
      // We use additional box here to place an outlined cutout border as a sibling after the
      // rest of UI. This allows us to use Modifier.border to draw an outline on top of the
      // text field. We can't use the border modifier directly on the IconsWithTextFieldLayout
      // as we also need to do the clipping (to form the cutout) which should not affect
      // the rest of text field UI
      Box(
        Modifier
          .layoutId("border")
          .outlinedBorder(shape, borderWidth, borderColor, labelSize)
      )

      if (leading != null) {
        Box(
          modifier = Modifier.layoutId(LeadingId).then(IconDefaultSizeModifier),
          contentAlignment = Alignment.Center
        ) {
          Decoration(
            contentColor = leadingColor,
            content = leading
          )
        }
      }
      if (trailing != null) {
        Box(
          modifier = Modifier.layoutId(TrailingId).then(IconDefaultSizeModifier),
          contentAlignment = Alignment.Center
        ) {
          Decoration(
            contentColor = trailingColor,
            content = trailing
          )
        }
      }
      val paddingToIcon = TextFieldPadding - HorizontalIconPadding
      val padding = Modifier.padding(
        start = if (leading != null) paddingToIcon else TextFieldPadding,
        end = if (trailing != null) paddingToIcon else TextFieldPadding
      )
      if (placeholder != null) {
        placeholder(Modifier.layoutId(PlaceholderId).then(padding))
      }

      Box(
        modifier = Modifier.layoutId(TextFieldId).then(padding),
        propagateMinConstraints = true
      ) {
        textField()
      }

      if (label != null) {
        Box(modifier = Modifier.layoutId(LabelId)) { label() }
      }
    },
    measurePolicy = measurePolicy
  )
}

private class OutlinedTextFieldMeasurePolicy(
  val onLabelMeasured: (Size) -> Unit,
  val singleLine: Boolean,
  val animationProgress: Float
) : MeasurePolicy {
  override fun MeasureScope.measure(
    measurables: List<Measurable>,
    constraints: Constraints
  ): MeasureResult {
    // used to calculate the constraints for measuring elements that will be placed in a row
    var occupiedSpaceHorizontally = 0
    val bottomPadding = TextFieldPadding.roundToPx()

    // measure leading icon
    val relaxedConstraints = constraints.copy(minWidth = 0, minHeight = 0)
    val leadingPlaceable = measurables.find {
      it.layoutId == LeadingId
    }?.measure(relaxedConstraints)
    occupiedSpaceHorizontally += widthOrZero(
      leadingPlaceable
    )

    // measure trailing icon
    val trailingPlaceable = measurables.find { it.layoutId == TrailingId }
      ?.measure(relaxedConstraints.offset(horizontal = -occupiedSpaceHorizontally))
    occupiedSpaceHorizontally += widthOrZero(
      trailingPlaceable
    )

    // measure label
    val labelConstraints = relaxedConstraints.offset(
      horizontal = -occupiedSpaceHorizontally,
      vertical = -bottomPadding
    )
    val labelPlaceable =
      measurables.find { it.layoutId == LabelId }?.measure(labelConstraints)
    labelPlaceable?.let {
      onLabelMeasured(Size(it.width.toFloat(), it.height.toFloat()))
    }

    // measure text field
    // on top we offset either by default padding or by label's half height if its too big
    // minWidth must not be set to 0 due to how foundation TextField treats zero minWidth
    val topPadding = max(heightOrZero(labelPlaceable) / 2, bottomPadding)
    val textConstraints = constraints.offset(
      horizontal = -occupiedSpaceHorizontally,
      vertical = -bottomPadding - topPadding
    ).copy(minHeight = 0)
    val textFieldPlaceable =
      measurables.first { it.layoutId == TextFieldId }.measure(textConstraints)

    // measure placeholder
    val placeholderConstraints = textConstraints.copy(minWidth = 0)
    val placeholderPlaceable =
      measurables.find { it.layoutId == PlaceholderId }?.measure(placeholderConstraints)

    val width =
      calculateWidth(
        widthOrZero(leadingPlaceable),
        widthOrZero(trailingPlaceable),
        textFieldPlaceable.width,
        widthOrZero(labelPlaceable),
        widthOrZero(placeholderPlaceable),
        constraints
      )
    val height =
      calculateHeight(
        heightOrZero(leadingPlaceable),
        heightOrZero(trailingPlaceable),
        textFieldPlaceable.height,
        heightOrZero(labelPlaceable),
        heightOrZero(placeholderPlaceable),
        constraints,
        density
      )

    val borderPlaceable = measurables.first { it.layoutId == "border" }.measure(
      Constraints(
        minWidth = if (width != Constraints.Infinity) width else 0,
        maxWidth = width,
        minHeight = if (height != Constraints.Infinity) height else 0,
        maxHeight = height
      )
    )
    return layout(width, height) {
      place(
        height,
        width,
        leadingPlaceable,
        trailingPlaceable,
        textFieldPlaceable,
        labelPlaceable,
        placeholderPlaceable,
        borderPlaceable,
        animationProgress,
        singleLine,
        density
      )
    }
  }

  override fun IntrinsicMeasureScope.maxIntrinsicHeight(
    measurables: List<IntrinsicMeasurable>,
    width: Int
  ): Int {
    return intrinsicHeight(measurables, width) { intrinsicMeasurable, w ->
      intrinsicMeasurable.maxIntrinsicHeight(w)
    }
  }

  override fun IntrinsicMeasureScope.minIntrinsicHeight(
    measurables: List<IntrinsicMeasurable>,
    width: Int
  ): Int {
    return intrinsicHeight(measurables, width) { intrinsicMeasurable, w ->
      intrinsicMeasurable.minIntrinsicHeight(w)
    }
  }

  override fun IntrinsicMeasureScope.maxIntrinsicWidth(
    measurables: List<IntrinsicMeasurable>,
    height: Int
  ): Int {
    return intrinsicWidth(measurables, height) { intrinsicMeasurable, h ->
      intrinsicMeasurable.maxIntrinsicWidth(h)
    }
  }

  override fun IntrinsicMeasureScope.minIntrinsicWidth(
    measurables: List<IntrinsicMeasurable>,
    height: Int
  ): Int {
    return intrinsicWidth(measurables, height) { intrinsicMeasurable, h ->
      intrinsicMeasurable.minIntrinsicWidth(h)
    }
  }

  private fun intrinsicWidth(
    measurables: List<IntrinsicMeasurable>,
    height: Int,
    intrinsicMeasurer: (IntrinsicMeasurable, Int) -> Int
  ): Int {
    val textFieldWidth =
      intrinsicMeasurer(measurables.first { it.layoutId == TextFieldId }, height)
    val labelWidth = measurables.find { it.layoutId == LabelId }?.let {
      intrinsicMeasurer(it, height)
    } ?: 0
    val trailingWidth = measurables.find { it.layoutId == TrailingId }?.let {
      intrinsicMeasurer(it, height)
    } ?: 0
    val leadingWidth = measurables.find { it.layoutId == LeadingId }?.let {
      intrinsicMeasurer(it, height)
    } ?: 0
    val placeholderWidth = measurables.find { it.layoutId == PlaceholderId }?.let {
      intrinsicMeasurer(it, height)
    } ?: 0
    return calculateWidth(
      leadingPlaceableWidth = leadingWidth,
      trailingPlaceableWidth = trailingWidth,
      textFieldPlaceableWidth = textFieldWidth,
      labelPlaceableWidth = labelWidth,
      placeholderPlaceableWidth = placeholderWidth,
      constraints = ZeroConstraints
    )
  }

  private fun IntrinsicMeasureScope.intrinsicHeight(
    measurables: List<IntrinsicMeasurable>,
    width: Int,
    intrinsicMeasurer: (IntrinsicMeasurable, Int) -> Int
  ): Int {
    val textFieldHeight =
      intrinsicMeasurer(measurables.first { it.layoutId == TextFieldId }, width)
    val labelHeight = measurables.find { it.layoutId == LabelId }?.let {
      intrinsicMeasurer(it, width)
    } ?: 0
    val trailingHeight = measurables.find { it.layoutId == TrailingId }?.let {
      intrinsicMeasurer(it, width)
    } ?: 0
    val leadingHeight = measurables.find { it.layoutId == LeadingId }?.let {
      intrinsicMeasurer(it, width)
    } ?: 0
    val placeholderHeight = measurables.find { it.layoutId == PlaceholderId }?.let {
      intrinsicMeasurer(it, width)
    } ?: 0
    return calculateHeight(
      leadingPlaceableHeight = leadingHeight,
      trailingPlaceableHeight = trailingHeight,
      textFieldPlaceableHeight = textFieldHeight,
      labelPlaceableHeight = labelHeight,
      placeholderPlaceableHeight = placeholderHeight,
      constraints = ZeroConstraints,
      density = density
    )
  }
}

/**
 * Calculate the width of the [OutlinedTextField] given all elements that should be
 * placed inside
 */
private fun calculateWidth(
  leadingPlaceableWidth: Int,
  trailingPlaceableWidth: Int,
  textFieldPlaceableWidth: Int,
  labelPlaceableWidth: Int,
  placeholderPlaceableWidth: Int,
  constraints: Constraints
): Int {
  val middleSection = maxOf(
    textFieldPlaceableWidth,
    labelPlaceableWidth,
    placeholderPlaceableWidth
  )
  val wrappedWidth =
    leadingPlaceableWidth + middleSection + trailingPlaceableWidth
  return max(wrappedWidth, constraints.minWidth)
}

/**
 * Calculate the height of the [OutlinedTextField] given all elements that should be
 * placed inside
 */
private fun calculateHeight(
  leadingPlaceableHeight: Int,
  trailingPlaceableHeight: Int,
  textFieldPlaceableHeight: Int,
  labelPlaceableHeight: Int,
  placeholderPlaceableHeight: Int,
  constraints: Constraints,
  density: Float
): Int {
  // middle section is defined as a height of the text field or placeholder ( whichever is
  // taller) plus 16.dp or half height of the label if it is taller, given that the label
  // is vertically centered to the top edge of the resulting text field's container
  val inputFieldHeight = max(
    textFieldPlaceableHeight,
    placeholderPlaceableHeight
  )
  val topBottomPadding = TextFieldPadding.value * density
  val middleSectionHeight = inputFieldHeight + topBottomPadding + max(
    topBottomPadding,
    labelPlaceableHeight / 2f
  )
  return max(
    constraints.minHeight,
    maxOf(
      leadingPlaceableHeight,
      trailingPlaceableHeight,
      middleSectionHeight.roundToInt()
    )
  )
}

/**
 * Places the provided text field, placeholder, label, optional leading and trailing icons inside
 * the [OutlinedTextField]
 */
private fun Placeable.PlacementScope.place(
  height: Int,
  width: Int,
  leadingPlaceable: Placeable?,
  trailingPlaceable: Placeable?,
  textFieldPlaceable: Placeable,
  labelPlaceable: Placeable?,
  placeholderPlaceable: Placeable?,
  borderPlaceable: Placeable,
  animationProgress: Float,
  singleLine: Boolean,
  density: Float
) {
  val topBottomPadding = (TextFieldPadding.value * density).roundToInt()
  val iconPadding = HorizontalIconPadding.value * density

  // placed center vertically and to the start edge horizontally
  leadingPlaceable?.placeRelative(
    0,
    Alignment.CenterVertically.align(leadingPlaceable.height, height)
  )

  // placed center vertically and to the end edge horizontally
  trailingPlaceable?.placeRelative(
    width - trailingPlaceable.width,
    Alignment.CenterVertically.align(trailingPlaceable.height, height)
  )

  // label position is animated
  // in single line text field label is centered vertically before animation starts
  labelPlaceable?.let {
    val startPositionY = if (singleLine) {
      Alignment.CenterVertically.align(it.height, height)
    } else {
      topBottomPadding
    }
    val positionY =
      startPositionY * (1 - animationProgress) - (it.height / 2) * animationProgress
    val positionX = (
        if (leadingPlaceable == null) {
          0f
        } else {
          (widthOrZero(leadingPlaceable) - iconPadding) * (1 - animationProgress)
        }
        ).roundToInt() + topBottomPadding
    it.placeRelative(positionX, positionY.roundToInt())
  }

  // placed center vertically and after the leading icon horizontally if single line text field
  // placed to the top with padding for multi line text field
  val textVerticalPosition = if (singleLine) {
    Alignment.CenterVertically.align(textFieldPlaceable.height, height)
  } else {
    topBottomPadding
  }
  textFieldPlaceable.placeRelative(widthOrZero(leadingPlaceable), textVerticalPosition)

  // placed similar to the input text above
  placeholderPlaceable?.let {
    val placeholderVerticalPosition = if (singleLine) {
      Alignment.CenterVertically.align(it.height, height)
    } else {
      topBottomPadding
    }
    it.placeRelative(widthOrZero(leadingPlaceable), placeholderVerticalPosition)
  }

  // place border
  borderPlaceable.place(IntOffset.Zero)
}

/**
 * Draws an outlined border with label cutout
 */
private fun Modifier.outlinedBorder(
  shape: Shape,
  borderWidth: Dp,
  borderColor: Color,
  labelSize: Size
) = this
  .outlineCutout(labelSize)
  .border(
    width = borderWidth,
    color = borderColor,
    shape = shape
  )

private fun Modifier.outlineCutout(labelSize: Size) =
  this.drawWithContent {
    val labelWidth = labelSize.width
    if (labelWidth > 0f) {
      val innerPadding = OutlinedTextFieldInnerPadding.toPx()
      val leftLtr = TextFieldPadding.toPx() - innerPadding
      val rightLtr = leftLtr + labelWidth + 2 * innerPadding
      val left = when (layoutDirection) {
        LayoutDirection.Ltr -> leftLtr
        LayoutDirection.Rtl -> size.width - rightLtr
      }
      val right = when (layoutDirection) {
        LayoutDirection.Ltr -> rightLtr
        LayoutDirection.Rtl -> size.width - leftLtr
      }
      val labelHeight = labelSize.height
      // using label height as a cutout area to make sure that no hairline artifacts are
      // left when we clip the border
      clipRect(left, -labelHeight / 2, right, labelHeight / 2, ClipOp.Difference) {
        this@drawWithContent.drawContent()
      }
    } else {
      this@drawWithContent.drawContent()
    }
  }

private val OutlinedTextFieldInnerPadding = 4.dp

/*
This padding is used to allow label not overlap with the content above it. This 8.dp will work
for default cases when developers do not override the label's font size. If they do, they will
need to add additional padding themselves
*/
private val OutlinedTextFieldTopPadding = 8.dp

private val TextFieldHeight = 40.dp
