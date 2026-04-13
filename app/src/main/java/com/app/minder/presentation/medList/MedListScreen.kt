package com.app.minder.presentation.medList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.minder.domain.model.Medication
import com.app.minder.presentation.components.MSurface
import com.app.minder.presentation.components.TopBar

@Composable
fun MedListScreen(
    viewModel: MedListViewModel,
    onBack: () -> Unit,
    onMedicationClick: (String) -> Unit
){
    val uiState by viewModel.uiState.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize().padding(start = 16.dp, bottom = 0.dp, end = 16.dp, top = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ){
        TopBar(
            onBack
        )

        MSurface(
            color = MaterialTheme.colorScheme.surfaceContainer
        ){
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                )
            } else if (uiState.medications.isEmpty()) {
                Text(
                    text = "Нет лекарств",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.medications){ medication ->
                        MedicationCard(
                            medication = medication,
                            onClick = {onMedicationClick(medication.id)}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationCard(
    medication: Medication,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick=onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
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
                text = medication.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 10.dp)
            )
        }
    }
}
