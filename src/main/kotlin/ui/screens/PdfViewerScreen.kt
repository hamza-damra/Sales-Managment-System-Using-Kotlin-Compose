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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import ui.components.RTLProvider
import ui.theme.AppTheme
import utils.FileDialogUtils
import java.io.File
import java.awt.image.BufferedImage
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.toComposeImageBitmap

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
                    val image = renderer.renderImageWithDPI(page, 150f) // 150 DPI for good quality
                    images.add(image)
                }

                document.close()
                pdfImages = images
                isLoading = false

            } catch (e: Exception) {
                errorMessage = "خطأ في تحميل ملف PDF: ${e.message}"
                isLoading = false
            }
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
                                    // Page navigation (if multiple pages)
                                    if (pdfImages.size > 1) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
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
                                    }

                                    // PDF Image Display
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
                                                modifier = Modifier.padding(16.dp)
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Print button
                            OutlinedButton(
                                onClick = onPrint,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Print,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("طباعة")
                            }

                            // Save PDF button
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            // Generate default filename based on current PDF name
                                            val defaultFileName = pdfFile.nameWithoutExtension + "_copy.pdf"
                                            val selectedFile = FileDialogUtils.selectPdfSaveFile(defaultFileName)

                                            if (selectedFile != null) {
                                                // Copy the PDF file to the selected location
                                                pdfFile.copyTo(selectedFile, overwrite = true)

                                                // Optionally show success message or open the saved location
                                                try {
                                                    FileDialogUtils.openFolder(selectedFile.parentFile)
                                                } catch (e: Exception) {
                                                    // Ignore if can't open folder
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Handle error - could show error message in UI
                                            println("Error saving PDF: ${e.message}")
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("حفظ باسم")
                            }

                            // Open in system button
                            OutlinedButton(
                                onClick = {
                                    try {
                                        FileDialogUtils.openWithSystemDefault(pdfFile)
                                    } catch (e: Exception) {
                                        // Handle error
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("فتح خارجياً")
                            }
                        }
                    }
                }
            }
        }
    }
}
