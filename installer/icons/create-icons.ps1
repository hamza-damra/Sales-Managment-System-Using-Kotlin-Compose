# Enhanced Icon Creation Script for Sales Management System
# This script creates placeholder icons and WiX installer graphics

param(
    [switch]$CreateWixGraphics,
    [switch]$Force,
    [switch]$Help
)

if ($Help) {
    Write-Host ""
    Write-Host "Enhanced Icon Creation Script for Sales Management System" -ForegroundColor Cyan
    Write-Host "=========================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage:" -ForegroundColor Yellow
    Write-Host "  .\create-icons.ps1                    # Create app icons only" -ForegroundColor White
    Write-Host "  .\create-icons.ps1 -CreateWixGraphics # Create all graphics including WiX" -ForegroundColor White
    Write-Host "  .\create-icons.ps1 -Force             # Overwrite existing files" -ForegroundColor White
    Write-Host "  .\create-icons.ps1 -Help              # Show this help" -ForegroundColor White
    Write-Host ""
    Write-Host "Parameters:" -ForegroundColor Yellow
    Write-Host "  -CreateWixGraphics  Create WiX installer banner and dialog graphics" -ForegroundColor White
    Write-Host "  -Force              Overwrite existing icon files" -ForegroundColor White
    Write-Host "  -Help               Show this help message" -ForegroundColor White
    Write-Host ""
    exit 0
}

Write-Host "Enhanced Icon Creation for Sales Management System" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# Function to create a placeholder file with metadata
function Create-PlaceholderFile {
    param(
        [string]$FilePath,
        [int]$Width,
        [int]$Height,
        [string]$Description,
        [string]$Format
    )

    $content = @"
# Placeholder for $Description
# Format: $Format
# Dimensions: $Width x $Height
# Created: $(Get-Date)
#
# This is a placeholder file for the Sales Management System installer.
# Replace this file with a professional $Format graphic.
#
# Requirements:
# - Size: $Width x $Height pixels
# - Format: $Format
# - Professional business appearance
# - Consistent branding
# - High quality and clear at all sizes
"@

    [System.IO.File]::WriteAllText($FilePath, $content)
}

# Create application icons
Write-Host ""
Write-Host "Creating application icons..." -ForegroundColor Yellow

$icons = @(
    @{ Name = "app-icon.ico"; Width = 256; Height = 256; Description = "Windows application icon"; Format = "ICO" },
    @{ Name = "app-icon.png"; Width = 256; Height = 256; Description = "Linux application icon"; Format = "PNG" },
    @{ Name = "app-icon.icns"; Width = 256; Height = 256; Description = "macOS application icon bundle"; Format = "ICNS" }
)

foreach ($icon in $icons) {
    $iconPath = $icon.Name
    if (-not (Test-Path $iconPath) -or $Force) {
        Write-Host "  Creating placeholder: $iconPath" -ForegroundColor Gray
        Create-PlaceholderFile -FilePath $iconPath -Width $icon.Width -Height $icon.Height -Description $icon.Description -Format $icon.Format
        Write-Host "  ⚠️  Created placeholder $($icon.Description)" -ForegroundColor Yellow
    } else {
        Write-Host "  ✅ Icon exists: $iconPath" -ForegroundColor Green
    }
}

# Create WiX installer graphics if requested
if ($CreateWixGraphics) {
    Write-Host ""
    Write-Host "Creating WiX installer graphics..." -ForegroundColor Yellow

    $wixGraphics = @(
        @{ Name = "banner.bmp"; Width = 493; Height = 58; Description = "WiX installer banner"; Format = "BMP" },
        @{ Name = "dialog.bmp"; Width = 493; Height = 312; Description = "WiX installer dialog background"; Format = "BMP" }
    )

    foreach ($graphic in $wixGraphics) {
        $graphicPath = $graphic.Name
        if (-not (Test-Path $graphicPath) -or $Force) {
            Write-Host "  Creating placeholder: $graphicPath" -ForegroundColor Gray
            Create-PlaceholderFile -FilePath $graphicPath -Width $graphic.Width -Height $graphic.Height -Description $graphic.Description -Format $graphic.Format
            Write-Host "  ⚠️  Created placeholder $($graphic.Description)" -ForegroundColor Yellow
            Write-Host "     Size: $($graphic.Width) x $($graphic.Height)" -ForegroundColor Gray
        } else {
            Write-Host "  ✅ Graphic exists: $graphicPath" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "✅ Icon creation process completed!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Summary:" -ForegroundColor Cyan
Write-Host "  - Application icon placeholders created" -ForegroundColor White
if ($CreateWixGraphics) {
    Write-Host "  - WiX installer graphics placeholders created" -ForegroundColor White
}
Write-Host ""
Write-Host "🚀 Next Steps:" -ForegroundColor Cyan
Write-Host "1. Replace placeholders with professional designs" -ForegroundColor White
Write-Host "2. Use professional design tools (Adobe, Figma, etc.)" -ForegroundColor White
Write-Host "3. Test icons in different contexts and sizes" -ForegroundColor White
Write-Host "4. Ensure consistent branding across all graphics" -ForegroundColor White
Write-Host ""
Write-Host "💡 Professional Design Tips:" -ForegroundColor Cyan
Write-Host "- Use vector graphics for scalability" -ForegroundColor White
Write-Host "- Maintain consistent color scheme (#1976D2, #2E7D32)" -ForegroundColor White
Write-Host "- Consider Arabic/RTL interface conventions" -ForegroundColor White
Write-Host "- Test readability at 16x16 size" -ForegroundColor White
Write-Host "- Use business-appropriate imagery (charts, graphs)" -ForegroundColor White
    pause
    exit /b 1
)

REM Create a simple colored square as base
magick -size 256x256 xc:#2563eb -fill white -gravity center -pointsize 72 -annotate +0+0 "SMS" app-icon-256.png

REM Create ICO file with multiple sizes
magick app-icon-256.png -resize 16x16 app-icon-16.png
magick app-icon-256.png -resize 32x32 app-icon-32.png
magick app-icon-256.png -resize 48x48 app-icon-48.png

REM Combine into ICO file
magick app-icon-16.png app-icon-32.png app-icon-48.png app-icon-256.png app-icon.ico

REM Create PNG for Linux
copy app-icon-256.png app-icon.png

REM Create ICNS for macOS (requires additional tools)
echo For macOS ICNS file, use: png2icns app-icon.icns app-icon-256.png
echo Or use online converters to create app-icon.icns

REM Cleanup temporary files
del app-icon-16.png app-icon-32.png app-icon-48.png app-icon-256.png

echo Icons created successfully!
echo Files: app-icon.ico, app-icon.png
echo Note: Create app-icon.icns manually for macOS support
pause
"@

$imageMagickScript | Out-File -FilePath "installer/icons/create-icons-imagemagick.bat" -Encoding ASCII

Write-Host "Created icon generation script: installer/icons/create-icons-imagemagick.bat" -ForegroundColor Green
Write-Host "Run this script if you have ImageMagick installed to create basic icons." -ForegroundColor Yellow
