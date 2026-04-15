package com.app.minder.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.minder.presentation.components.MButton
import com.app.minder.presentation.components.MSurface
import com.app.minder.presentation.components.PopupWithField
import com.app.minder.presentation.components.ProfileCarousel
import com.app.minder.presentation.components.LinkedUserCard
import com.app.minder.presentation.components.PopupDialog
import com.app.minder.presentation.components.PopupWithCopy
import com.app.minder.presentation.theme.Mint
import com.app.minder.presentation.theme.MintUnfocus
import com.app.minder.presentation.theme.OnContainerError
import com.app.minder.presentation.theme.PrimarySurface
import com.app.minder.presentation.theme.TertiaryVariant
import com.app.minder.presentation.theme.onMint
import com.app.minder.presentation.theme.onTertiaryVariant

data class LinkedUser(
    val name: String,
    val email: String
)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var showCreateProfile by remember { mutableStateOf(false) }
    var showLeave by remember { mutableStateOf(false) }
    var showUnlinkObserver by remember { mutableStateOf(false) }
    var showUnlinkPatient by remember { mutableStateOf(false) }
    var showAccept by remember { mutableStateOf(false) }
    var showInvite by remember { mutableStateOf(false) }

    var linkedName by remember { mutableStateOf("") }
    var linkedEmail by remember { mutableStateOf("") }
    var newProfileName by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var inviteCode = "C6G2KL"

    val observerList = listOf(
        LinkedUser("Observer user", "observer@example.com"),
        LinkedUser("Another observer user", "another_observer@example.com"),
        LinkedUser("Another observer user", "another_observer@example.com")
    )
    val patientList = listOf(
        LinkedUser("Patient user", "patient@example.com"),
        LinkedUser("Another patient user", "another_patient@example.com")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize().padding(start = 16.dp, bottom = 0.dp, end = 16.dp, top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MSurface(
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Активный пользователь",
                        tint = onMint,
                        modifier = Modifier.size(50.dp)
                    )
                    Column(
                        modifier = Modifier.width(240.dp)
                    ) {
                        Text(
                            text = uiState.user?.name ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            color = onMint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = uiState.user?.email ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = onMint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(
                        onClick = {showLeave = true},
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Выйти из аккаунта",
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(35.dp)
                        )
                    }

                }
            }
        }

        item {
            MSurface(
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Text(
                    text = "Ваши профили",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineLarge
                )

                ProfileCarousel(
                    profiles = uiState.profiles,
                    currentProfile = uiState.currentProfile,
                    onProfileSelected = { profile ->
                        viewModel.switchProfile(profile.id)
                    }
                )

                OutlinedButton(
                    onClick = { showCreateProfile = true },
                    border = null
                ) {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Добавить профиль",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            }
        }

        item {
            MSurface(
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.width(165.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Наблюдатели",
                            color = onTertiaryVariant,
                            style = MaterialTheme.typography.titleLarge
                        )

                        observerList.forEach { observer ->
                            LinkedUserCard(
                                name = observer.name,
                                email = observer.email,
                                onDelete = {
                                    linkedName = observer.name
                                    linkedEmail = observer.email
                                    showUnlinkObserver = true
                                }
                            )
                        }

                    }

                    Column(
                        modifier = Modifier.width(165.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Пациенты",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )

                        patientList.forEach { patient ->
                            LinkedUserCard(
                                name = patient.name,
                                email = patient.email,
                                onDelete = {
                                    linkedName = patient.name
                                    linkedEmail = patient.email
                                    showUnlinkPatient = true
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MButton(
                    text = "Пригласить\nнаблюдателя",
                    onClick = {showInvite = true},
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surfaceContainer,
                )
                MButton(
                    text = "Принять\nприглашение",
                    onClick = {showAccept = true},
                    containerColor = PrimarySurface,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            }
        }

        item{}
    }

    if (showCreateProfile) {
        PopupWithField(
            backgroundColor = Mint,
            textColor = onMint,
            submitColor = onMint,
            dismissColor = MintUnfocus,
            title = "Создать профиль",
            text = "Введите имя для нового локального профиля.\n" +
                    "Профили имеют отдельные лекарства и истории измерений. Вы можете использовать их для управления лечением разных членов семьи.",
            submitText = "Создать",
            onDismiss = {showCreateProfile = false},
            onSubmit = {
                viewModel.createProfile(newProfileName)
                showCreateProfile = false
            },
            value = newProfileName,
            onValueChange = {newProfileName = it}
        )
    }

    if (showLeave) {
        PopupDialog(
            backgroundColor = MaterialTheme.colorScheme.tertiary,
            textColor = OnContainerError,
            submitColor = OnContainerError,
            dismissColor = TertiaryVariant,
            title = "Выйти",
            text = "Вы уверены, что хотите выйти из аккаунта? Вы можете потерять историю приемов и измерений," +
            " а также несинхронизированные данные.",
            onSubmit = {
                viewModel.logout()
                onLogout()
            },
            onDismiss = {showLeave = false}
        )
    }

    if (showUnlinkObserver) {
        PopupDialog(
            backgroundColor = MaterialTheme.colorScheme.tertiary,
            textColor = OnContainerError,
            submitColor = OnContainerError,
            dismissColor = TertiaryVariant,
            title = "Отвязать пользователя",
            text = "Вы уверены, что хотите отвязать наблюдаетля $linkedName ($linkedEmail)? Пользователь перестанет" +
            " получать уведомления о Ваших пропусках лекарств и измерениях.",
            onSubmit = {},
            onDismiss = {showUnlinkObserver = false}
        )
    }

    if (showUnlinkPatient) {
        PopupDialog(
            backgroundColor = MaterialTheme.colorScheme.tertiary,
            textColor = OnContainerError,
            submitColor = OnContainerError,
            dismissColor = TertiaryVariant,
            title = "Отвязать пользователя",
            text = "Вы уверены, что хотите отвязать пациента $linkedName ($linkedEmail)? Вы перестанете" +
                    " получать уведомления о его пропусках лекарств и измерениях.",
            onSubmit = {},
            onDismiss = {showUnlinkPatient = false}
        )
    }

    if (showAccept) {
        PopupWithField(
            backgroundColor = Mint,
            textColor = onMint,
            submitColor = onMint,
            dismissColor = MintUnfocus,
            title = "Принять приглашение",
            text = "Введите код приглашения пользователья, чтобы стать его наблюдателем.\n" +
                    " Вы будете получать уведомления, когда пользователь пропускает прием\n" +
                    " лекарств или его результаты измерений становятся опасными.",
            submitText = "Принять",
            onDismiss = {showAccept = false},
            onSubmit = {},
            value = code,
            onValueChange = {code = it}
        )
    }

    if (showInvite) {
        PopupWithCopy(
            backgroundColor = Mint,
            textColor = onMint,
            submitColor = onMint,
            dismissColor = MintUnfocus,
            title = "Пригласить наблюдателя",
            text = "Сообщите этот код другому пользователю, чтобы он смог получать \n" +
                    "уведомления, когда Вы пропускаете прием лекарств или Ваши результаты\n" +
                    " измерений становятся опасными. Код действителен 24 часа, после этого\n" +
                    " для принятия приглашения нужно будет получить новый.",
            value = inviteCode,
            onDismiss = {showInvite = false}
        )
    }

}