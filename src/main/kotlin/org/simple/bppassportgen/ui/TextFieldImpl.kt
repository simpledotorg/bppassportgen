/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.simple.bppassportgen.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldColorsWithIcons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.lerp
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Implementation of the [TextField] and [OutlinedTextField]
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun TextFieldImpl(
  enabled: Boolean,
  readOnly: Boolean,
  value: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  modifier: Modifier,
  singleLine: Boolean,
  textStyle: TextStyle,
  label: @Composable (() -> Unit)?,
  placeholder: @Composable (() -> Unit)?,
  leading: @Composable (() -> Unit)?,
  trailing: @Composable (() -> Unit)?,
  isError: Boolean,
  visualTransformation: VisualTransformation,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions,
  maxLines: Int = Int.MAX_VALUE,
  interactionSource: MutableInteractionSource,
  shape: Shape,
  colors: TextFieldColors
) {
  // If color is not provided via the text style, use content color as a default
  val textColor = textStyle.color.takeOrElse {
    colors.textColor(enabled).value
  }
  val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

  val isFocused = interactionSource.collectIsFocusedAsState().value
  val transformedText = remember(value.annotatedString, visualTransformation) {
    visualTransformation.filter(value.annotatedString)
  }.text
  val inputState = when {
    isFocused -> InputPhase.Focused
    transformedText.isEmpty() -> InputPhase.UnfocusedEmpty
    else -> InputPhase.UnfocusedNotEmpty
  }

  val labelColor: @Composable (InputPhase) -> Color = {
    colors.labelColor(
      enabled,
      // if label is used as a placeholder (aka not as a small header
      // at the top), we don't use an error color
      if (it == InputPhase.UnfocusedEmpty) false else isError,
      interactionSource
    ).value
  }

  val typography = MaterialTheme.typography
  val subtitle1 = typography.subtitle1
  val caption = typography.caption
  val shouldOverrideTextStyleColor =
    (subtitle1.color == Color.Unspecified && caption.color != Color.Unspecified) ||
        (subtitle1.color != Color.Unspecified && caption.color == Color.Unspecified)

  TextFieldTransitionScope.Transition(
    inputState = inputState,
    focusedTextStyleColor = with(MaterialTheme.typography.caption.color) {
      if (shouldOverrideTextStyleColor) this.takeOrElse { labelColor(inputState) } else this
    },
    unfocusedTextStyleColor = with(MaterialTheme.typography.subtitle1.color) {
      if (shouldOverrideTextStyleColor) this.takeOrElse { labelColor(inputState) } else this
    },
    contentColor = labelColor,
    showLabel = label != null
  ) { labelProgress, labelTextStyleColor, labelContentColor, indicatorWidth,
      placeholderAlphaProgress ->

    val decoratedLabel: @Composable (() -> Unit)? = label?.let {
      @Composable {
        val labelTextStyle = lerp(
          MaterialTheme.typography.subtitle1,
          MaterialTheme.typography.caption,
          labelProgress
        ).let {
          if (shouldOverrideTextStyleColor) it.copy(color = labelTextStyleColor) else it
        }
        Decoration(labelContentColor, labelTextStyle, null, it)
      }
    }

    val decoratedPlaceholder: @Composable ((Modifier) -> Unit)? =
      if (placeholder != null && transformedText.isEmpty()) {
        @Composable { modifier ->
          Box(modifier.alpha(placeholderAlphaProgress)) {
            Decoration(
              contentColor = colors.placeholderColor(enabled).value,
              typography = MaterialTheme.typography.subtitle1,
              content = placeholder
            )
          }
        }
      } else null

    val leadingIconColor = if (colors is TextFieldColorsWithIcons) {
      colors.leadingIconColor(enabled, isError, interactionSource).value
    } else {
      colors.leadingIconColor(enabled, isError).value
    }

    val trailingIconColor = if (colors is TextFieldColorsWithIcons) {
      colors.trailingIconColor(enabled, isError, interactionSource).value
    } else {
      colors.trailingIconColor(enabled, isError).value
    }

    OutlinedTextFieldLayout(
      modifier = modifier,
      value = value,
      onValueChange = onValueChange,
      enabled = enabled,
      readOnly = readOnly,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      textStyle = mergedTextStyle,
      singleLine = singleLine,
      maxLines = maxLines,
      visualTransformation = visualTransformation,
      interactionSource = interactionSource,
      decoratedPlaceholder = decoratedPlaceholder,
      decoratedLabel = decoratedLabel,
      leading = leading,
      trailing = trailing,
      leadingColor = leadingIconColor,
      trailingColor = trailingIconColor,
      labelProgress = labelProgress,
      indicatorWidth = indicatorWidth,
      indicatorColor =
      colors.indicatorColor(enabled, isError, interactionSource).value,
      shape = shape,
      backgroundColor = colors.backgroundColor(enabled).value,
      cursorColor = colors.cursorColor(isError).value
    )
  }
}

/**
 * Set content color, typography and emphasis for [content] composable
 */
@Composable
internal fun Decoration(
  contentColor: Color,
  typography: TextStyle? = null,
  contentAlpha: Float? = null,
  content: @Composable () -> Unit
) {
  val colorAndEmphasis: @Composable () -> Unit = @Composable {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
      if (contentAlpha != null) {
        CompositionLocalProvider(
          LocalContentAlpha provides contentAlpha,
          content = content
        )
      } else {
        CompositionLocalProvider(
          LocalContentAlpha provides contentColor.alpha,
          content = content
        )
      }
    }
  }
  if (typography != null) ProvideTextStyle(typography, colorAndEmphasis) else colorAndEmphasis()
}

internal fun widthOrZero(placeable: Placeable?) = placeable?.width ?: 0
internal fun heightOrZero(placeable: Placeable?) = placeable?.height ?: 0

private object TextFieldTransitionScope {
  @Composable
  fun Transition(
    inputState: InputPhase,
    focusedTextStyleColor: Color,
    unfocusedTextStyleColor: Color,
    contentColor: @Composable (InputPhase) -> Color,
    showLabel: Boolean,
    content: @Composable (
      labelProgress: Float,
      labelTextStyleColor: Color,
      labelContentColor: Color,
      indicatorWidth: Dp,
      placeholderOpacity: Float
    ) -> Unit
  ) {
    // Transitions from/to InputPhase.Focused are the most critical in the transition below.
    // UnfocusedEmpty <-> UnfocusedNotEmpty are needed when a single state is used to control
    // multiple text fields.
    val transition = updateTransition(inputState, label = "TextFieldInputState")

    val labelProgress by transition.animateFloat(
      label = "LabelProgress",
      transitionSpec = { tween(durationMillis = AnimationDuration) }
    ) {
      when (it) {
        InputPhase.Focused -> 1f
        InputPhase.UnfocusedEmpty -> 0f
        InputPhase.UnfocusedNotEmpty -> 1f
      }
    }

    val indicatorWidth by transition.animateDp(
      label = "IndicatorWidth",
      transitionSpec = { tween(durationMillis = AnimationDuration) }
    ) {
      when (it) {
        InputPhase.Focused -> IndicatorFocusedWidth
        InputPhase.UnfocusedEmpty -> IndicatorUnfocusedWidth
        InputPhase.UnfocusedNotEmpty -> IndicatorUnfocusedWidth
      }
    }

    val placeholderOpacity by transition.animateFloat(
      label = "PlaceholderOpacity",
      transitionSpec = {
        if (InputPhase.Focused isTransitioningTo InputPhase.UnfocusedEmpty) {
          tween(
            durationMillis = PlaceholderAnimationDelayOrDuration,
            easing = LinearEasing
          )
        } else if (InputPhase.UnfocusedEmpty isTransitioningTo InputPhase.Focused ||
          InputPhase.UnfocusedNotEmpty isTransitioningTo InputPhase.UnfocusedEmpty
        ) {
          tween(
            durationMillis = PlaceholderAnimationDuration,
            delayMillis = PlaceholderAnimationDelayOrDuration,
            easing = LinearEasing
          )
        } else {
          spring()
        }
      }
    ) {
      when (it) {
        InputPhase.Focused -> 1f
        InputPhase.UnfocusedEmpty -> if (showLabel) 0f else 1f
        InputPhase.UnfocusedNotEmpty -> 0f
      }
    }

    val labelTextStyleColor by transition.animateColor(
      transitionSpec = { tween(durationMillis = AnimationDuration) },
      label = "LabelTextStyleColor"
    ) {
      when (it) {
        InputPhase.Focused -> focusedTextStyleColor
        else -> unfocusedTextStyleColor
      }
    }

    val labelContentColor by transition.animateColor(
      transitionSpec = { tween(durationMillis = AnimationDuration) },
      label = "LabelContentColor",
      targetValueByState = contentColor
    )

    content(
      labelProgress,
      labelTextStyleColor,
      labelContentColor,
      indicatorWidth,
      placeholderOpacity
    )
  }
}

/**
 * An internal state used to animate a label and an indicator.
 */
private enum class InputPhase {
  // Text field is focused
  Focused,

  // Text field is not focused and input text is empty
  UnfocusedEmpty,

  // Text field is not focused but input text is not empty
  UnfocusedNotEmpty
}

internal val IntrinsicMeasurable.layoutId: Any?
  get() = (parentData as? LayoutIdParentData)?.layoutId

internal const val TextFieldId = "TextField"
internal const val PlaceholderId = "Hint"
internal const val LabelId = "Label"
internal const val LeadingId = "Leading"
internal const val TrailingId = "Trailing"
internal val ZeroConstraints = Constraints(0, 0, 0, 0)

internal const val AnimationDuration = 150
private const val PlaceholderAnimationDuration = 83
private const val PlaceholderAnimationDelayOrDuration = 67

private val IndicatorUnfocusedWidth = 1.dp
private val IndicatorFocusedWidth = 2.dp
internal val TextFieldPadding = 8.dp
internal val HorizontalIconPadding = 12.dp

internal val IconDefaultSizeModifier = Modifier.defaultMinSize(48.dp, 48.dp)
