package com.app.minder.util.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import com.app.minder.presentation.components.MButton
import com.app.minder.presentation.theme.ButtonNeutral

@Composable
fun PermissionDialog(
    onResult: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                onResult(granted)
            }
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            icon = { Icon(Icons.Default.Notifications, null, tint= MaterialTheme.colorScheme.onSurface) },
            title = { Text(
                "Разрешить уведомления?",
                color = MaterialTheme.colorScheme.onSurface,
                style= MaterialTheme.typography.titleLarge
            ) },
            text = {
                Text(
                    text = "Приложение будет напоминать вам о приеме лекарств и пополнении запасов. ",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                MButton(
                    onClick = {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    text = "Разрешить",
                    containerColor = ButtonNeutral,
                    contentColor = MaterialTheme.colorScheme.background,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            },
            dismissButton = {
                MButton(
                    onClick = onDismiss,
                    text = "Не сейчас",
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = ButtonNeutral,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }
        )
    } else {
        LaunchedEffect(Unit) {
            onResult(true)
        }
    }
}