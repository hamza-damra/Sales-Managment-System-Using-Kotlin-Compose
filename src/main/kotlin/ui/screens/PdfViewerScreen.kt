package ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import ui.components.RTLProvider
import ui.theme.AppTheme
import utils.FileDialogUtils
import java.awt.image.BufferedImage
import java.io.File

/**
 * Enhanced PDF Viewer Screen for displaying generated receipts
 * Provides viewing, printing, and downloading functionality with PDF rendering
 */
@Composable
fun PdfViewerDialog(
    pdfFile: File,
    onDismiss: () -> Unit,
    onPrint: () -> Unit = {},
    onDownload: () -> Unit = {}
) {
    var pdfImages by remember { mutableStateOf<List<BufferedImage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(0) }
    var zoomLevel by remember { mutableStateOf(1f) }
    var showSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var isPrinting by remember { mutableStateOf(false) }
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
                    val image = renderer.renderImageWithDPI(page, 200f) // Increased DPI for better quality
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
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "عارض الفواتير",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = pdfFile.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Divider()

                    // Content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        when {
                            isLoading -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        strokeWidth = 4.dp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "جاري تحميل الفاتورة...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            errorMessage != null -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = errorMessage!!,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                FileDialogUtils.openWithSystemDefault(pdfFile)
                                            } catch (e: Exception) {
                                                // Handle error
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("فتح في التطبيق الافتراضي")
                                    }
                                }
                            }

                            pdfImages.isNotEmpty() -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Enhanced navigation and zoom controls
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Page navigation (if multiple pages)
                                        if (pdfImages.size > 1) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                                    text = "صفحة ${currentPage + 1} من ${pdfImages.size}",
                                                    style = MaterialTheme.typography.bodyMedium
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
                                        } else {
                                            Spacer(modifier = Modifier.width(1.dp))
                                        }

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
                                                    contentDescription = "تصغير",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Text(
                                                text = "${(zoomLevel * 100).toInt()}%",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
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
                                                    contentDescription = "تكبير",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            // Reset zoom button
                                            IconButton(
                                                onClick = { zoomLevel = 1f }
                                            ) {
                                                Icon(
                                                    Icons.Default.CenterFocusStrong,
                                                    contentDescription = "إعادة تعيين التكبير",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Enhanced PDF Image Display with zoom
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
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
                                                    ),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons
                    if (!isLoading && errorMessage == null) {
                        Divider()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // First row of buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Enhanced Print button with loading state
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            isPrinting = true
                                            try {
                                                onPrint()
                                                showSuccessMessage = "تم إرسال الملف للطباعة بنجاح"
                                            } catch (e: Exception) {
                                                errorMessage = "خطأ في الطباعة: ${e.message}"
                                            } finally {
                                                isPrinting = false
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    enabled = !isPrinting && !isDownloading,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isPrinting) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                                    )
                                ) {
                                    if (isPrinting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Print,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isPrinting) "جاري الطباعة..." else "طباعة")
                                }

                                // Enhanced Save PDF button with loading state
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            isDownloading = true
                                            try {
                                                // Generate default filename based on current PDF name
                                                val defaultFileName = pdfFile.nameWithoutExtension + "_copy.pdf"
                                                val selectedFile = FileDialogUtils.selectPdfSaveFile(defaultFileName)

                                                if (selectedFile != null) {
                                                    // Copy the PDF file to the selected location
                                                    pdfFile.copyTo(selectedFile, overwrite = true)
                                                    showSuccessMessage = "تم حفظ الملف بنجاح في: ${selectedFile.name}"

                                                    // Open the saved location
                                                    try {
                                                        FileDialogUtils.openFolder(selectedFile.parentFile)
                                                    } catch (e: Exception) {
                                                        // Ignore if can't open folder
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = "خطأ في حفظ الملف: ${e.message}"
                                            } finally {
                                                isDownloading = false
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    enabled = !isDownloading && !isPrinting,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isDownloading) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                                    )
                                ) {
                                    if (isDownloading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Save,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isDownloading) "جاري الحفظ..." else "حفظ باسم")
                                }
                            }

                            // Second row of buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Open in system button
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val success = FileDialogUtils.openWithSystemDefault(pdfFile)
                                            if (success) {
                                                showSuccessMessage = "تم فتح الملف في التطبيق الافتراضي"
                                            } else {
                                                errorMessage = "لا يمكن فتح الملف. تأكد من وجود تطبيق لقراءة PDF"
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "خطأ في فتح الملف: ${e.message}"
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    enabled = !isDownloading && !isPrinting,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("فتح خارجياً")
                                }

                                // Close button
                                Button(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("إغلاق")
                                }
                            }
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
