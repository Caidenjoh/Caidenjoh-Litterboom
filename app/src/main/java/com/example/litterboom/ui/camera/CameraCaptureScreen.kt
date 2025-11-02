package com.example.litterboom.ui.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * A Jetpack Compose screen that handles camera capture functionality.
 * It manages camera permissions, launches the native camera application,
 * and returns the URI of the captured image.
 *
 * @param onCaptured A callback function that is invoked with the [Uri] of the captured image upon success.
 * @param onClose A callback function that is invoked when the user cancels the camera operation or if an error occurs.
 */
@Composable
fun CameraCaptureScreen(
    onCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    // Get the current context, which is needed for permission checks and content resolution.
    val context = LocalContext.current

    // Determine if WRITE_EXTERNAL_STORAGE permission is needed (only for Android API level 28 and below).
    val needsWrite = Build.VERSION.SDK_INT <= 28
    // State to track if camera permission has been granted.
    var hasCamera by remember { mutableStateOf(false) }
    // State to track if write permission has been granted (defaults to true if not needed).
    var hasWrite by remember { mutableStateOf(!needsWrite) }

    // Create a launcher for requesting multiple permissions.
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        // Update permission states based on the user's response.
        hasCamera = res[Manifest.permission.CAMERA] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (needsWrite) {
            hasWrite = res[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Effect that runs once to check and request permissions if they haven't been granted yet.
    LaunchedEffect(Unit) {
        // Check current permission status.
        hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (needsWrite) {
            hasWrite = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        // If permissions are missing, launch the permission request.
        if (!hasCamera || !hasWrite) {
            val perms = mutableListOf(Manifest.permission.CAMERA)
            if (needsWrite) perms += Manifest.permission.WRITE_EXTERNAL_STORAGE
            permLauncher.launch(perms.toTypedArray())
        }
    }

    // A boolean flag indicating if all required permissions are granted.
    val permissionsGranted = hasCamera && hasWrite

    // State to hold the URI for the image file being created, to be used by the camera app.
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    // Create a launcher for the TakePicture activity. This is what opens the camera app.
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingUri
        // Clear the pending URI after the camera activity returns.
        pendingUri = null
        // If the picture was taken successfully and we have a URI.
        if (success && uri != null) {
            // Mark pending done on Q+ just in case
            if (Build.VERSION.SDK_INT >= 29) {
                runCatching {
                    context.contentResolver.update(
                        uri,
                        ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                        null,
                        null
                    )
                }
            }
            // Notify the caller with the captured image URI.
            onCaptured(uri)
        } else {
            // If capture failed or was cancelled, delete the temporary file and notify the caller to close.
            if (uri != null) {
                runCatching { context.contentResolver.delete(uri, null, null) }
            }
            onClose()
        }
    }

    // Effect that triggers once permissions are granted. It creates an image URI and launches the camera.
    LaunchedEffect(permissionsGranted, takePictureLauncher) {
        if (permissionsGranted) {
            val (created, uri) = createMediaStoreImageUri(context)
            if (!created || uri == null) {
                Toast.makeText(context, "Cannot create output file", Toast.LENGTH_SHORT).show()
                onClose()
                return@LaunchedEffect
            }
            // Store the created URI and launch the camera.
            pendingUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    // UI to display when permissions have not yet been granted.
     if (!permissionsGranted) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission required")
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = true)
                        ) {
                            // Manually trigger the permission request again if the user clicks "Grant".
                            val perms = mutableListOf(Manifest.permission.CAMERA)
                            if (needsWrite) perms += Manifest.permission.WRITE_EXTERNAL_STORAGE
                            permLauncher.launch(perms.toTypedArray())
                        }
                ) {
                    Text("Grant")
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = true)
                        ) { onClose() }
                ) { Text("Cancel") }
            }
        }
        return
    }

    // UI to display while the camera is being launched.
    Box(Modifier.fillMaxSize()) {
        IconButton(
            onClick = {
                // Handle back press: delete any pending file and call the close callback.
                pendingUri?.let { runCatching { context.contentResolver.delete(it, null, null) } }
                pendingUri = null
                onClose()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Opening camera…")
        }
    }
}

/**
 * Creates a new image file entry in the MediaStore.
 * This method handles differences between Android versions (specifically for API 29+ using Scoped Storage).
 *
 * @param context The application context.
 * @param directory The subdirectory within the "Pictures" directory where the image will be saved (e.g., "LitterBoom").
 * @return A [Pair] containing a [Boolean] indicating success and the resulting [Uri] if successful.
 */
private fun createMediaStoreImageUri(
    context: Context,
    directory: String = "LitterBoom"
): Pair<Boolean, Uri?> {
    // Check if running on Android Q (API 29) or higher.
    val isQPlus = Build.VERSION.SDK_INT >= 29
    // Generate a unique file name based on the current timestamp.
    val name = "waste_${System.currentTimeMillis()}.jpg"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (isQPlus) { // For Android Q+, use RELATIVE_PATH and set IS_PENDING.
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$directory")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        put(MediaStore.Images.Media.TITLE, name)
    }
    val resolver = context.contentResolver
    // Determine the correct content URI based on the Android version.
    val collection: Uri =
        if (isQPlus) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    // Insert the new image entry into the MediaStore.
    val uri = runCatching { resolver.insert(collection, values) }.getOrNull()
    // Return a pair indicating success and the created URI.
    return (uri != null) to uri
}
