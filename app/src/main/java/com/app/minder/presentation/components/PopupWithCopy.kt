package com.app.minder.presentation.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PopupWithCopy(
    backgroundColor: Color,
    textColor: Color,
    submitColor: Color,
    dismissColor: Color,
    title: String,
    text: String,
    value: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ){
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
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

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
                    MButton(
                        text = "Копировать",
                        onClick = {
                            val clip = ClipData.newPlainText("code", value)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
                        },
                        containerColor = submitColor,
                        contentColor = backgroundColor,
                        textStyle = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }

    }
}