package com.template.project.feature.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.template.project.core.designsystem.components.AppButton
import com.template.project.core.designsystem.components.LoadingIndicator
import com.template.project.core.presentation.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreenRoot(
    viewModel: ProfileViewModel = koinViewModel(),
    onLogout: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ProfileEvent.LogoutSuccess -> onLogout()
            is ProfileEvent.ShowError -> { /* show snackbar */ }
        }
    }

    ProfileScreen(state = state, onAction = viewModel::onAction)
}

@Composable
private fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
) {
    if (state.isLoading) {
        LoadingIndicator()
        return
    }

    val user = state.user ?: return

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        AsyncImage(
            model = user.image,
            contentDescription = "Profile picture",
            modifier = Modifier.size(120.dp).clip(CircleShape),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${user.firstName} ${user.lastName}",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = user.email, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.weight(1f))

        AppButton(
            text = "Logout",
            onClick = { onAction(ProfileAction.OnLogoutClick) },
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
