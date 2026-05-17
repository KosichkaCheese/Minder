package com.app.minder.presentation.correlation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.minder.presentation.components.CorrelationChart
import com.app.minder.presentation.components.MSurface
import com.app.minder.presentation.components.TopBar
import com.app.minder.presentation.theme.ButtonNeutral

@Composable
fun CorrelationScreen(
    viewModel: CorrelationViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.clearState()
    }

    val uiState by viewModel.uiState.collectAsState()
    val allItems = remember(uiState.measurementTypes, uiState.medications) {
        val measurements = uiState.measurementTypes.map {
            CorrelationItem.MeasurementItem(it.id, it.name)
        }
        val medications = uiState.medications.map {
            CorrelationItem.MedicationItem(it.id, it.name)
        }
        measurements + medications
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            MSurface(
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                TopBar(
                    onBack,
                    title = "Анализ зависимости"
                )

                Text(
                    text = "Выберите 2 показателя или лекарства для анализа зависимости",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        allItems.forEach { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.toggleItem(item)
                                    }
                            ) {
                                Checkbox(
                                    checked = uiState.selectedItems.contains(item),
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                if (uiState.selectedItems.size == 2 && uiState.series.size == 2) {
                    CorrelationChart(series = uiState.series)
                }

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Коэффициент\n" +
                                        "корреляции",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Text(
                                text = uiState.correlation?.let{ "%.1f".format(it) } ?: "--",
                                color = ButtonNeutral,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Text(
                            text = "Показывает зависимость параметров друг от друга.\n" +
                                    "0.0 - 0.3 - слабая связь\n" +
                                    "0.3 - 0.5 - умеренная\n" +
                                    "0.5 - 0.7 - заметная\n" +
                                    "0.7 - 0.9 - высокая\n" +
                                    "0.9 - 1.0 - очень высокая\n" +
                                    "отрицательные значения означают обратную связь - когда один параметр растет, другой снижается.\n" +
                                    "Корреляция не означает причинность и является только анализом статистики. Нельзя утверждать, что один параметр влияет на другой, можно только увидеть, что они изменяются согласованно. На результат может влиять малое количество данных или скрытые факторы.",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}
