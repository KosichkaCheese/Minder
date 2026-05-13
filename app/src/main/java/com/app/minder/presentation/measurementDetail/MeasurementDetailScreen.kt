package com.app.minder.presentation.measurementDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.app.minder.domain.model.BLOOD_PRESSURE_DIASTOLIC
import com.app.minder.domain.model.BLOOD_PRESSURE_SYSTOLIC
import com.app.minder.domain.model.Trend
import com.app.minder.presentation.components.MSurface
import com.app.minder.presentation.components.MeasurementChart
import com.app.minder.presentation.components.MeasurementPopupType
import com.app.minder.presentation.components.SaveMeasurementPopup
import com.app.minder.presentation.components.TextFieldSmall
import com.app.minder.presentation.components.TopBar
import com.app.minder.presentation.theme.ButtonNeutral
import com.app.minder.presentation.theme.MintUnfocus
import com.app.minder.presentation.theme.TertiaryVariant

@Composable
fun MeasurementDetailScreen(
    viewModel: MeasurementDetailViewModel,
    measurementTypeId: String,
    onBack: () -> Unit
){
    val uiState by viewModel.uiState.collectAsState()
    val deviation = uiState.analysis?.deviation

    var isEditable by remember { mutableStateOf(false) }
    var targetValue by remember { mutableStateOf("") }
    var targetValue2 by remember { mutableStateOf("") }
    var showPopup by remember { mutableStateOf(false) }
    var measurementResult by remember { mutableStateOf("") }
    var measurementResult2 by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(uiState.target) {
        targetValue = uiState.target.toString()
    }
    LaunchedEffect(uiState.targetSecondary) {
        targetValue2 = uiState.targetSecondary.toString()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPopup = true },
                containerColor = MaterialTheme.colorScheme.onTertiary,
                contentColor = MaterialTheme.colorScheme.tertiary,
                elevation = FloatingActionButtonDefaults.elevation(2.dp, 0.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить"
                )
            }
        }
    ) { _ ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end=10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.loading) {
                item {
                    CircularProgressIndicator()
                }
            } else {

                item {
                    MSurface(
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        TopBar(
                            onBack,
                            title = if (measurementTypeId == BLOOD_PRESSURE_SYSTOLIC) "Артериальное давление"
                            else uiState.type?.name ?: ""
                        )

                        MeasurementChart(
                            measurements = uiState.measurements,
                            secondaryMeasurements = uiState.measurementsSecondary
                        )

                        Text(
                            modifier = Modifier.padding(start =10.dp, end = 10.dp),
                            text = "Приведенная ниже информация - статистический анализ, а не " +
                                    "медицинские рекомендации",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Container(
                            modifier = Modifier.padding(
                                start = 16.dp, end = 16.dp,
                                bottom = 16.dp, top = 0.dp
                            )
                        )
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Целевое значение",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                if (isEditable) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextFieldSmall(
                                            value = targetValue,
                                            onValueChange = { targetValue = it },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        )
                                        if (measurementTypeId == BLOOD_PRESSURE_SYSTOLIC) {
                                            Text(
                                                text = "/",
                                                color = ButtonNeutral,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                            TextFieldSmall(
                                                value = targetValue2,
                                                onValueChange = { targetValue2 = it },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = if (measurementTypeId == BLOOD_PRESSURE_SYSTOLIC) "${targetValue.toDoubleOrNull() ?: "-"}/${targetValue2.toDoubleOrNull() ?: "-"}"
                                        else targetValue.toDoubleOrNull()?.toString() ?:"-",
                                        color = ButtonNeutral,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }

                                Box {
                                    if (!isEditable) {
                                        IconButton(
                                            onClick = { isEditable = true },
                                            colors = IconButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.onSurface,
                                                disabledContainerColor = Color.Transparent,
                                                disabledContentColor = MaterialTheme.colorScheme.onSurface
                                            ),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Create,
                                                contentDescription = "Edit",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            onClick = {
                                                viewModel.saveGoal(measurementTypeId, targetValue.toDoubleOrNull() ?: return@IconButton)
                                                if (measurementTypeId == BLOOD_PRESSURE_SYSTOLIC) {
                                                    viewModel.saveGoal(
                                                        BLOOD_PRESSURE_DIASTOLIC,
                                                        targetValue2.toDoubleOrNull() ?: return@IconButton
                                                    )
                                                }
                                                isEditable = false
                                            },
                                            colors = IconButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.onSurface,
                                                disabledContainerColor = Color.Transparent,
                                                disabledContentColor = MaterialTheme.colorScheme.onSurface
                                            ),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Save",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Для разных людей \"нормальные\" значения могут быть разными," +
                                        " поэтому Вы можете изменить целевое значение",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Container(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Среднее значение",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    text = if (measurementTypeId == BLOOD_PRESSURE_SYSTOLIC) {
                                        "${uiState.analysis?.mean?.let { "%.1f".format(it) } ?: "-"}/${uiState.analysis?.meanSecondary?.let { "%.1f".format(it) } ?: "-"}"
                                    } else {
                                        uiState.analysis?.mean?.let { "%.1f".format(it) } ?: "-"
                                    },
                                    color = ButtonNeutral,
                                    style = MaterialTheme.typography.titleLarge
                                )

                            }
                        }

                        Container(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Тенденция",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    text = if (uiState.analysis?.trend == Trend.INCREASING) "повышение" else if (uiState.analysis?.trend == Trend.DECREASING) "снижение" else "стабильно",
                                    color = ButtonNeutral,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Icon(
                                    imageVector = if (uiState.analysis?.trend == Trend.INCREASING) Icons.Default.ArrowUpward else if (uiState.analysis?.trend == Trend.DECREASING) Icons.Default.ArrowDownward else Icons.Default.ArrowForward,
                                    contentDescription = "Arrow",
                                    modifier = Modifier.size(24.dp),
                                    tint = ButtonNeutral
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Показывает направление изменений. Считается относительно среднего значения",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Container(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Отклонение от цели",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    text = if (measurementTypeId == BLOOD_PRESSURE_SYSTOLIC) {
                                        "${deviation?.let { "%.1f%%".format(it) } ?: "-"}/${uiState.analysis?.deviationSecondary?.let { "%.1f%%".format(it) } ?: "-"}"
                                    } else {
                                        deviation?.let { "%+.1f%%".format(it) } ?: "-"
                                    },
                                    color = when {
                                        deviation == null -> ButtonNeutral
                                        kotlin.math.abs(deviation) > 5 -> TertiaryVariant
                                        else -> MintUnfocus
                                    },
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Показывает среднее отклонение от целевого показателя",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Container(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Стабильность",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    text = if (measurementTypeId == BLOOD_PRESSURE_SYSTOLIC) {
                                        (uiState.analysis?.stability?.let { "%.1f%%".format(it) } ?: "-") +"/"+ (uiState.analysis?.stabilitySecondary?.let { "%.1f%%".format(it) } ?: "-")
                                    } else {
                                        uiState.analysis?.stability?.let { "%.1f%%".format(it) } ?: "-"
                                    },
                                    color = when {
                                        uiState.analysis?.stability == null -> ButtonNeutral
                                        (uiState.analysis?.stability
                                            ?: 0.0) > 10.0 -> TertiaryVariant

                                        (uiState.analysis?.stability ?: 0.0) < 5.0 -> MintUnfocus
                                        else -> ButtonNeutral
                                    },
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Показывает, насколько сильно колеблется показатель относительно среднего значения\n" +
                                        "<5% - высокая стабильность\n" +
                                        "5-10% - средняя\n" +
                                        ">10% - низкая ",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Container(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TiR",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    text = if (measurementTypeId == BLOOD_PRESSURE_SYSTOLIC) {
                                        (uiState.analysis?.timeInRange?.let { "%.1f%%".format(it) } ?: "-")+"/"+(uiState.analysis?.timeInRangeSecondary?.let { "%.1f%%".format(it) } ?: "-")
                                    } else {
                                        uiState.analysis?.timeInRange?.let { "%.1f%%".format(it) } ?: "-"
                                    },
                                    color = when {
                                        uiState.analysis?.timeInRange == null -> ButtonNeutral
                                        (uiState.analysis?.timeInRange ?: 0.0) < 50.0 -> TertiaryVariant
                                        (uiState.analysis?.timeInRange ?: 0.0) > 50.0 -> MintUnfocus
                                        else -> ButtonNeutral
                                    },
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Показывает, какая часть измерений находится в целевом диапазоне",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPopup){
        when {
            measurementTypeId == BLOOD_PRESSURE_SYSTOLIC -> SaveMeasurementPopup(
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                textColor = MaterialTheme.colorScheme.onSurface,
                submitColor = ButtonNeutral,
                dismissColor = MaterialTheme.colorScheme.background,
                title = "Добавить измерение\n" +
                        "артетиального давления",
                text1 = "Систолическое\n" +
                        "(верхнее)",
                text2 = "Диастолическое\n" +
                        "(нижнее)",
                submitText = " Ок ",
                onDismiss = { showPopup = false },
                onSubmit = {
                                viewModel.saveMeasurement(measurementResult.toDoubleOrNull() ?: return@SaveMeasurementPopup, note, measurementResult2.toDoubleOrNull() ?: return@SaveMeasurementPopup)
                                showPopup = false
                           },
                value1 = measurementResult.toString(),
                value2 = measurementResult2.toString(),
                noteValue = note,
                onValue1Change = { measurementResult = it },
                onValue2Change = { measurementResult2 = it },
                onNoteChange = { note = it },
                unit = uiState.type?.unit ?: "",
                type = MeasurementPopupType.TWO_FIELDS
            );
            measurementTypeId == "mood" -> SaveMeasurementPopup(
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                textColor = MaterialTheme.colorScheme.onSurface,
                submitColor = ButtonNeutral,
                dismissColor = MaterialTheme.colorScheme.background,
                title = "Добавить измерение",
                text1 = uiState.type?.name ?: "",
                submitText = " Ок ",
                onDismiss = { showPopup = false },
                onSubmit = {
                                viewModel.saveMeasurement(measurementResult.toDoubleOrNull() ?: return@SaveMeasurementPopup, note)
                                showPopup = false
                           },
                value1 = measurementResult.toString(),
                noteValue = note,
                onValue1Change = { measurementResult = it },
                onNoteChange = { note = it },
                type = MeasurementPopupType.SLIDER
            );
            else -> SaveMeasurementPopup(
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                textColor = MaterialTheme.colorScheme.onSurface,
                submitColor = ButtonNeutral,
                dismissColor = MaterialTheme.colorScheme.background,
                title = "Добавить измерение",
                text1 = uiState.type?.name ?: "",
                submitText = " Ок ",
                onDismiss = { showPopup = false },
                onSubmit = {
                    viewModel.saveMeasurement(measurementResult.toDoubleOrNull() ?: return@SaveMeasurementPopup, note)
                    showPopup = false
                },
                value1 = measurementResult.toString(),
                noteValue = note,
                onValue1Change = { measurementResult = it },
                onNoteChange = { note = it },
                unit = uiState.type?.unit ?: "",
                type = MeasurementPopupType.ONE_FIELD
            );
        }
    }

}

@Composable
fun Container(
    modifier: Modifier,
    content: @Composable () -> Unit
){
    Surface(
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(20.dp),
    ){
        Column (
            modifier = modifier,
        )
        {
            content()
        }
    }
}