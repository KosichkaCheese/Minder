package com.app.minder.presentation.medDetail

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ChipColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.minder.domain.model.MeasurementUnit
import com.app.minder.domain.model.Timing
import com.app.minder.presentation.components.MButton
import com.app.minder.presentation.components.MSurface
import com.app.minder.presentation.components.MTextField
import com.app.minder.presentation.components.TimePickerDialog
import com.app.minder.presentation.components.TopBar
import com.app.minder.presentation.theme.*
import com.app.minder.util.permissions.PermissionDialog

enum class MedScreenMode {
    VIEW,
    INTAKE,
    CREATE,
    EDIT
}

@Composable
fun MedDetailScreen(
    mode: MedScreenMode,
    viewModel: MedDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentProfile =uiState.currentProfile

    var inputError by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(MeasurementUnit.MG) }
    var showUnitMenu by remember { mutableStateOf(false) }
    var selectedTiming by remember { mutableStateOf(Timing.ANY) }
    var selectedDays by remember { mutableStateOf(emptySet<Int>()) }
    var selectedTimes by remember { mutableStateOf(emptyList<String>()) }
    var editTimeIndex by remember {mutableStateOf<Int?>(null)}
    var showTimePickerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.medication) {
        uiState.medication?.let { med ->
            name = med.name
            dosage = med.dosage.toString()
            stock = med.stock.toString()
            selectedUnit = med.unit
            selectedTiming = med.timing
            note = med.note ?: ""
        }
    }

    LaunchedEffect(uiState.schedules) {
        if (uiState.schedules.isNotEmpty()) {
            val days = uiState.schedules.mapNotNull { it.dayOfWeek }.toSet()
            val times = uiState.schedules.map { schedule ->
                val hours = schedule.timeMinutes / 60
                val minutes = schedule.timeMinutes % 60
                String.format("%02d:%02d", hours, minutes)
            }.distinct()

            selectedDays = days
            selectedTimes = times
        }
    }

    val isEditable = mode == MedScreenMode.CREATE || mode == MedScreenMode.EDIT

    fun saveMedication(){
        currentProfile?.let { profile ->
            viewModel.saveMedication(
                name = name,
                dosage = dosage.replace(",", ".").toDoubleOrNull()
                    ?: 0.0,
                unit = selectedUnit,
                stock = stock.replace(",", ".").toDoubleOrNull()
                    ?: 0.0,
                note = note,
                timing = selectedTiming,
                selectedDays = selectedDays,
                selectedTimes = selectedTimes
            )
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        TopBar(
            onBack
        )

        MSurface(
            color = PrimarySurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text(
                        text = "Название",
                        color = onPrimarySurface,
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (mode==MedScreenMode.VIEW) {
                        Box {
                            IconButton(
                                colors = IconButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = OnContainerError,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = OnContainerError
                                ),
                                onClick = {
                                    viewModel.deleteMedication()
                                    onBack()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    "Удалить",
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            IconButton(
                                colors = IconButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                onClick = onEdit,
                                modifier = Modifier.padding(start=40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Create,
                                    "Редактировать",
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                }
                if (isEditable){
                    MTextField(
                        value = name,
                        onValueChange = { name = it },
                        showClearButton = isEditable
                    )
                } else {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Text(
                    text = "Дозировка за прием",
                    color = onPrimarySurface,
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (isEditable) {
                        MTextField(
                            value = dosage,
                            showClearButton = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*[.,]?\\d*$"))) {
                                    dosage = newValue
                                }
                            },
                            modifier = Modifier.width(120.dp).height(60.dp)
                        )
                        Box {
                            TextButton(
                                onClick = { showUnitMenu = true },
                                colors = ButtonColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    contentColor = onPrimarySurface,
                                    disabledContainerColor = MaterialTheme.colorScheme.background,
                                    disabledContentColor = onPrimarySurface
                                )
                            ) {
                                Text(
                                    selectedUnit.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(
                                expanded = showUnitMenu,
                                onDismissRequest = { showUnitMenu = false },
                                containerColor = MaterialTheme.colorScheme.background
                            ) {
                                MeasurementUnit.entries.forEach { unit ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                unit.displayName,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        onClick = {
                                            selectedUnit = unit
                                            showUnitMenu = false
                                        },
                                        colors = MenuItemColors(
                                            textColor = onPrimarySurface,
                                            onPrimarySurface,
                                            onPrimarySurface,
                                            disabledTextColor = onPrimarySurface,
                                            onPrimarySurface,
                                            onPrimarySurface
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = dosage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = selectedUnit.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Text(
                    text = "Связь с приемами пищи",
                    style = MaterialTheme.typography.titleLarge,
                    color = onPrimarySurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Timing.entries.forEach { timing ->
                        Button(
                            onClick = { if (isEditable) selectedTiming = timing },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTiming == timing)
                                    onPrimarySurfaceSelection
                                else
                                    Color.Transparent
                            ),
                            modifier = Modifier.width(86.dp).height(135.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    painter = painterResource(timing.iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = null
                                )
                                Text(
                                    text = timing.displayName,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3
                                )

                            }
                        }
                    }

                }

                Text(
                    text = "Дни приема",
                    color = onPrimarySurface,
                    style = MaterialTheme.typography.titleLarge
                )
                Column{
                    listOf(
                        1 to "Понедельник",
                        2 to "Вторник",
                        3 to "Среда",
                        4 to "Четверг",
                        5 to "Пятница",
                        6 to "Суббота",
                        7 to "Воскресенье"
                    ).forEach { (day, name) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = day in selectedDays,
                                onCheckedChange = {
                                    if (isEditable) {
                                        selectedDays = if (it) {
                                            selectedDays + day
                                        } else {
                                            selectedDays - day
                                        }
                                    }
                                },
                                enabled = isEditable
                            )
                            Text(name, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                }

                Text(
                    text = "Время приема",
                    color = onPrimarySurface,
                    style = MaterialTheme.typography.titleLarge
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ){
                    selectedTimes.forEachIndexed { index, time ->
                        AssistChip(
                            onClick = {
                                if (isEditable) {
                                    editTimeIndex = index
                                    showTimePickerDialog = true
                                }
                            },
                            label = { Text(
                                    time,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 22.sp,
                                    )
                                    },
                            trailingIcon = if (isEditable) {
                                {
                                    IconButton(
                                        onClick = {
                                            selectedTimes =
                                                selectedTimes.filterIndexed { i, _ -> i != index }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            "Удалить",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else null,
                            enabled = isEditable,
                            colors = ChipColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                leadingIconContentColor = MaterialTheme.colorScheme.onSurface,
                                trailingIconContentColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.background,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                                disabledLeadingIconContentColor = MaterialTheme.colorScheme.onSurface,
                                disabledTrailingIconContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = null,
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                    if (isEditable) {
                        IconButton(
                            colors = IconButtonColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                disabledContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            onClick = {
                                editTimeIndex = null
                                showTimePickerDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Add, "Добавить")
                        }
                    }
                }

                Text(
                    text = "Текущий запас",
                    color = onPrimarySurface,
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    if (isEditable) {
                        MTextField(
                            value = stock,
                            showClearButton = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*[.,]?\\d*$"))) {
                                    stock = newValue
                                }
                            },
                            modifier = Modifier.width(120.dp).height(60.dp)
                        )
                    } else {
                        Text(
                            text = stock,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = selectedUnit.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Text(
                    text = "Заметка",
                    color = onPrimarySurface,
                    style = MaterialTheme.typography.titleLarge
                )
                if (isEditable) {
                    MTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = "Дополнительная информация",
                        modifier = Modifier.fillMaxWidth(),
                        showClearButton = isEditable,
                        singleLine = false
                    )
                } else {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                inputError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top=30.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                ){
                    when (mode) {
                        MedScreenMode.INTAKE -> {
                            MButton(
                                text = "Принять",
                                onClick = {
                                    viewModel.markAsTaken(onSuccess = onBack)
                                },
                                containerColor = onMint,
                                contentColor =PrimarySurface
                            )
                        }

                        MedScreenMode.CREATE, MedScreenMode.EDIT -> {
                            MButton(
                                text = "Отмена",
                                onClick = onBack,
                                containerColor = onPrimarySurfaceSelection,
                                contentColor = onMint
                            )
                            MButton(
                                text = "Сохранить",
                                onClick = {
                                    if (name==""){
                                        inputError = "Укажите название"
                                        return@MButton
                                    }
                                    if (dosage==""){
                                        inputError = "Укажите дозировку"
                                        return@MButton
                                    }
                                    if (selectedTimes.isEmpty()){
                                        inputError = "Выберите время приема"
                                        return@MButton
                                    }

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        showPermissionDialog = true
                                    } else {
                                        saveMedication()
                                    }
                                },
                                containerColor = onMint,
                                contentColor =PrimarySurface
                            )
                        }

                        else -> {}

                    }
                }
            }
        }
    }

    if (showTimePickerDialog){
        TimePickerDialog(
            onDismiss = { showTimePickerDialog=false },
            onConfirm = { hour, minute ->
                val newTime = String.format("%02d:%02d", hour, minute)
                selectedTimes = if (editTimeIndex != null) {
                    selectedTimes.toMutableList().apply {
                        set(editTimeIndex!!, newTime)
                    }
                } else {
                    selectedTimes + newTime
                }
                showTimePickerDialog = false
            }
        )
    }

    if (showPermissionDialog){
        PermissionDialog(
            onResult = { granted ->
                showPermissionDialog = false
                saveMedication()
            },
            onDismiss = {
                showPermissionDialog = false
                saveMedication()
            }
        )
    }
}