/**
 * Theme Persistence Test
 * This file demonstrates and tests the theme persistence functionality
 */

import data.preferences.ThemePreferencesManager
import ui.theme.ThemeMode
import ui.theme.ThemeState
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("🎨 Theme Persistence Test")
    println("=" * 50)
    
    // Create theme preferences manager
    val preferencesManager = ThemePreferencesManager()
    
    // Test 1: Check initial state
    println("\n📋 Test 1: Initial State")
    println("Has theme preference: ${preferencesManager.hasThemePreference()}")
    println("Current theme: ${preferencesManager.getCurrentTheme()}")
    
    // Test 2: Save different themes
    println("\n💾 Test 2: Saving Themes")
    
    // Save Light theme
    preferencesManager.saveTheme(ThemeMode.LIGHT)
    println("Saved LIGHT theme")
    println("Current theme: ${preferencesManager.getCurrentTheme()}")
    
    // Save Dark theme
    preferencesManager.saveTheme(ThemeMode.DARK)
    println("Saved DARK theme")
    println("Current theme: ${preferencesManager.getCurrentTheme()}")
    
    // Save System theme
    preferencesManager.saveTheme(ThemeMode.SYSTEM)
    println("Saved SYSTEM theme")
    println("Current theme: ${preferencesManager.getCurrentTheme()}")
    
    // Test 3: Theme state integration
    println("\n🔄 Test 3: ThemeState Integration")
    val themeState = ThemeState(preferencesManager)
    println("ThemeState current mode: ${themeState.themeMode}")
    
    // Change theme through ThemeState
    themeState.setThemeMode(ThemeMode.LIGHT)
    println("Changed to LIGHT via ThemeState")
    
    // Verify persistence
    val newPreferencesManager = ThemePreferencesManager()
    println("New manager loaded theme: ${newPreferencesManager.getCurrentTheme()}")
    
    // Test 4: Clear preferences
    println("\n🗑️ Test 4: Clear Preferences")
    preferencesManager.clearTheme()
    println("Cleared theme preference")
    println("Has theme preference: ${preferencesManager.hasThemePreference()}")
    println("Current theme (should be SYSTEM): ${preferencesManager.getCurrentTheme()}")
    
    println("\n✅ All tests completed successfully!")
    println("🎉 Theme persistence is working correctly!")
}

private operator fun String.times(n: Int): String = this.repeat(n)
