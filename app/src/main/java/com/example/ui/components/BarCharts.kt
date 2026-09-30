package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralCalories
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SlateBorder
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DayStepData(
    val dateString: String,
    val dayLabel: String,
    val steps: Int,
    val isToday: Boolean
)

data class DayCalorieData(
    val dateString: String,
    val dayLabel: String,
    val intake: Int,
    val burned: Int,
    val isToday: Boolean
)

@Composable
fun WeeklyStepBarChart(
    days: List<DayStepData>,
    stepGoal: Int,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<DayStepData?>(null) }
    val maxSteps = (days.maxOfOrNull { it.steps } ?: stepGoal).coerceAtLeast(stepGoal)
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_step_bar_chart"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Steps (Last 7 Days)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = selectedDay?.let {
                            "${it.dayLabel}: ${numberFormat.format(it.steps)} steps"
                        } ?: "Tap a bar to see details",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedDay != null) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Goal: ${numberFormat.format(stepGoal)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Goal benchmark dashed line
                val goalFraction = (stepGoal.toFloat() / maxSteps).coerceIn(0.1f, 1f)

                Canvas(modifier = Modifier.matchParentSize()) {
                    val lineY = size.height * (1f - goalFraction)
                    drawLine(
                        color = EmeraldPrimary.copy(alpha = 0.4f),
                        start = Offset(0f, lineY),
                        end = Offset(size.width, lineY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                }

                // Bars Row
                Row(
                    modifier = Modifier.matchParentSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    days.forEach { day ->
                        val heightFraction = if (maxSteps > 0) {
                            (day.steps.toFloat() / maxSteps).coerceIn(0.04f, 1f)
                        } else 0.05f

                        val isSelected = selectedDay?.dateString == day.dateString

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedDay = if (selectedDay?.dateString == day.dateString) null else day
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height((130 * heightFraction).dp)
                                    .background(
                                        color = when {
                                            isSelected -> EmeraldLight
                                            day.steps >= stepGoal -> EmeraldPrimary
                                            day.isToday -> EmeraldPrimary.copy(alpha = 0.85f)
                                            else -> EmeraldDark.copy(alpha = 0.45f)
                                        },
                                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                days.forEach { day ->
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (day.isToday) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyCalorieChart(
    days: List<DayCalorieData>,
    calorieGoal: Int,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
    val maxCalorie = (days.maxOfOrNull { maxOf(it.intake, it.burned) } ?: calorieGoal).coerceAtLeast(calorieGoal)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_calorie_chart"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Calorie Balance (Intake vs Burned)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Target: ${numberFormat.format(calorieGoal)} kcal/day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CoralCalories, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Intake",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(EmeraldPrimary, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Burned",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart area with paired bars
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Row(
                    modifier = Modifier.matchParentSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    days.forEach { day ->
                        val intakeFrac = (day.intake.toFloat() / maxCalorie).coerceIn(0.04f, 1f)
                        val burnFrac = (day.burned.toFloat() / maxCalorie).coerceIn(0.04f, 1f)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.weight(1f),
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            // Intake bar
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height((110 * intakeFrac).dp)
                                    .background(CoralCalories, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            )
                            // Burn bar
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height((110 * burnFrac).dp)
                                    .background(EmeraldPrimary, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                days.forEach { day ->
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (day.isToday) CoralCalories else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
