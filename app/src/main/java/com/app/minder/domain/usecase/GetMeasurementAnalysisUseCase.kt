package com.app.minder.domain.usecase

import com.app.minder.domain.interfaces.MeasurementRepository
import com.app.minder.domain.model.BLOOD_PRESSURE_DIASTOLIC
import com.app.minder.domain.model.BLOOD_PRESSURE_SYSTOLIC
import com.app.minder.domain.model.Measurement
import com.app.minder.domain.model.MeasurementAnalysis
import com.app.minder.domain.model.Trend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

data class Metrics (
    val mean: Double? = null,
    val trend: Trend? = null,
    val deviation: Double? = null,
    val stability: Double? = null,
    val timeInRange: Double? = null
)

class GetMeasurementAnalysisUseCase(
    private val measurementRep: MeasurementRepository
) {
    companion object{
        private const val DAYS_14 = 1000*60*60*24*14L
    }

    operator fun invoke(
        typeId: String,
        profileId: String,
        goalFlow: Flow<Double?>,
        goalSecondaryFlow: Flow<Double?> = flowOf(null)
    ): Flow<MeasurementAnalysis> {
        val end = System.currentTimeMillis()
        val start = end - DAYS_14

        return if (typeId==BLOOD_PRESSURE_SYSTOLIC){
            combine(
                measurementRep.getMeasurementsByRange(typeId, profileId, start),
                measurementRep.getMeasurementsByRange(BLOOD_PRESSURE_DIASTOLIC, profileId, start),
                goalFlow,
                goalSecondaryFlow
            ){ primary, secondary, goal, goalSecondary ->
                calculateAnalysis(primary, secondary, goal, goalSecondary)
            }
        } else {
            combine(
            measurementRep.getMeasurementsByRange(typeId, profileId, start),
                goalFlow
            ) {measurements, goal ->
                calculateAnalysis(measurements = measurements, goal = goal)
            }

        }
    }

    private fun calculateAnalysis(
        measurements: List<Measurement>,
        measurementsSecondary: List<Measurement>?=null,
        goal: Double? = null,
        goalSecondary: Double? = null
    ): MeasurementAnalysis {
        if (measurements.isEmpty()){
            return MeasurementAnalysis()
        }

        val metricsPrimary =calculateMetrics(measurements, goal)
        val metricsSecondary = measurementsSecondary?.let { calculateMetrics(it, goalSecondary) }

        return MeasurementAnalysis(
            mean = metricsPrimary.mean,
            meanSecondary = metricsSecondary?.mean,
            trend = metricsPrimary.trend,
            trendSecondary = metricsSecondary?.trend,
            deviation = metricsPrimary.deviation,
            deviationSecondary = metricsSecondary?.deviation,
            stability = metricsPrimary.stability,
            stabilitySecondary = metricsSecondary?.stability,
            timeInRange = metricsPrimary.timeInRange,
            timeInRangeSecondary = metricsSecondary?.timeInRange
        )

    }

    private fun calculateMetrics(
        measurements: List<Measurement>,
        goal: Double? = null,
    ): Metrics {
        val sorted = measurements.sortedBy { it.createdAt }
        val values = sorted.map{it.result}
        val n = values.size

        //среднее
        val mean = if (n>0) values.average() else 0.0

        //тенденция
        val trend = if (n>=2){
            val times = measurements.map{it.createdAt.toDouble()} //x (время)
            val tMean = times.average() //среднее x
            val yMean = mean //среднее y (значения)

            val numerator = times.zip(values).sumOf{ (t, y) -> (t - tMean) * (y - yMean) }
            val denominator = times.sumOf{ t -> (t - tMean) * (t - tMean) }

            if (denominator!=0.0){
                val tilt = numerator/denominator
                val tiltPerDay = tilt * 1000*60*60*24 //считали в миллисекундах, переводим в дни

                val normalTilt = tiltPerDay/mean //процент от среднего

                if (normalTilt>0.01) {
                    Trend.INCREASING
                } else if (normalTilt < -0.01) {
                    Trend.DECREASING
                } else {
                    Trend.STABLE
                }
            } else {
                Trend.STABLE
            }

        } else {
            Trend.STABLE
        }

        //отклонение от цели
        val deviationFromGoal = if (goal!=null && goal!=0.0){
            ((mean - goal)/goal)*100
        } else {
            0.0
        }

        //стабильность
        val stability = if (n>1 && mean!=0.0){
            val dispersion = values.sumOf{(it - mean)*(it - mean)}/(n-1) //выборочная дисперсия с поправкой Бесселя
            val standartDeviation = kotlin.math.sqrt(dispersion)
            (standartDeviation/kotlin.math.abs(mean))*100
        } else {
            0.0
        }

        //TiR
        val tir = if (goal != null && n>0){
            val lower = goal * 0.9
            val upper = goal * 1.1
            val countInRange = values.count { it in lower..upper }
            (countInRange.toDouble() / n) * 100
        } else {
            0.0
        }

        return Metrics(
            mean = mean,
            trend = trend,
            deviation = deviationFromGoal,
            stability = stability,
            timeInRange = tir
        )
    }
}