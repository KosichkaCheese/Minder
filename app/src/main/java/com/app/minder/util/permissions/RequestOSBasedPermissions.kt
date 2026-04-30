package com.app.minder.util.permissions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.app.minder.presentation.components.PopupDialog
import com.app.minder.presentation.theme.ButtonNeutral

@Composable
fun RequestOSBasedPermissions(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val manufacturer = OSBasedRequestHelper.getManufacturerName()
    val instructions = OSBasedRequestHelper.getManufacturerInstructions()

    PopupDialog(
        onSubmit = {
            OSBasedRequestHelper.openManufacturerSettings(context)
            onDismiss()
        },
        onDismiss = {
           onDismiss()
        },
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
        textColor = MaterialTheme.colorScheme.onSurface,
        dismissColor = MaterialTheme.colorScheme.background,
        submitColor = ButtonNeutral,
        title = "Настройте уведомления",
        text = "На устройствах $manufacturer система может блокировать " +
                "фоновую работу приложения. Из-за этого напоминания могут не приходить вовремя.\n" +
                "Как это предотвратить:\n" +
                instructions
    )

}