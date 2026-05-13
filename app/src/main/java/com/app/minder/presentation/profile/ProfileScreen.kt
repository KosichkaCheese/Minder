package com.app.minder.presentation.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.minder.data.remote.dto.UserResponse
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

    var selectedUser by remember { mutableStateOf<UserResponse?>(null) }
    var newProfileName by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
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
                if (uiState.isLinksLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                } else if (uiState.error != null && uiState.observers.isEmpty() && uiState.patients.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        MButton(
                            text = "Повторить",
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.background,
                            onClick = { viewModel.loadLinkedUsers() }
                        )
                    }
                } else {
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

                            if (uiState.observers.isEmpty()) {
                                Text(
                                    text = "Нет наблюдателей",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            } else {
                                uiState.observers.forEach { observer ->
                                    LinkedUserCard(
                                        name = observer.name,
                                        email = observer.email,
                                        onDelete = {
                                            selectedUser = observer
                                            showUnlinkObserver = true
                                        }
                                    )
                                }
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

                            if (uiState.patients.isEmpty()) {
                                Text(
                                    text = "Нет пациентов",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            } else {
                                uiState.patients.forEach { patient ->
                                    LinkedUserCard(
                                        name = patient.name,
                                        email = patient.email,
                                        onDelete = {
                                            selectedUser = patient
                                            showUnlinkPatient = true
                                        }
                                    )
                                }
                            }
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
                    onClick = {
                        viewModel.createInvitation()
                        showInvite = true
                              },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surfaceContainer,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                MButton(
                    text = "Принять\nприглашение",
                    onClick = {showAccept = true},
                    containerColor = PrimarySurface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    textStyle = MaterialTheme.typography.bodyLarge
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
                newProfileName = ""
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
                showLeave = false
                onLogout()
            },
            onDismiss = {showLeave = false}
        )
    }

    if (showUnlinkObserver) {
        selectedUser?.let { user ->
            PopupDialog(
                backgroundColor = MaterialTheme.colorScheme.tertiary,
                textColor = OnContainerError,
                submitColor = OnContainerError,
                dismissColor = TertiaryVariant,
                title = "Отвязать пользователя",
                text = "Вы уверены, что хотите отвязать наблюдаетля ${user.name} (${user.email})? Пользователь перестанет" +
                        " получать уведомления о Ваших пропусках лекарств.",
                onSubmit = {
                    viewModel.removeLink(
                        patientId = uiState.user?.id ?: "",
                        observerId = user.id,
                        onSuccess = { showUnlinkObserver = false }
                    )
                },
                onDismiss = { showUnlinkObserver = false }
            )
        }
    }

    if (showUnlinkPatient) {
        selectedUser?.let { user ->
            PopupDialog(
                backgroundColor = MaterialTheme.colorScheme.tertiary,
                textColor = OnContainerError,
                submitColor = OnContainerError,
                dismissColor = TertiaryVariant,
                title = "Отвязать пользователя",
                text = "Вы уверены, что хотите отвязать пациента ${user.name} (${user.email})? Вы перестанете" +
                        " получать уведомления о его пропусках лекарств.",
                onSubmit = {
                    viewModel.removeLink(
                        patientId = user.id,
                        observerId = uiState.user?.id ?: "",
                        onSuccess = { showUnlinkPatient = false }
                    )
                },
                onDismiss = { showUnlinkPatient = false }
            )
        }
    }

    if (showAccept) {
        PopupWithField(
            backgroundColor = Mint,
            textColor = onMint,
            submitColor = onMint,
            dismissColor = MintUnfocus,
            title = "Принять приглашение",
            text = "Введите код приглашения пользователя, чтобы стать его наблюдателем.\n" +
                    " Вы будете получать уведомления, когда пользователь пропускает прием\n" +
                    " лекарств",
            submitText = "Принять",
            onDismiss = {showAccept = false},
            onSubmit = {
                viewModel.acceptInvitation(code) {
                    code = ""
                    showAccept = false
                }
            },
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
                    "уведомления, когда Вы пропускаете прием лекарств.\n" +
                    " Код действителен 24 часа, после этого\n" +
                    " для принятия приглашения нужно будет получить новый.",
            value = if (uiState.isLoading) "Загрузка..." else uiState.inviteCode ?: "Ошибка",
            onDismiss = {showInvite = false}
        )
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

}