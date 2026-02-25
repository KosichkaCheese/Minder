package com.app.minder.data.local.database

import com.app.minder.data.local.dao.MeasurementTypeDao
import com.app.minder.data.local.entity.MeasurementTypeEntity

object MeasurementTypeSeeder {
    private val measurementTypes = listOf<MeasurementTypeEntity>(
        MeasurementTypeEntity(
            id = "blood_pressure_systolic",
            name = "Артериальное давление(верхнее)",
            unit = "мм рт. ст.",
            lowerBound = 90.0,
            upperBound = 140.0
        ),
        MeasurementTypeEntity(
            id="blood_pressure_diastolic",
            name="Артериальное давление(нижнее)",
            unit = "мм рт. ст.",
            lowerBound = 60.0,
            upperBound = 90.0,
        ),
        MeasurementTypeEntity(
            id = "heart_rate",
            name = "Пульс",
            unit = "уд/мин",
            lowerBound = 50.0,
            upperBound = 110.0
        ),
        MeasurementTypeEntity(
            id = "blood_oxygen",
            name = "Кислород в крови",
            unit = "%",
            lowerBound = 95.0,
            upperBound = 100.0
        ),
        MeasurementTypeEntity(
            id = "temperature",
            name = "Температура тела",
            unit = "°C",
            lowerBound = 36.0,
            upperBound = 37.5
        ),
        MeasurementTypeEntity(
            id = "blood_glucose",
            name = "Глюкоза в крови",
            unit = "ммоль/л",
            lowerBound = 3.9,
            upperBound = 6.1
        ),
        MeasurementTypeEntity(
            id = "mood",
            name = "Настроение",
            unit = "балл",
            lowerBound = null,
            upperBound = null
        )
    )

    suspend fun seedIfEmpty(dao: MeasurementTypeDao){
        if (dao.getCount()==0){
            dao.insertAll(measurementTypes)
        }
    }
}