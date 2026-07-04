package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun CompletionRingChart(
    percentage: Float,
    title: String,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 14.dp,
    mainColor: Color = AccentTeal,
    trackColor: Color = if (MaterialTheme.colorScheme.background == ObsidianBg) Color(0xFF1E1E2C) else Color(0xFFE2E4EC)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage.coerceIn(0f, 1f),
        animationSpec = tween(1200),
        label = "percentage"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(130.dp)
        ) {
            Canvas(modifier = Modifier.size(110.dp)) {
                // Background Track Ring
                drawCircle(
                    color = trackColor,
                    radius = size.minDimension / 2,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )

                // Foreground Animated Progress
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(mainColor.copy(alpha = 0.4f), mainColor, mainColor)
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun FocusTimeBarChart(
    data: List<Pair<String, Float>>, // List of DayLabel to Minutes
    modifier: Modifier = Modifier,
    barColor: Color = AccentIndigo
) {
    if (data.isEmpty()) return

    val maxVal = (data.maxOfOrNull { it.second } ?: 10f).coerceAtLeast(10f)

    Column(modifier = modifier) {
        Text(
            text = "Daily Focus Time (minutes)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { (label, value) ->
                val animatedRatio by animateFloatAsState(
                    targetValue = value / maxVal,
                    animationSpec = tween(1000),
                    label = "bar"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${value.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(130.dp)
                    ) {
                        val barHeight = size.height * animatedRatio
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(barColor, barColor.copy(alpha = 0.5f))
                            ),
                            topLeft = Offset(0f, size.height - barHeight),
                            size = Size(size.width, barHeight),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MoodTrendLineChart(
    data: List<Float>, // List of numerical values (e.g. Happy=5, Focused=4, Motivated=3, Tired=2, Stressed=1)
    modifier: Modifier = Modifier,
    lineColor: Color = AccentRose
) {
    if (data.isEmpty()) return

    val points = if (data.size < 7) {
        // pad to 7
        data + List(7 - data.size) { 3f }
    } else data

    val maxVal = 5f

    Column(modifier = modifier) {
        Text(
            text = "Weekly Mood Trend",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    if (MaterialTheme.colorScheme.background == ObsidianBg) Color(0xFF13131D) else Color(0xFFF1F3F9),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (points.size - 1)

            val path = Path()
            val fillPath = Path()

            points.forEachIndexed { index, valRaw ->
                val ratio = valRaw / maxVal
                val x = index * stepX
                val y = height - (ratio * height)

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                if (index == points.size - 1) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }
            }

            // Draw line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )

            // Draw soft fill gradient underneath
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent)
                )
            )

            // Draw points
            points.forEachIndexed { index, valRaw ->
                val ratio = valRaw / maxVal
                val x = index * stepX
                val y = height - (ratio * height)
                drawCircle(
                    color = lineColor,
                    radius = 10f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(x, y)
                )
            }
        }
    }
}
