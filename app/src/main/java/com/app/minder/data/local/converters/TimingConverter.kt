package com.app.minder.data.local.converters

import androidx.room.TypeConverter
import com.app.minder.domain.model.Timing

class TimingConverter {
    @TypeConverter
    fun fromTiming(timing: Timing): String=timing.name

    @TypeConverter
    fun toTiming(value: String): Timing = Timing.valueOf(value)
}