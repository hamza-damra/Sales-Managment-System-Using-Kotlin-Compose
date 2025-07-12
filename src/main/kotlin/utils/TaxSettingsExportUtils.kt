package utils

import data.preferences.TaxSettings
import data.preferences.TaxPreferencesManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Utility for exporting and importing tax settings
 * Supports JSON format for easy backup and restore
 */
object TaxSettingsExportUtils {
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    @Serializable
    data class TaxSettingsExport(
        val version: String = "1.0",
        val exportDate: String,
        val taxSettings: TaxSettingsData,
        val metadata: ExportMetadata
    )
    
    @Serializable
    data class TaxSettingsData(
        val taxRate: Double,
        val showTaxBreakdownInCart: Boolean,
        val showTaxOnReceipts: Boolean,
        val displayTaxInclusivePricing: Boolean,
        val calculateTaxOnDiscountedAmount: Boolean
    )
    
    @Serializable
    data class ExportMetadata(
        val applicationName: String = "نظام إدارة المبيعات",
        val applicationVersion: String = "1.0.0",
        val exportedBy: String = "System",
        val notes: String = ""
    )
    
    /**
     * Export tax settings to JSON file
     */
    fun exportTaxSettings(
        taxSettings: TaxSettings,
        filePath: String,
        notes: String = ""
    ): Result<String> {
        return try {
            val exportData = TaxSettingsExport(
                exportDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                taxSettings = TaxSettingsData(
                    taxRate = taxSettings.taxRate,
                    showTaxBreakdownInCart = taxSettings.showTaxBreakdownInCart,
                    showTaxOnReceipts = taxSettings.showTaxOnReceipts,
                    displayTaxInclusivePricing = taxSettings.displayTaxInclusivePricing,
                    calculateTaxOnDiscountedAmount = taxSettings.calculateTaxOnDiscountedAmount
                ),
                metadata = ExportMetadata(notes = notes)
            )
            
            val jsonString = json.encodeToString(exportData)
            File(filePath).writeText(jsonString)
            
            Result.success("تم تصدير إعدادات الضريبة بنجاح إلى: $filePath")
        } catch (e: Exception) {
            Result.failure(Exception("فشل في تصدير إعدادات الضريبة: ${e.message}"))
        }
    }
    
    /**
     * Import tax settings from JSON file
     */
    fun importTaxSettings(filePath: String): Result<TaxSettings> {
        return try {
            val jsonString = File(filePath).readText()
            val exportData = json.decodeFromString<TaxSettingsExport>(jsonString)
            
            val taxSettings = TaxSettings(
                taxRate = exportData.taxSettings.taxRate,
                showTaxBreakdownInCart = exportData.taxSettings.showTaxBreakdownInCart,
                showTaxOnReceipts = exportData.taxSettings.showTaxOnReceipts,
                displayTaxInclusivePricing = exportData.taxSettings.displayTaxInclusivePricing,
                calculateTaxOnDiscountedAmount = exportData.taxSettings.calculateTaxOnDiscountedAmount
            )
            
            // Validate imported settings
            val taxManager = TaxPreferencesManager()
            if (taxManager.isValidTaxRate(taxSettings.taxRate)) {
                Result.success(taxSettings)
            } else {
                Result.failure(Exception("معدل الضريبة في الملف المستورد غير صحيح: ${taxSettings.taxRate}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("فشل في استيراد إعدادات الضريبة: ${e.message}"))
        }
    }
    
    /**
     * Create backup of current tax settings
     */
    fun createTaxSettingsBackup(
        taxPreferencesManager: TaxPreferencesManager,
        backupDirectory: String = "backups"
    ): Result<String> {
        return try {
            val currentSettings = taxPreferencesManager.loadTaxSettings()
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val backupFileName = "tax_settings_backup_$timestamp.json"
            
            // Create backup directory if it doesn't exist
            val backupDir = File(backupDirectory)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val backupFilePath = File(backupDir, backupFileName).absolutePath
            exportTaxSettings(
                taxSettings = currentSettings,
                filePath = backupFilePath,
                notes = "نسخة احتياطية تلقائية"
            )
        } catch (e: Exception) {
            Result.failure(Exception("فشل في إنشاء النسخة الاحتياطية: ${e.message}"))
        }
    }
    
    /**
     * Restore tax settings from backup
     */
    fun restoreTaxSettingsFromBackup(
        backupFilePath: String,
        taxPreferencesManager: TaxPreferencesManager
    ): Result<String> {
        return try {
            val importResult = importTaxSettings(backupFilePath)
            
            when {
                importResult.isSuccess -> {
                    val taxSettings = importResult.getOrThrow()
                    // Create backup of current settings before restore
                    createTaxSettingsBackup(taxPreferencesManager, "pre_restore_backups")
                    
                    // Apply imported settings
                    kotlinx.coroutines.runBlocking {
                        taxPreferencesManager.saveTaxSettings(taxSettings)
                    }
                    
                    Result.success("تم استعادة إعدادات الضريبة بنجاح من: $backupFilePath")
                }
                else -> {
                    Result.failure(importResult.exceptionOrNull() ?: Exception("فشل في استيراد الإعدادات"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("فشل في استعادة إعدادات الضريبة: ${e.message}"))
        }
    }
    
    /**
     * Get list of available backup files
     */
    fun getAvailableBackups(backupDirectory: String = "backups"): List<BackupInfo> {
        return try {
            val backupDir = File(backupDirectory)
            if (!backupDir.exists()) {
                emptyList()
            } else {
                backupDir.listFiles { file ->
                    file.isFile && file.name.startsWith("tax_settings_backup_") && file.name.endsWith(".json")
                }?.map { file ->
                    BackupInfo(
                        fileName = file.name,
                        filePath = file.absolutePath,
                        fileSize = file.length(),
                        lastModified = file.lastModified(),
                        isValid = validateBackupFile(file.absolutePath)
                    )
                }?.sortedByDescending { it.lastModified } ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Validate backup file
     */
    private fun validateBackupFile(filePath: String): Boolean {
        return try {
            importTaxSettings(filePath).isSuccess
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Backup file information
     */
    data class BackupInfo(
        val fileName: String,
        val filePath: String,
        val fileSize: Long,
        val lastModified: Long,
        val isValid: Boolean
    )
    
    /**
     * Generate default tax settings for different regions
     */
    fun getRegionalTaxSettings(): Map<String, TaxSettings> {
        return mapOf(
            "السعودية" to TaxSettings(
                taxRate = 0.15, // 15% VAT
                showTaxBreakdownInCart = true,
                showTaxOnReceipts = true,
                displayTaxInclusivePricing = false,
                calculateTaxOnDiscountedAmount = true
            ),
            "الإمارات" to TaxSettings(
                taxRate = 0.05, // 5% VAT
                showTaxBreakdownInCart = true,
                showTaxOnReceipts = true,
                displayTaxInclusivePricing = false,
                calculateTaxOnDiscountedAmount = true
            ),
            "مصر" to TaxSettings(
                taxRate = 0.14, // 14% VAT
                showTaxBreakdownInCart = true,
                showTaxOnReceipts = true,
                displayTaxInclusivePricing = false,
                calculateTaxOnDiscountedAmount = true
            ),
            "بدون ضريبة" to TaxSettings(
                taxRate = 0.0, // 0% VAT
                showTaxBreakdownInCart = false,
                showTaxOnReceipts = false,
                displayTaxInclusivePricing = false,
                calculateTaxOnDiscountedAmount = true
            )
        )
    }
}
