package com.app.minder.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.minder.domain.model.IntakeStatus
import com.app.minder.domain.model.TodayIntake
import com.app.minder.presentation.components.MSurface
import com.app.minder.presentation.theme.Mint
import com.app.minder.presentation.theme.PrimarySurface

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onIntakeClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top=16.dp),
            colors = CardDefaults.cardColors(
                MaterialTheme.colorScheme.background
            )
        ) {
            Box(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { uiState.progressPercent / 100f },
                    modifier = Modifier.size(205.dp),
                    color = Mint,
                    trackColor = MaterialTheme.colorScheme.surfaceContainer,
                    strokeWidth = 20.dp
                )

                Text(
                    text = "${uiState.progressPercent}%",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        MSurface(
            color = MaterialTheme.colorScheme.surfaceContainer,

        ){
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                )
            } else if (uiState.intakes.isEmpty()) {
                Text(
                    text = "Нет запланированных приемов",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.intakes) { intake ->
                        IntakeCard(
                            intake = intake,
                            onClick = {onIntakeClick(intake.medication.id)}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IntakeCard(
    intake: TodayIntake,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().then(
            if (intake.canBeTaken) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = when (intake.status) {
                IntakeStatus.TAKEN -> PrimarySurface
                IntakeStatus.MISSED -> MaterialTheme.colorScheme.tertiary
                IntakeStatus.UPCOMING -> MaterialTheme.colorScheme.background
            }
        ),
        shape = RoundedCornerShape(30)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = intake.medication.name,
                style = MaterialTheme.typography.titleMedium,
                color = when (intake.status) {
                    IntakeStatus.TAKEN -> MaterialTheme.colorScheme.primary
                    IntakeStatus.MISSED -> MaterialTheme.colorScheme.onTertiary
                    IntakeStatus.UPCOMING -> MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 10.dp)
            )
            Text(
                text = intake.timeString,
                style = MaterialTheme.typography.titleMedium,
                color = when (intake.status) {
                    IntakeStatus.TAKEN -> MaterialTheme.colorScheme.primary
                    IntakeStatus.MISSED -> MaterialTheme.colorScheme.onTertiary
                    IntakeStatus.UPCOMING -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}