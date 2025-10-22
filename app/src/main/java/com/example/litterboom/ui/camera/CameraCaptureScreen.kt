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
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun CameraCaptureScreen(
    onCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // === Runtime permissions ===
    val needsWrite = Build.VERSION.SDK_INT <= 28
    var hasCamera by remember { mutableStateOf(false) }
    var hasWrite by remember { mutableStateOf(!needsWrite) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        hasCamera = res[Manifest.permission.CAMERA] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (needsWrite) {
            hasWrite = res[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    LaunchedEffect(Unit) {
        hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        if (needsWrite) {
            hasWrite = ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (!hasCamera || !hasWrite) {
            val perms = mutableListOf(Manifest.permission.CAMERA)
            if (needsWrite) perms += Manifest.permission.WRITE_EXTERNAL_STORAGE
            permLauncher.launch(perms.toTypedArray())
        }
    }

    val permissionsGranted = hasCamera && hasWrite
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
                            val perms = mutableListOf(Manifest.permission.CAMERA)
                            if (needsWrite) perms += Manifest.permission.WRITE_EXTERNAL_STORAGE
                            permLauncher.launch(perms.toTypedArray())
                        }
                ) { Text("Grant") }
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

    // === Camera controller ===
    val controller = remember(context) {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
        }
    }

    // Rebind whenever permissions become granted or lifecycle changes
    LaunchedEffect(permissionsGranted, lifecycleOwner) {
        if (permissionsGranted) controller.bindToLifecycle(lifecycleOwner)
    }

    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    Box(Modifier.fillMaxSize()) {
        // Preview wired to controller (no setSurfaceProvider with controller)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx -> PreviewView(ctx).apply { this.controller = controller } }
        )

        // Back arrow
        IconButton(
            onClick = onClose,
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

        // White circular shutter
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .size(76.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false, radius = 36.dp)
                ) {
                    val (uri, output) = createMediaStoreOutput(context)
                    if (output == null || uri == null) {
                        Toast.makeText(context, "Cannot create output file", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }
                    controller.takePicture(
                        output,
                        mainExecutor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onError(exception: ImageCaptureException) {
                                runCatching { context.contentResolver.delete(uri, null, null) }
                                Toast.makeText(context, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                            override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                                // finalize pending on Q+ just in case
                                if (Build.VERSION.SDK_INT >= 29) {
                                    runCatching {
                                        context.contentResolver.update(
                                            uri,
                                            ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                                            null, null
                                        )
                                    }
                                }
                                onCaptured(results.savedUri ?: uri)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White, CircleShape)
                    .border(3.dp, Color.White.copy(alpha = 0.9f), CircleShape)
            )
        }
    }
}

private fun createMediaStoreOutput(
    context: Context,
    directory: String = "LitterBoom"
): Pair<Uri?, ImageCapture.OutputFileOptions?> {
    val isQPlus = Build.VERSION.SDK_INT >= 29
    val name = "waste_${System.currentTimeMillis()}.jpg"

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (isQPlus) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$directory")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        put(MediaStore.Images.Media.TITLE, name)
    }

    val resolver = context.contentResolver
    val collection: Uri =
        if (isQPlus) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val uri = runCatching { resolver.insert(collection, values) }.getOrNull() ?: return null to null
    val output = ImageCapture.OutputFileOptions.Builder(resolver, uri, ContentValues()).build()
    return uri to output
}
