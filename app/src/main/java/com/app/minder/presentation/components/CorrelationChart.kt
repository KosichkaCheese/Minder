package com.app.minder.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.minder.presentation.correlation.CorrelationItem
import com.app.minder.presentation.correlation.DataPoint
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.point
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CorrelationChart(
    series: Map<CorrelationItem, List<DataPoint>>
) {
    val seriesList = series.entries.toList()
    if (seriesList[0].value.size<3 || seriesList[1].value.size<3){
        Text(text = "Недостаточно данных",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )
        return
    }
    val points1 = seriesList[0].value.sortedBy { it.timestamp }
    val points2 = seriesList[1].value.sortedBy { it.timestamp }
    val allTimestamps = (points1.map { it.timestamp } + points2.map { it.timestamp })
        .distinct()
        .sorted()
    val minTimestamp = allTimestamps.first()
    val maxTimestamp = allTimestamps.last()
    val timeRange = (maxTimestamp - minTimestamp).toDouble().coerceAtLeast(1.0)


    val dateFormat = remember { SimpleDateFormat("dd.MM", Locale.getDefault()) }
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(series) {
        modelProducer.runTransaction {

            lineSeries {
                series(
                    x = points1.map { p ->
                        allTimestamps.indexOfFirst { it >= p.timestamp }.toDouble()
                    },
                    y = points1.map { it.value }
                )
                series(
                    x = points2.map { p ->
                        allTimestamps.indexOfFirst { it >= p.timestamp }.toDouble()
                    },
                    y = points2.map { it.value }
                )
            }

        }
    }

    val lineProvider = LineCartesianLayer.LineProvider.series(
        seriesList.mapIndexed { index, _ ->
            val color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onTertiary
            LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(fill(Color.Transparent)),
                areaFill = null,
                pointProvider = LineCartesianLayer.PointProvider.single(
                    LineCartesianLayer.point(
                        component = rememberShapeComponent(fill(color), CorneredShape.Pill),
                        size = 8.dp
                    )
                )
            )
        }
    )

    Column {
        seriesList.forEachIndexed { index, (item, _) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            if (index == 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onTertiary,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = rememberAxisLabelComponent(color = MaterialTheme.colorScheme.onPrimary)
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = rememberAxisLabelComponent(color = MaterialTheme.colorScheme.onPrimary),
                    valueFormatter = { _, value, _ ->
                        val index = value.toInt().coerceIn(0, allTimestamps.size - 1)
                        dateFormat.format(Date(allTimestamps[index]))
                    },
//                    itemPlacer = remember { HorizontalAxis.ItemPlacer.aligned(spacing = { 10 }) },
                )
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
}