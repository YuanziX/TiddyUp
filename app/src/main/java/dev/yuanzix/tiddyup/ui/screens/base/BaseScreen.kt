package dev.yuanzix.tiddyup.ui.screens.base

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yuanzix.tiddyup.ui.components.MediaPermissionTextProvider
import dev.yuanzix.tiddyup.ui.components.PermissionDialog
import dev.yuanzix.tiddyup.ui.viewmodels.PermissionsViewModel
import kotlin.reflect.KFunction1

@Composable
fun BaseScreen(
    viewModel: PermissionsViewModel = viewModel<PermissionsViewModel>(),
    openAppSettings: () -> Unit,
    shouldShowRequestPermissionRationale: KFunction1<String, Boolean>,
    onNavigateToHome: () -> Unit,
) {
    val dialogQueue = viewModel.visiblePermissionDialogQueue
    var allPermissionsGranted by remember { mutableStateOf(false) }

    val mediaPermissionResultLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { perms ->
                var granted = true
                permissions.forEach { permission ->
                    val isGranted = perms[permission] == true
                    viewModel.onPermissionResult(permission = permission, isGranted = isGranted)
                    if (!isGranted) {
                        granted = false
                    }
                }
                allPermissionsGranted = granted
            })

    LaunchedEffect(true) {
        mediaPermissionResultLauncher.launch(permissions)
    }

    // Block navigation until all permissions are granted
    LaunchedEffect(allPermissionsGranted) {
        if (allPermissionsGranted) {
            onNavigateToHome()
        }
    }

    dialogQueue.reversed().forEach {
        PermissionDialog(
            permissionTextProvider = when (it) {
                Manifest.permission.READ_MEDIA_VIDEO -> MediaPermissionTextProvider()
                Manifest.permission.READ_MEDIA_IMAGES -> MediaPermissionTextProvider()
                else -> return@forEach
            },
            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(it),
            onDismiss = viewModel::dismissDialog,
            onOkClick = {
                viewModel.dismissDialog()
                mediaPermissionResultLauncher.launch(permissions)
            },
            onGoToAppSettingsClick = {
                viewModel.dismissDialog()
                openAppSettings()
            }

        )
    }

    return Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

val permissions = if (Build.VERSION.SDK_INT >= 33) {
    arrayOf(
//        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_IMAGES,
    )
} else {
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}
