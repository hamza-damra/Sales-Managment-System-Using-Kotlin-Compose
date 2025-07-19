# Simple Icon Creation Script for Sales Management System
# Creates placeholder icon files for the MSI installer

param(
    [switch]$CreateWixGraphics,
    [switch]$Force
)

Write-Host "Creating icon placeholders for Sales Management System..." -ForegroundColor Cyan

# Create icons directory if it doesn't exist
$iconsDir = "installer/icons"
if (-not (Test-Path $iconsDir)) {
    New-Item -Path $iconsDir -ItemType Directory -Force | Out-Null
    Write-Host "Created icons directory: $iconsDir" -ForegroundColor Green
}

# Function to create placeholder files
function Create-PlaceholderFile {
    param(
        [string]$FilePath,
        [string]$Description
    )
    
    $content = @"
# Placeholder for $Description
# Created: $(Get-Date)
#
# This is a placeholder file for the Sales Management System installer.
# Replace this file with a professional graphic.
#
# Requirements:
# - Use professional design tools
# - Maintain consistent branding
# - Ensure high quality and clarity
# - Test at different sizes
"@
    
    [System.IO.File]::WriteAllText($FilePath, $content)
}

# Create application icons
Write-Host "Creating application icons..." -ForegroundColor Yellow

$icons = @(
    @{ Name = "app-icon.ico"; Description = "Windows application icon (256x256)" },
    @{ Name = "app-icon.png"; Description = "Linux application icon (256x256)" },
    @{ Name = "app-icon.icns"; Description = "macOS application icon bundle" }
)

foreach ($icon in $icons) {
    $iconPath = "$iconsDir/$($icon.Name)"
    if (-not (Test-Path $iconPath) -or $Force) {
        Write-Host "  Creating: $($icon.Name)" -ForegroundColor Gray
        Create-PlaceholderFile -FilePath $iconPath -Description $icon.Description
        Write-Host "  Created placeholder for $($icon.Description)" -ForegroundColor Yellow
    } else {
        Write-Host "  Exists: $($icon.Name)" -ForegroundColor Green
    }
}

# Create WiX installer graphics if requested
if ($CreateWixGraphics) {
    Write-Host ""
    Write-Host "Creating WiX installer graphics..." -ForegroundColor Yellow
    
    $wixGraphics = @(
        @{ Name = "banner.bmp"; Description = "WiX installer banner (493x58)" },
        @{ Name = "dialog.bmp"; Description = "WiX installer dialog background (493x312)" }
    )
    
    foreach ($graphic in $wixGraphics) {
        $graphicPath = "$iconsDir/$($graphic.Name)"
        if (-not (Test-Path $graphicPath) -or $Force) {
            Write-Host "  Creating: $($graphic.Name)" -ForegroundColor Gray
            Create-PlaceholderFile -FilePath $graphicPath -Description $graphic.Description
            Write-Host "  Created placeholder for $($graphic.Description)" -ForegroundColor Yellow
        } else {
            Write-Host "  Exists: $($graphic.Name)" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "Icon creation completed!" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Replace placeholder files with professional graphics" -ForegroundColor White
Write-Host "2. Use design tools like Adobe Illustrator, Photoshop, or Figma" -ForegroundColor White
Write-Host "3. Ensure proper sizing and format compliance" -ForegroundColor White
Write-Host "4. Test graphics in installer and application contexts" -ForegroundColor White
Write-Host ""
Write-Host "Professional Requirements:" -ForegroundColor Cyan
Write-Host "- ICO: Multi-resolution Windows icon (16x16 to 256x256)" -ForegroundColor White
Write-Host "- PNG: High-quality 256x256 with transparency" -ForegroundColor White
Write-Host "- ICNS: macOS icon bundle with Retina support" -ForegroundColor White
Write-Host "- BMP: 24-bit color for WiX installer graphics" -ForegroundColor White
