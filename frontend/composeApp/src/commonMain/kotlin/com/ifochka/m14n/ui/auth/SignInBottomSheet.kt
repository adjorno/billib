package com.ifochka.m14n.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInBottomSheet(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: SignInViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is SignInUiState.Success) {
            onSuccess()
            viewModel.resetState()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetState()
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Sign in to see full history",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { viewModel.signInWithGoogle() },
                enabled = uiState !is SignInUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue with Google")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.signInWithApple() },
                enabled = uiState !is SignInUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue with Apple")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  or  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )
            if (uiState is SignInUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (uiState as SignInUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.signInWithEmail(email = email, password = password) },
                enabled = uiState !is SignInUiState.Loading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState is SignInUiState.Loading) "Signing in…" else "Sign In / Register")
            }
        }
    }
}
