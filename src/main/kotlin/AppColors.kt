// Temporary compatibility layer for AppColors
// This allows existing code to compile while we transition to the new theme system

// root-level alias for backward compatibility
// no package declaration so this resides in the default (root) package
import ui.theme.AppColorsCompat

// Re-export AppColorsCompat as AppColors for backwards compatibility at root
typealias AppColors = AppColorsCompat
