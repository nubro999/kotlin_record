package com.mhss.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.R
import com.mhss.app.ui.theme.Blue
import com.mhss.app.ui.theme.DarkOrange
import com.mhss.app.ui.theme.LightPurple
import sv.lib.squircleshape.CornerSmoothing
import sv.lib.squircleshape.SquircleShape

@Composable
fun SttBottomSheet(
    modifier: Modifier = Modifier,
    isListening: Boolean,
    error: String?,
    onStopClick: () -> Unit,
    recognizedText: String
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

    // 그라데이션 애니메이션을 위한 값
    val xMul by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x Multiplier"
    )
    val yMul by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y Multiplier"
    )

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Box(
        Modifier
            .wrapContentHeight()
            .offset {
                if (isListening) {
                    IntOffset(0, offset)
                } else IntOffset.Zero
            }
    ) {
        GlowingBorder(
            modifier = Modifier.matchParentSize(),
            innerPadding = PaddingValues(
                vertical = 28.dp,
                horizontal = 22.dp
            ),
            blur = 24.dp,
            animationDuration = 2000
        )

        Card(
            modifier = modifier
                .padding(vertical = 24.dp)
                .widthIn(max = 500.dp)
                .padding(horizontal = 12.dp)
                .clickable(enabled = false) {},
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
                        // AiResultSheet와 동일한 그라데이션 배경 적용
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
                                .compositeOver(DarkOrange),
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
                if (isListening) {
                    // 마이크 아이콘과 애니메이션
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .size(80.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(80.dp),
                            strokeWidth = 3.dp
                            // 색상 지정 제거 - 테마 기본값 사용
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mic),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                            // 색상 지정 제거 - 테마 기본값 사용
                        )
                    }

                    // 음성인식 중 텍스트
                    Text(
                        text = stringResource(id = R.string.listening),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // 에러 메시지 표시
                AnimatedVisibility(error != null) {
                    Text(
                        text = error ?: "",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                // 인식된 텍스트 표시 (필요한 경우)
                if (recognizedText.isNotEmpty()) {
                    Text(
                        text = recognizedText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // 중지 버튼
                Button(
                    onClick = onStopClick,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .padding(bottom = 8.dp)
                ) {
                    Text(stringResource(id = R.string.stop))
                }
            }
        }
    }
}



