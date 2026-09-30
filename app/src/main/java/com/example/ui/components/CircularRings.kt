package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralCalories
import com.example.ui.theme.CoralDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DualActivityRings(
    stepProgress: Float,
    calorieProgress: Float,
    steps: Int,
    stepGoal: Int,
    caloriesIntake: Int,
    caloriesBurned: Int,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp
) {
    val animatedStepProgress by animateFloatAsState(
        targetValue = stepProgress.coerceIn(0f, 1.5f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "step_ring_anim"
    )

    val animatedCalorieProgress by animateFloatAsState(
        targetValue = calorieProgress.coerceIn(0f, 1.5f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "calorie_ring_anim"
    )

    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    Box(
        modifier = modifier
            .size(size)
            .testTag("dual_activity_rings"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeOuter = 16.dp.toPx()
            val strokeInner = 14.dp.toPx()
            val gap = 8.dp.toPx()

            val outerRadius = (this.size.minDimension - strokeOuter) / 2f
            val innerRadius = outerRadius - strokeOuter / 2f - gap - strokeInner / 2f

            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Background track - Outer (Steps)
            drawCircle(
                color = EmeraldPrimary.copy(alpha = 0.15f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
            )

            // Progress - Outer (Steps)
            val stepSweep = (animatedStepProgress * 360f).coerceAtMost(360f)
            if (stepSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(EmeraldLight, EmeraldPrimary, EmeraldLight),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = stepSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
                )
            }

            // Background track - Inner (Calories)
            drawCircle(
                color = CoralCalories.copy(alpha = 0.15f),
                radius = innerRadius,
                center = center,
                style = Stroke(width = strokeInner, cap = StrokeCap.Round)
            )

            // Progress - Inner (Calories)
            val calSweep = (animatedCalorieProgress * 360f).coerceAtMost(360f)
            if (calSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(CoralCalories, CoralDark, CoralCalories),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = calSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                    size = Size(innerRadius * 2, innerRadius * 2),
                    style = Stroke(width = strokeInner, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = "Steps",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = numberFormat.format(steps),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "of ${numberFormat.format(stepGoal)} steps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Calories",
                    tint = CoralCalories,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${numberFormat.format(caloriesIntake)} kcal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = CoralCalories
                    )
                )
            }
            Text(
                text = "Burned: ${numberFormat.format(caloriesBurned)} kcal",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
