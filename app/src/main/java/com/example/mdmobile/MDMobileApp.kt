package com.example.mdmobile

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mdmobile.ui.screens.MainScreen
import com.example.mdmobile.ui.theme.MDMobileTheme
import com.example.mdmobile.ui.viewmodels.UserPreferencesViewModel
import com.example.mdmobile.utils.Permissions

@Composable
fun MDMobileApp() {
    val context = LocalContext.current
    val preferencesViewModel: UserPreferencesViewModel = viewModel(
        factory = UserPreferencesViewModel.provideFactory(context)
    )
    val userPreferences by preferencesViewModel.userPreferences.collectAsState()

    val hasPermission = Permissions.hasStoragePermission(context)

    MDMobileTheme(themeMode = userPreferences.themeMode) {
        if (!hasPermission) {
            PermissionRequestScreen(
                onPermissionGranted = {}
            )
        } else {
            MainScreen(
                userPreferences = userPreferences,
                onUpdateThemeMode = preferencesViewModel::updateThemeMode,
                onUpdateFontSize = preferencesViewModel::updateFontSize,
                onUpdateDefaultFolder = preferencesViewModel::updateDefaultFolder
            )
        }
    }
}

@Composable
fun PermissionRequestScreen(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.permission_required),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = stringResource(R.string.permission_explanation),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )
            Button(onClick = { Permissions.openStorageSettings(context) }) {
                Text(stringResource(R.string.open_settings))
            }
            Button(
                onClick = {
                    if (Permissions.hasStoragePermission(context)) {
                        onPermissionGranted()
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.permission_granted_check))
            }
        }
    } else {
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.all { it }) {
                onPermissionGranted()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.permission_required),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = stringResource(R.string.permission_explanation),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )
            Button(onClick = { permissionLauncher.launch(Permissions.getRequiredPermissions()) }) {
                Text(stringResource(R.string.grant_permission))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionRequestScreenPreview() {
    MDMobileTheme {
        PermissionRequestScreen(onPermissionGranted = {})
    }
}
