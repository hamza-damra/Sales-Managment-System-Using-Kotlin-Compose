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
}
