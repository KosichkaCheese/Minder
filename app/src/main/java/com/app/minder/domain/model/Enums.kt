package com.app.minder.domain.model

import androidx.annotation.DrawableRes
import com.app.minder.R

enum class MeasurementUnit(val displayName: String) {
    PILL("таб"),
    PIECE("шт"),
    MG("мг"),
    ML("мл"),
    G("г"),
    DROPS("капли"),
    TABLESPOON("ст.л."),
    TEASPOON("ч.л.")
}

enum class Timing(val displayName: String, @DrawableRes val iconRes: Int){
    BEFORE("до еды", R.drawable.before),
    DURING("во время еды", R.drawable.during),
    AFTER("после еды", R.drawable.after),
    ANY("не зависит от еды", R.drawable.any)
}