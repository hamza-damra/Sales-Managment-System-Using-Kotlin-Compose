package data.preferences

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ui.theme.ThemeMode
import java.util.prefs.Preferences

/**
 * Manages theme preferences with persistent storage using Java Preferences API
 * Follows the same pattern as TokenManager for consistency
 */
class ThemePreferencesManager {
    private val prefs = Preferences.userNodeForPackage(ThemePreferencesManager::class.java)
    private val mutex = Mutex()
    
    companion object {
        private const val THEME_MODE_KEY = "theme_mode"
        private const val DEFAULT_THEME = "SYSTEM"
    }
    
    private var _currentTheme: ThemeMode? = null
    
    init {
        loadThemeFromStorage()
    }
    
    /**
     * Save theme preference to persistent storage
     */
    suspend fun saveTheme(themeMode: ThemeMode) = mutex.withLock {
        try {
            _currentTheme = themeMode
            prefs.put(THEME_MODE_KEY, themeMode.name)
            println("✅ ThemePreferencesManager - Theme saved: ${themeMode.name}")
        } catch (e: Exception) {
            println("❌ ThemePreferencesManager - Failed to save theme: ${e.message}")
        }
    }
    
    /**
     * Load theme preference from persistent storage
     */
    fun loadTheme(): ThemeMode {
        return _currentTheme ?: run {
            loadThemeFromStorage()
            _currentTheme ?: ThemeMode.SYSTEM
        }
    }
    
    /**
     * Get current theme without loading from storage
     */
    fun getCurrentTheme(): ThemeMode {
        return _currentTheme ?: ThemeMode.SYSTEM
    }
    
    /**
     * Check if theme preference exists in storage
     */
    fun hasThemePreference(): Boolean {
        return prefs.get(THEME_MODE_KEY, null) != null
    }
    
    /**
     * Clear theme preference from storage (reset to default)
     */
    suspend fun clearTheme() = mutex.withLock {
        try {
            _currentTheme = null
            prefs.remove(THEME_MODE_KEY)
            println("✅ ThemePreferencesManager - Theme preference cleared")
        } catch (e: Exception) {
            println("❌ ThemePreferencesManager - Failed to clear theme: ${e.message}")
        }
    }
    
    /**
     * Load theme from persistent storage
     */
    private fun loadThemeFromStorage() {
        try {
            val savedTheme = prefs.get(THEME_MODE_KEY, DEFAULT_THEME)
            _currentTheme = try {
                ThemeMode.valueOf(savedTheme)
            } catch (e: IllegalArgumentException) {
                println("⚠️ ThemePreferencesManager - Invalid theme mode '$savedTheme', using default")
                ThemeMode.SYSTEM
            }
            println("✅ ThemePreferencesManager - Theme loaded: ${_currentTheme?.name}")
        } catch (e: Exception) {
            println("❌ ThemePreferencesManager - Failed to load theme: ${e.message}")
            _currentTheme = ThemeMode.SYSTEM
        }
    }
}
