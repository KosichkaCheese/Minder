package com.app.minder.data.local.converters

import androidx.room.TypeConverter
import com.app.minder.domain.model.Unit

class UnitConverter {
    @TypeConverter
    fun fromUnit (unit: Unit): String = unit.name

    @TypeConverter
    fun toUnit (value: String): Unit = Unit.valueOf(value)
}