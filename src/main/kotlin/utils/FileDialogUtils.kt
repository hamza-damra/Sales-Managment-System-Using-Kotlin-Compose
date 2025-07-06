package utils

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Centralized utility for file dialog operations
 */
object FileDialogUtils {
    
    private fun getCurrentTimestamp(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    }
    
    /**
     * Show save dialog for Excel files
     */
    fun selectExcelSaveFile(defaultFileName: String? = null): File? {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "حفظ ملف Excel"
        fileChooser.selectedFile = File(defaultFileName ?: "products_export_${getCurrentTimestamp()}.xlsx")
        fileChooser.fileFilter = FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx")
        
        return if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            if (!file.name.endsWith(".xlsx")) {
                File(file.absolutePath + ".xlsx")
            } else {
                file
            }
        } else {
            null
        }
    }
    
    /**
     * Show save dialog for CSV files
     */
    fun selectCsvSaveFile(defaultFileName: String? = null): File? {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "حفظ ملف CSV"
        fileChooser.selectedFile = File(defaultFileName ?: "products_export_${getCurrentTimestamp()}.csv")
        fileChooser.fileFilter = FileNameExtensionFilter("CSV Files (*.csv)", "csv")
        
        return if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            if (!file.name.endsWith(".csv")) {
                File(file.absolutePath + ".csv")
            } else {
                file
            }
        } else {
            null
        }
    }
    
    /**
     * Show save dialog for JSON files
     */
    fun selectJsonSaveFile(defaultFileName: String? = null): File? {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "حفظ ملف JSON"
        fileChooser.selectedFile = File(defaultFileName ?: "products_export_${getCurrentTimestamp()}.json")
        fileChooser.fileFilter = FileNameExtensionFilter("JSON Files (*.json)", "json")
        
        return if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            if (!file.name.endsWith(".json")) {
                File(file.absolutePath + ".json")
            } else {
                file
            }
        } else {
            null
        }
    }
    
    /**
     * Show open dialog for import files (CSV and JSON)
     */
    fun selectImportFile(): File? {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "اختيار ملف للاستيراد"
        fileChooser.fileFilter = FileNameExtensionFilter("Import Files (*.csv, *.json)", "csv", "json")
        fileChooser.addChoosableFileFilter(FileNameExtensionFilter("CSV Files (*.csv)", "csv"))
        fileChooser.addChoosableFileFilter(FileNameExtensionFilter("JSON Files (*.json)", "json"))
        
        return if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile
        } else {
            null
        }
    }
    
    /**
     * Get file extension
     */
    fun getFileExtension(file: File): String {
        return file.extension.lowercase()
    }
    
    /**
     * Validate if file is supported for import
     */
    fun isValidImportFile(file: File): Boolean {
        val extension = getFileExtension(file)
        return extension in listOf("csv", "json") && file.exists() && file.canRead()
    }

    /**
     * Show save dialog for PDF files
     */
    fun selectPdfSaveFile(defaultFileName: String? = null): File? {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "حفظ فاتورة PDF"
        fileChooser.selectedFile = File(defaultFileName ?: "receipt_${getCurrentTimestamp()}.pdf")
        fileChooser.fileFilter = FileNameExtensionFilter("PDF Files (*.pdf)", "pdf")

        return if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            if (!file.name.endsWith(".pdf")) {
                File(file.absolutePath + ".pdf")
            } else {
                file
            }
        } else {
            null
        }
    }

    /**
     * Open file with system default application
     */
    fun openWithSystemDefault(file: File): Boolean {
        return try {
            if (java.awt.Desktop.isDesktopSupported()) {
                val desktop = java.awt.Desktop.getDesktop()
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(file)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Print file using system default printer with fallback options
     */
    fun printFile(file: File): PrintResult {
        return try {
            if (java.awt.Desktop.isDesktopSupported()) {
                val desktop = java.awt.Desktop.getDesktop()
                if (desktop.isSupported(java.awt.Desktop.Action.PRINT)) {
                    desktop.print(file)
                    PrintResult.Success
                } else {
                    PrintResult.NotSupported("طباعة الملفات غير مدعومة على هذا النظام")
                }
            } else {
                PrintResult.NotSupported("سطح المكتب غير مدعوم")
            }
        } catch (e: java.io.IOException) {
            if (e.message?.contains("No application is associated") == true) {
                PrintResult.NoAssociatedApp("لا يوجد تطبيق مرتبط بملفات PDF. يرجى تثبيت قارئ PDF مثل Adobe Reader أو فتح الملف يدوياً للطباعة.")
            } else {
                PrintResult.Error("خطأ في الطباعة: ${e.message}")
            }
        } catch (e: Exception) {
            PrintResult.Error("خطأ غير متوقع: ${e.message}")
        }
    }

    /**
     * Result of print operation
     */
    sealed class PrintResult {
        object Success : PrintResult()
        data class Error(val message: String) : PrintResult()
        data class NotSupported(val message: String) : PrintResult()
        data class NoAssociatedApp(val message: String) : PrintResult()
    }

    /**
     * Get file size in human readable format
     */
    fun getFileSizeString(file: File): String {
        if (!file.exists()) return "0 B"

        val bytes = file.length()
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return "%.1f %s".format(size, units[unitIndex])
    }

    /**
     * Open folder in system file explorer
     */
    fun openFolder(folder: File): Boolean {
        return try {
            if (java.awt.Desktop.isDesktopSupported()) {
                val desktop = java.awt.Desktop.getDesktop()
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(folder)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
