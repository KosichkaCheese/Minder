package com.app.minder.presentation.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class MeasurementPopupType{
    ONE_FIELD,
    TWO_FIELDS,
    SLIDER
}

@Composable
fun SaveMeasurementPopup(
    backgroundColor: Color,
    textColor: Color,
    submitColor: Color,
    dismissColor: Color,
    title: String,
    text1: String,
    text2: String="",
    submitText: String,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    value1: String,
    value2: String="",
    onValue1Change: (String) -> Unit,
    onValue2Change: (String) -> Unit = {},
    type: MeasurementPopupType,
    noteValue: String,
    onNoteChange: (String) -> Unit,
    unit: String = ""
){
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ){
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                if (type==MeasurementPopupType.ONE_FIELD || type== MeasurementPopupType.TWO_FIELDS) {

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = text1,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        OutlinedTextField(
                            value = value1,
                            { if (it.length <= 6) onValue1Change(it) },
                            modifier = Modifier
                                .width(80.dp)
                                .wrapContentHeight(),
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = textColor,
                                unfocusedBorderColor = textColor,
                                focusedLabelColor = textColor,
                                cursorColor = textColor,
                                unfocusedLabelColor = textColor,
                                unfocusedSupportingTextColor = textColor,
                                focusedSupportingTextColor = textColor,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            textAlign = TextAlign.Left
                        )
                    }

                    if (type == MeasurementPopupType.TWO_FIELDS) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = text2,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            OutlinedTextField(
                                value = value2,
                                onValueChange = { if (it.length <= 6) onValue2Change(it) },
                                modifier = Modifier
                                    .width(80.dp)
                                    .wrapContentHeight(),
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = textColor,
                                    unfocusedBorderColor = textColor,
                                    focusedLabelColor = textColor,
                                    cursorColor = textColor,
                                    unfocusedLabelColor = textColor,
                                    unfocusedSupportingTextColor = textColor,
                                    focusedSupportingTextColor = textColor,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                    focusedContainerColor = MaterialTheme.colorScheme.background,
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor,
                                textAlign = TextAlign.Left
                            )
                        }
                    }
                } else {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = text1,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = value1.toFloat(),
                        onValueChange = {onValue1Change(it.toString())},
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderColors(
                            activeTrackColor = textColor,
                            inactiveTrackColor = textColor,
                            activeTickColor = textColor,
                            inactiveTickColor = textColor,
                            thumbColor = textColor,
                            disabledThumbColor = textColor,
                            disabledActiveTrackColor = textColor,
                            disabledActiveTickColor = textColor,
                            disabledInactiveTickColor = textColor,
                            disabledInactiveTrackColor = textColor
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "%.2f".format(value1.toFloat()),
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    label = { Text("Заметка") },
                    value = noteValue,
                    onValueChange = {if (it.length <= 50) onNoteChange(it)},
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    trailingIcon = {
                        if (noteValue.isNotEmpty()) {
                            IconButton(onClick = { onNoteChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Очистить",
                                    tint = textColor
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = textColor,
                        unfocusedBorderColor = textColor,
                        focusedLabelColor = textColor,
                        cursorColor = textColor,
                        unfocusedLabelColor = textColor,
                        unfocusedSupportingTextColor = textColor,
                        focusedSupportingTextColor = textColor,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MButton(
                        text = "Отмена",
                        onClick = onDismiss,
                        containerColor = dismissColor,
                        contentColor = textColor,
                        textStyle = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                    MButton(
                        text = submitText,
                        onClick = onSubmit,
                        containerColor = submitColor,
                        contentColor = backgroundColor,
                        textStyle = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}