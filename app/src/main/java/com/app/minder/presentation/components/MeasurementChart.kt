package com.app.minder.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.minder.domain.model.Measurement
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeasurementChart(
    measurements: List<Measurement>,
    secondaryMeasurements: List<Measurement>? = null,
) {
    if (measurements.isEmpty()) {
        Text(
            text = "Нет данных за последние 2 недели",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    val dateFormat = remember { SimpleDateFormat("dd.MM", Locale.getDefault()) }
    val sorted = measurements.sortedBy { it.createdAt }
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(measurements, secondaryMeasurements) {
        modelProducer.runTransaction {
            if (secondaryMeasurements != null && secondaryMeasurements.isNotEmpty()) {
                val sortedSec = secondaryMeasurements.sortedBy { it.createdAt }
                lineSeries {
                    series(sorted.map { it.result })
                    series(sortedSec.map { it.result })
                }
            } else {
                lineSeries {
                    series(sorted.map { it.result })
                }
            }
        }
    }

    val primaryLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(MaterialTheme.colorScheme.primary)),
        pointProvider = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.Point(
                component = rememberShapeComponent(
                    fill = fill(MaterialTheme.colorScheme.primary),
                    shape = CorneredShape.Pill,
                    strokeThickness = 8.dp
                ),
            )
        )
    )

    val lineProvider = if (secondaryMeasurements != null && secondaryMeasurements.isNotEmpty()) {
        val secondaryLine = LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(fill(MaterialTheme.colorScheme.onTertiary)),
            pointProvider = LineCartesianLayer.PointProvider.single(
                LineCartesianLayer.Point(
                    component = rememberShapeComponent(
                        fill = fill(MaterialTheme.colorScheme.onTertiary),
                        shape = CorneredShape.Pill,
                        strokeThickness = 8.dp
                    ),
                )
            )
        )
        LineCartesianLayer.LineProvider.series(primaryLine, secondaryLine)
    } else {
        LineCartesianLayer.LineProvider.series(primaryLine)
    }

    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent(),
        valueFormatter = { _, targets ->
            val target = targets.firstOrNull() ?: return@rememberDefaultCartesianMarker ""
            val index = target.x.toInt().coerceIn(0, sorted.size - 1)
            val primary = sorted[index]
            val note = primary.note

            val value = if (secondaryMeasurements != null && secondaryMeasurements.isNotEmpty()) {
                val sortedSec = secondaryMeasurements.sortedBy { it.createdAt }
                val secondary = sortedSec.getOrNull(index)
                if (secondary != null) "%.0f/%.0f".format(primary.result, secondary.result)
                else "%.1f".format(primary.result)
            } else {
                "%.1f".format(primary.result)
            }

            if (!note.isNullOrBlank()) "$value — $note" else value
        }
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(lineProvider),
            startAxis = VerticalAxis.rememberStart(
                label = rememberAxisLabelComponent(color = MaterialTheme.colorScheme.onPrimary)
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberAxisLabelComponent(color = MaterialTheme.colorScheme.onPrimary),
                valueFormatter = { _, value, _ ->
                    val index = value.toInt().coerceIn(0, sorted.size - 1)
                    dateFormat.format(Date(sorted[index].createdAt))
                }
            ),
            marker = marker,
        ),
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(
            initialScroll = Scroll.Absolute.End
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.background)

    )
}
