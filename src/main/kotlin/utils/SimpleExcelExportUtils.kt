package utils

import data.api.ProductDTO
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Simplified Excel export utility that exports as CSV with .xlsx extension
 * This avoids POI dependency conflicts while providing Excel-compatible output
 */
object SimpleExcelExportUtils {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Export products list to Excel-compatible CSV format
     */
    fun exportProductsList(
        products: List<ProductDTO>,
        fileName: String? = null
    ): Boolean {
        return try {
            // Get file location
            val file = selectSaveFile(fileName ?: "products_export_${getCurrentTimestamp()}.xlsx")
            if (file == null) {
                return false
            }

            // Use CSV export but save with .xlsx extension for Excel compatibility
            val csvFile = if (file.name.endsWith(".xlsx")) {
                File(file.parent, file.nameWithoutExtension + ".csv")
            } else {
                file
            }

            // Export as CSV
            val success = ProductImportUtils.exportProductsToCsv(products, csvFile)

            if (success && csvFile != file) {
                // Rename CSV to Excel extension if needed
                try {
                    csvFile.renameTo(file)
                } catch (e: Exception) {
                    println("⚠️ Could not rename to .xlsx, saved as CSV: ${csvFile.name}")
                }
            }

            success
        } catch (e: Exception) {
            println("❌ Simple Excel export failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * File selection dialog
     */
    private fun selectSaveFile(defaultFileName: String): File? {
        return FileDialogUtils.selectExcelSaveFile(defaultFileName)
    }

    /**
     * Get current timestamp for file naming
     */
    private fun getCurrentTimestamp(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    }
}
