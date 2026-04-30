package com.app.minder.util.permissions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

object OSBasedRequestHelper {
    private val POWER_MANAGER_INTENTS = listOf(
        // Xiaomi
        Intent().setComponent(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        ),
        // Xiaomi MIUI 12+
        Intent().setComponent(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.powercenter.PowerSettings"
            )
        ),
        // Huawei
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        ),
        // Huawei (альтернативный)
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"
            )
        ),
        // Honor
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )
        )
    )

    fun needsManufacturerSettings(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return manufacturer in listOf(
            "xiaomi", "redmi", "poco",
            "huawei", "honor"
        )
    }

    fun getManufacturerName(): String {
        return Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
    }

    fun getManufacturerInstructions(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer in listOf("xiaomi", "redmi", "poco") ->
                "1. Настройки → Приложения → Управление приложениями → Minder\n" +
                        "2. Включите «Автозапуск»\n" +
                        "3. Приложения → Minder → Питание → «Без ограничений»"

            manufacturer in listOf("huawei", "honor") ->
                "1. Настройки → Приложения → Запуск приложений → Minder\n" +
                        "2. Отключите «Управлять автоматически»\n" +
                        "3. Включите все три переключателя"

            else ->
                "1. Настройки → Батарея → Minder\n" +
                        "2. Отключите оптимизацию батареи для приложения"
        }
    }

    fun openManufacturerSettings(context: Context): Boolean {
        for (intent in POWER_MANAGER_INTENTS) {
            try {
                if (context.packageManager.resolveActivity(
                        intent, 0
                    ) != null
                ) {
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    return true
                }
            } catch (_: Exception) {
                continue
            }
        }

        return try {
            context.startActivity(
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (_: Exception) {
            false
        }
    }
}