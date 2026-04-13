package com.app.minder.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.minder.presentation.components.MButton
import com.app.minder.presentation.components.MSurface
import com.app.minder.presentation.components.MTextField

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLoginMode) "Вход" else "Регистрация",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        MSurface(
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {

            // Имя (только для регистрации)
            if (!isLoginMode) {
                MTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    label = "Имя пользователя",
                    showClearButton = true,
                    supportingText = "Это имя будут видеть другие пользователи",
                )
            }

            // Email
            MTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                showClearButton = true,
            )

            // Пароль
            MTextField(
                value = password,
                onValueChange = { password = it },
                label = "Пароль",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                showClearButton = true,
            )

            // Подтверждение пароля
            if (!isLoginMode) {
                MTextField(
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    label = "Повторите пароль",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    showClearButton = true,
                )
            }

            // Ошибка
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Кнопка входа/регистрации
            MButton(
                onClick = {
                    if (isLoginMode) {
                        viewModel.login(email, password)
                    } else {
                        viewModel.register(email, name, password, passwordConfirm)
                    }
                },
                modifier = Modifier.padding(top = 20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background,
                isLoading = uiState.isLoading,
                text = if (isLoginMode) "Войти" else "Зарегистрироваться"
            )

            // Переключение между входом и регистрацией
            MButton(
                onClick = {
                    isLoginMode = !isLoginMode
                    viewModel.clearError()
                },
                text = if (isLoginMode) "Зарегистрироваться" else "Войти",
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary

            )
        }
    }
}