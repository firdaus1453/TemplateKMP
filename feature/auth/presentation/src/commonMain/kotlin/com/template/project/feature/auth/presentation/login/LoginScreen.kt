package com.template.project.feature.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.project.core.designsystem.components.AppButton
import com.template.project.core.designsystem.components.AppTextField
import com.template.project.core.designsystem.components.AppTextButton
import com.template.project.core.presentation.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreenRoot(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            LoginEvent.LoginSuccess -> onLoginSuccess()
            is LoginEvent.ShowError -> {
                // Show snackbar or toast with event.error.asString()
            }
        }
    }

    LoginScreen(
        state = state,
        onAction = { action ->
            when (action) {
                LoginAction.OnRegisterClick -> onRegisterClick()
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Sign in to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        AppTextField(
            value = state.username,
            onValueChange = { onAction(LoginAction.OnUsernameChanged(it)) },
            label = "Username",
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = state.password,
            onValueChange = { onAction(LoginAction.OnPasswordChanged(it)) },
            label = "Password",
            isPassword = true,
            isPasswordVisible = state.isPasswordVisible,
            trailingIcon = {
                IconButton(onClick = { onAction(LoginAction.OnTogglePasswordVisibility) }) {
                    Icon(
                        imageVector = if (state.isPasswordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (state.isPasswordVisible) {
                            "Hide password"
                        } else {
                            "Show password"
                        },
                    )
                }
            },
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            text = "Sign In",
            onClick = { onAction(LoginAction.OnLoginClick) },
            isLoading = state.isLoading,
            enabled = state.canLogin,
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextButton(
            text = "Don't have an account? Register",
            onClick = { onAction(LoginAction.OnRegisterClick) },
        )
    }
}
