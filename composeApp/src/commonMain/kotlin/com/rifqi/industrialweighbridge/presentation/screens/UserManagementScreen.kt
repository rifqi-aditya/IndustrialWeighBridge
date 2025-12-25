package com.rifqi.industrialweighbridge.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.rifqi.industrialweighbridge.db.UserRole
import com.rifqi.industrialweighbridge.domain.model.User
import com.rifqi.industrialweighbridge.domain.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** User Management Screen - Admin only. Allows adding and deleting users. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen() {
    val authRepository = koinInject<AuthRepository>()
    val scope = rememberCoroutineScope()

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<User?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load users
    LaunchedEffect(Unit) { users = authRepository.getAllUsers() }

    // Add User Dialog
    if (showAddDialog) {
        AddUserDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { username, password, role ->
                    scope.launch {
                        val success = authRepository.createUser(username, password, role)
                        if (success) {
                            users = authRepository.getAllUsers()
                            showAddDialog = false
                            errorMessage = null
                        } else {
                            errorMessage = "Username sudah ada"
                        }
                    }
                },
                errorMessage = errorMessage
        )
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { userToDelete ->
        AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Hapus Pengguna") },
                text = {
                    Text("Apakah Anda yakin ingin menghapus pengguna '${userToDelete.username}'?")
                },
                confirmButton = {
                    Button(
                            onClick = {
                                scope.launch {
                                    authRepository.deleteUser(userToDelete.id)
                                    users = authRepository.getAllUsers()
                                    showDeleteDialog = null
                                }
                            },
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                    )
                    ) { Text("Hapus") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) { Text("Batal") }
                }
        )
    }

    Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary
                ) { Icon(Icons.Default.Add, contentDescription = "Tambah Pengguna") }
            }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text(
                    text = "Kelola Pengguna",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 24.dp)
            )

            if (users.isEmpty()) {
                Text(
                        text = "Belum ada pengguna",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(users, key = { it.id }) { user ->
                        UserCard(user = user, onDelete = { showDeleteDialog = user })
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCard(user: User, onDelete: () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                            text = user.role.name,
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                    if (user.isAdmin) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Don't allow deleting admin with ID 1 (default admin)
            if (user.id != 1L) {
                IconButton(onClick = onDelete) {
                    Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddUserDialog(
        onDismiss: () -> Unit,
        onConfirm: (username: String, password: String, role: UserRole) -> Unit,
        errorMessage: String?
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.OPERATOR) }
    var roleExpanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Tambah Pengguna Baru") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation =
                                    if (passwordVisible) VisualTransformation.None
                                    else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                            imageVector =
                                                    if (passwordVisible) Icons.Default.VisibilityOff
                                                    else Icons.Default.Visibility,
                                            contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Konfirmasi Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Role Dropdown
                    ExposedDropdownMenuBox(
                            expanded = roleExpanded,
                            onExpandedChange = { roleExpanded = !roleExpanded }
                    ) {
                        OutlinedTextField(
                                value = selectedRole.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Role") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = roleExpanded
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                                expanded = roleExpanded,
                                onDismissRequest = { roleExpanded = false }
                        ) {
                            UserRole.entries.forEach { role ->
                                DropdownMenuItem(
                                        text = { Text(role.name) },
                                        onClick = {
                                            selectedRole = role
                                            roleExpanded = false
                                        }
                                )
                            }
                        }
                    }

                    // Error messages
                    val displayError = localError ?: errorMessage
                    if (displayError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                text = displayError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            // Validate
                            when {
                                username.isBlank() -> localError = "Username tidak boleh kosong"
                                password.length < 6 -> localError = "Password minimal 6 karakter"
                                password != confirmPassword -> localError = "Password tidak sama"
                                else -> {
                                    localError = null
                                    onConfirm(username, password, selectedRole)
                                }
                            }
                        }
                ) { Text("Tambah") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
