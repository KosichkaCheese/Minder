package com.app.minder.presentation.metrics


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.minder.R
import com.app.minder.presentation.theme.ButtonNeutral
import com.app.minder.presentation.theme.ContainerBackground
import com.app.minder.presentation.theme.OnPrimary
import com.app.minder.presentation.theme.Primary
import com.app.minder.presentation.theme.PrimarySurface
import com.app.minder.presentation.theme.Tertiary
import com.app.minder.presentation.theme.onTertiary

sealed class MeasurementIcon {
    data class Vector(val imageVector: ImageVector) : MeasurementIcon()
    data class Resource(val resId: Int) : MeasurementIcon()
}

data class MeasurementType(
    val title: String,
    val color: Color,
    val icon: MeasurementIcon,
    val iconColor: Color,
    val id: String
)

val measurementTypes = listOf(
    MeasurementType(
        "Настроение",
        PrimarySurface,
        MeasurementIcon.Vector(Icons.Default.EmojiEmotions),
        Primary,
        "mood"
    ),
    MeasurementType(
        "Пульс",
        onTertiary,
        MeasurementIcon.Resource(R.drawable.ic_heart_pulse),
        Tertiary,
        "heart_rate"
    ),
    MeasurementType(
        "Давление",
        Primary,
        MeasurementIcon.Resource(R.drawable.ic_ruler),
        OnPrimary,
        "blood_pressure_systolic"
    ),
    MeasurementType(
        "Кислород",
        ContainerBackground,
        MeasurementIcon.Vector(Icons.Default.BubbleChart),
        ButtonNeutral,
        "blood_oxygen"
    ),
    MeasurementType(
        "Температура",
        PrimarySurface,
        MeasurementIcon.Vector(Icons.Default.Thermostat),
        Primary,
        "temperature"
    ),
    MeasurementType(
        "Глюкоза",
        onTertiary,
        MeasurementIcon.Vector(Icons.Default.WaterDrop),
        Tertiary,
        "blood_glucose"
    )
)

@Composable
fun MetricsScreen(
    onSelect: (MeasurementType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(measurementTypes) { measurementType ->
                MeasurementCard(
                    measurementType = measurementType,
                    onClick = {onSelect(measurementType)}
                )
            }
        }
    }
}

@Composable
fun MeasurementCard(
    measurementType: MeasurementType,
    onClick: () -> Unit
){
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = measurementType.color,
        shape = RoundedCornerShape(25.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp,
                top = 44.dp, bottom = 44.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when(measurementType.icon){
                is MeasurementIcon.Vector ->
                    Icon(
                        imageVector = measurementType.icon.imageVector,
                        contentDescription = measurementType.title,
                        tint = measurementType.iconColor,
                        modifier = Modifier.size(100.dp)
                    )
                is MeasurementIcon.Resource ->
                    Icon(
                        painter = painterResource(measurementType.icon.resId),
                        contentDescription = measurementType.title,
                        tint = measurementType.iconColor,
                        modifier = Modifier.size(100.dp)
                    )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = measurementType.title,
                color = measurementType.iconColor,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}