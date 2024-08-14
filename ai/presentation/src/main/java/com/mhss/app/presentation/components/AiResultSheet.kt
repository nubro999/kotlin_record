package com.mhss.app.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.R
import com.mhss.app.ui.theme.Blue
import com.mhss.app.ui.theme.LightPurple
import com.mhss.app.ui.theme.MyBrainTheme
import com.mhss.app.ui.theme.OrangeRed
import dev.jeziellago.compose.markdowntext.MarkdownText
import sv.lib.squircleshape.CornerSmoothing
import sv.lib.squircleshape.SquircleShape

@Composable
fun AiResultSheet(
    modifier: Modifier = Modifier,
    loading: Boolean,
    result: String?,
    error: String?,
    onReplaceClick: () -> Unit,
    onAddToNoteClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val offset by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 20,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse,
        ),
        typeConverter = Int.VectorConverter,
        label = "Card y offset"
    )
    val xMul by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
        infiniteRepeatable(
            animation = tween(2900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x Multiplier"
    )
    val yMul by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.9f,
        animationSpec =
        infiniteRepeatable(
            animation = tween(1900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y Multiplier"
    )

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    Box {
        GlowingBorder(
            cornerRadius = 22.dp,
            modifier = Modifier.matchParentSize(),
            innerPadding = PaddingValues(
                vertical = 22.dp,
                horizontal = 22.dp
            ),
            blur = 18.dp
        )
        Card(
            modifier = modifier
                .padding(vertical = 24.dp, horizontal = 8.dp)
                .widthIn(max = 500.dp)
                .padding(horizontal = 12.dp)
                .clickable(enabled = false) {}
                .offset {
                    if (loading) {
                        IntOffset(0, offset)
                    } else IntOffset.Zero
                },
            shape = SquircleShape(
                radius = 42.dp,
                cornerSmoothing = CornerSmoothing.Medium
            ),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .heightIn(min = 120.dp)
                    .drawBehind {
                        drawGradientRadial(
                            surfaceVariant
                                .copy(alpha = 0.75f)
                                .compositeOver(Blue),
                            Offset(
                                size.width * xMul,
                                size.height - size.height * yMul
                            )
                        )
                        drawGradientRadial(
                            surfaceVariant
                                .copy(alpha = 0.75f)
                                .compositeOver(OrangeRed),
                            Offset(
                                size.width - size.width * xMul,
                                size.height - size.height * yMul
                            )
                        )
                        drawGradientRadial(
                            surfaceVariant
                                .copy(alpha = 0.75f)
                                .compositeOver(LightPurple),
                            Offset(
                                size.width - size.width * xMul,
                                size.height * yMul
                            )
                        )
                    }
                    .fillMaxWidth()
                    .animateContentSize(
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (result != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 440.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        MarkdownText(
                            markdown = result,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    AiResultActions(
                        onCopyClick = onCopyClick,
                        onReplaceClick = onReplaceClick,
                        onAddToNoteClick = onAddToNoteClick
                    )
                }
                if (error != null) {
                    MarkdownText(
                        markdown = error,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AiResultActions(
    modifier: Modifier = Modifier,
    onCopyClick: () -> Unit,
    onReplaceClick: () -> Unit,
    onAddToNoteClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AiResultAction(
            textRes = R.string.copy,
            iconRes = R.drawable.ic_copy,
            onClick = onCopyClick
        )
        AiResultAction(
            textRes = R.string.replace,
            iconRes = R.drawable.ic_replace,
            onClick = onReplaceClick
        )
        AiResultAction(
            textRes = R.string.add_to_note,
            iconRes = R.drawable.ic_add_note,
            onClick = onAddToNoteClick
        )
    }
}

@Composable
private fun RowScope.AiResultAction(
    textRes: Int,
    iconRes: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(bottom = 12.dp, top = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(id = textRes),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(id = textRes),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun DrawScope.drawGradientRadial(
    color: Color,
    center: Offset,
    radius: Float = size.maxDimension * 0.75f
) = drawRect(
    brush = Brush.radialGradient(
        colors = listOf(
            color,
            Color.Transparent
        ),
        center = center,
        radius = radius,
    )
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AiResultSheetPreview() {
    MyBrainTheme {
        AiResultSheet(
            modifier = Modifier,
            loading = false,
            result = "This is a test content\n\n".repeat(9),
            error = null,
            {}, {}, {}
        )
    }
}

