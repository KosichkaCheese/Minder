package com.app.minder.presentation.home

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
import androidx.compose.ui.unit.dp
import com.app.minder.domain.model.IntakeStatus
import com.app.minder.domain.model.TodayIntake
import com.app.minder.presentation.components.MSurface
import com.app.minder.presentation.theme.Mint

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.intakes) { intake ->
                        IntakeCard(
                            intake = intake
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IntakeCard(
    intake: TodayIntake
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (intake.status) {
                IntakeStatus.TAKEN -> MaterialTheme.colorScheme.primaryContainer
                IntakeStatus.MISSED -> MaterialTheme.colorScheme.errorContainer
                IntakeStatus.UPCOMING -> MaterialTheme.colorScheme.surface
            }
        )
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
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}