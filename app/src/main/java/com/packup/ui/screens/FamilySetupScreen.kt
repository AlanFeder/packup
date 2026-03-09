package com.packup.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.packup.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.packup.viewmodel.FamilySetupState
import com.packup.viewmodel.FamilySetupViewModel

@Composable
fun FamilySetupScreen(
    viewModel: FamilySetupViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showJoin by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }
    val context = LocalContext.current

    when (val s = state) {
        is FamilySetupState.Created -> {
            CreatedView(
                familyId = s.familyId,
                onContinue = onSetupComplete,
            )
            return
        }
        is FamilySetupState.Joined -> {
            onSetupComplete()
            return
        }
        else -> {}
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AndroidView(
                factory = { ctx ->
                    android.widget.ImageView(ctx).apply {
                        setImageResource(R.mipmap.ic_launcher)
                        scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                    }
                },
                modifier = Modifier.size(72.dp),
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Pack Pal",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Sync your packing lists across devices",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            if (!showJoin) {
                Button(
                    onClick = { viewModel.createFamily() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is FamilySetupState.Loading,
                ) {
                    if (state is FamilySetupState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Icon(Icons.Outlined.GroupAdd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create New Family")
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showJoin = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is FamilySetupState.Loading,
                ) {
                    Icon(Icons.Outlined.Group, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Join Existing Family")
                }
            } else {
                OutlinedTextField(
                    value = joinCode,
                    onValueChange = { joinCode = it.uppercase().filter { c -> c.isLetterOrDigit() } },
                    label = { Text("Family Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.joinFamily(joinCode) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = joinCode.isNotBlank() && state !is FamilySetupState.Loading,
                ) {
                    if (state is FamilySetupState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Join Family")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        showJoin = false
                        joinCode = ""
                        viewModel.clearError()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Back")
                }
            }

            if (state is FamilySetupState.Error) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = (state as FamilySetupState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CreatedView(
    familyId: String,
    onContinue: () -> Unit,
) {
    val context = LocalContext.current

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.GroupAdd,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Family Created!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Share this code with your other devices:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = familyId,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Family Code", familyId))
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy")
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue to Pack Pal")
            }
        }
    }
}
