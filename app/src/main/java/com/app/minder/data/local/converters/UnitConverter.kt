package com.app.minder.data.local.converters

import androidx.room.TypeConverter
import com.app.minder.domain.model.MeasurementUnit

class UnitConverter {
    @TypeConverter
    fun fromUnit (unit: MeasurementUnit): String = unit.name

    @TypeConverter
    fun toUnit (value: String): MeasurementUnit = MeasurementUnit.valueOf(value)
}