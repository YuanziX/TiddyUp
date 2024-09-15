package dev.yuanzix.tiddyup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            dismissOnBackPress = !isPermanentlyDeclined,
            dismissOnClickOutside = !isPermanentlyDeclined,
        )
    ) {
        Surface(
            modifier = Modifier.wrapContentSize(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Permission Required",
                    fontWeight = FontWeight.W600,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = permissionTextProvider.getPermissionText(isPermanentlyDeclined),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        if (isPermanentlyDeclined) {
                            onGoToAppSettingsClick()
                        } else {
                            onOkClick()
                        }
                    }
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .align(Alignment.End)) {
                    Text(
                        text = if (isPermanentlyDeclined) "Grant Permission" else "OK",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(2.dp),
                    )
                }
            }

        }
    }
}

interface PermissionTextProvider {
    fun getPermissionText(isPermanentlyDeclined: Boolean): String
}

class MediaPermissionTextProvider : PermissionTextProvider {
    override fun getPermissionText(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) "It seems you have permanently declined photo/video access permission. Please go to app settings and grant the permission"
        else "This app needs access to your images/videos to work properly"
    }
}