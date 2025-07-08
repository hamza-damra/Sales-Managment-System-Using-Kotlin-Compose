package ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import ui.components.RTLProvider
import ui.theme.AppTheme
import utils.FileDialogUtils
import java.awt.image.BufferedImage
import java.io.File

/**
 * Full-screen PDF viewer with enhanced navigation and features
 * Provides comprehensive PDF viewing experience with zoom, navigation, and file management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerFullScreen(
    pdfFile: File,
    onBack: () -> Unit
) {
    var pdfImages by remember { mutableStateOf<List<BufferedImage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(0) }
    var zoomLevel by remember { mutableStateOf(1f) }
    var showSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var isPrinting by remember { mutableStateOf(false) }
    var showToolbar by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Load PDF pages as images
    LaunchedEffect(pdfFile) {
        coroutineScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val document = PDDocument.load(pdfFile)
                val renderer = PDFRenderer(document)
                val images = mutableListOf<BufferedImage>()

                for (page in 0 until document.numberOfPages) {
                    val image = renderer.renderImageWithDPI(page, 200f)
                    images.add(image)
                }

                document.close()
                pdfImages = images
                isLoading = false

            } catch (e: Exception) {
                errorMessage = "خطأ في تحميل ملف PDF: ${e.message}"
                isLoading = false
                println("PDF loading error: ${e.printStackTrace()}")
            }
        }
    }

    // Auto-dismiss success message
    LaunchedEffect(showSuccessMessage) {
        showSuccessMessage?.let {
            kotlinx.coroutines.delay(3000)
            showSuccessMessage = null
        }
    }

    RTLProvider {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top toolbar
                AnimatedVisibility(
                    visible = showToolbar,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = pdfFile.nameWithoutExtension,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (pdfImages.isNotEmpty()) {
                                    Text(
                                        text = "صفحة ${currentPage + 1} من ${pdfImages.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "رجوع"
                                )
                            }
                        },
                        actions = {
                            // Zoom controls
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { 
                                        if (zoomLevel > 0.5f) zoomLevel = (zoomLevel - 0.25f).coerceAtLeast(0.5f)
                                    },
                                    enabled = zoomLevel > 0.5f
                                ) {
                                    Icon(
                                        Icons.Default.ZoomOut,
                                        contentDescription = "تصغير"
                                    )
                                }

                                Text(
                                    text = "${(zoomLevel * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.widthIn(min = 40.dp),
                                    textAlign = TextAlign.Center
                                )

                                IconButton(
                                    onClick = { 
                                        if (zoomLevel < 3f) zoomLevel = (zoomLevel + 0.25f).coerceAtMost(3f)
                                    },
                                    enabled = zoomLevel < 3f
                                ) {
                                    Icon(
                                        Icons.Default.ZoomIn,
                                        contentDescription = "تكبير"
                                    )
                                }

                                IconButton(
                                    onClick = { zoomLevel = 1f }
                                ) {
                                    Icon(
                                        Icons.Default.CenterFocusStrong,
                                        contentDescription = "إعادة تعيين التكبير"
                                    )
                                }
                            }

                            // Action buttons
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isPrinting = true
                                        try {
                                            val printResult = FileDialogUtils.printFile(pdfFile)
                                            when (printResult) {
                                                is FileDialogUtils.PrintResult.Success -> {
                                                    showSuccessMessage = "تم إرسال الملف للطباعة بنجاح"
                                                }
                                                is FileDialogUtils.PrintResult.NoAssociatedApp,
                                                is FileDialogUtils.PrintResult.NotSupported,
                                                is FileDialogUtils.PrintResult.Error -> {
                                                    FileDialogUtils.openWithSystemDefault(pdfFile)
                                                    showSuccessMessage = "تم فتح الملف للطباعة اليدوية"
                                                }
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "خطأ في الطباعة: ${e.message}"
                                        } finally {
                                            isPrinting = false
                                        }
                                    }
                                },
                                enabled = !isPrinting && !isDownloading
                            ) {
                                if (isPrinting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Print,
                                        contentDescription = "طباعة"
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    // Use a simple approach to avoid coroutine conflicts
                                    try {
                                        val defaultFileName = pdfFile.nameWithoutExtension + "_copy.pdf"
                                        val selectedFile = FileDialogUtils.selectPdfSaveFile(defaultFileName)

                                        if (selectedFile != null) {
                                            isDownloading = true
                                            // Perform file operations in a background thread
                                            Thread {
                                                try {
                                                    pdfFile.copyTo(selectedFile, overwrite = true)

                                                    // Update UI on main thread
                                                    kotlinx.coroutines.runBlocking {
                                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                            showSuccessMessage = "تم حفظ الملف بنجاح"
                                                            isDownloading = false
                                                        }
                                                    }

                                                    try {
                                                        FileDialogUtils.openFolder(selectedFile.parentFile)
                                                    } catch (e: Exception) {
                                                        // Ignore if can't open folder
                                                    }
                                                } catch (e: Exception) {
                                                    kotlinx.coroutines.runBlocking {
                                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                            errorMessage = "خطأ في حفظ الملف: ${e.message}"
                                                            isDownloading = false
                                                        }
                                                    }
                                                }
                                            }.start()
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "خطأ في حفظ الملف: ${e.message}"
                                        isDownloading = false
                                    }
                                },
                                enabled = !isDownloading && !isPrinting
                            ) {
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = "تحميل"
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showToolbar = !showToolbar }
                            ) {
                                Icon(
                                    if (showToolbar) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showToolbar) "إخفاء الأدوات" else "إظهار الأدوات"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    )
                }

                // PDF content area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showToolbar = !showToolbar }
                ) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        strokeWidth = 4.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "جاري تحميل ملف PDF...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        errorMessage != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = AppTheme.colors.error.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Error,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = AppTheme.colors.error
                                        )
                                        Text(
                                            text = errorMessage!!,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = AppTheme.colors.error,
                                            textAlign = TextAlign.Center
                                        )
                                        Button(
                                            onClick = onBack,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AppTheme.colors.error
                                            )
                                        ) {
                                            Text("رجوع", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        pdfImages.isNotEmpty() -> {
                            // PDF display with enhanced scrolling
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Gray.copy(alpha = 0.1f))
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState()),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentPage < pdfImages.size) {
                                    val bufferedImage = pdfImages[currentPage]
                                    val composeImage = bufferedImage.toComposeImageBitmap()

                                    Image(
                                        bitmap = composeImage,
                                        contentDescription = "PDF Page ${currentPage + 1}",
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .graphicsLayer(
                                                scaleX = zoomLevel,
                                                scaleY = zoomLevel
                                            )
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom navigation for multiple pages
            if (pdfImages.size > 1 && showToolbar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (currentPage > 0) currentPage-- },
                                enabled = currentPage > 0
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "الصفحة السابقة"
                                )
                            }

                            Text(
                                text = "${currentPage + 1} / ${pdfImages.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            IconButton(
                                onClick = { if (currentPage < pdfImages.size - 1) currentPage++ },
                                enabled = currentPage < pdfImages.size - 1
                            ) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "الصفحة التالية"
                                )
                            }
                        }
                    }
                }
            }

            // Success message overlay
            showSuccessMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { showSuccessMessage = null },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = AppTheme.colors.success
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
