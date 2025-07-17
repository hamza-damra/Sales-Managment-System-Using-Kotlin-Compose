# PowerShell script to create basic application icons for the Sales Management System
# This creates simple placeholder icons that can be replaced with professional designs later

Write-Host "Creating application icons for Sales Management System..." -ForegroundColor Green

# Create a simple ICO file for Windows (placeholder)
# In a real scenario, you would use proper icon creation tools
$iconContent = @"
This is a placeholder for the Windows ICO icon file.
To create a proper icon:

1. Use an icon editor like IcoFX, GIMP, or online tools
2. Create icons with multiple sizes: 16x16, 32x32, 48x48, 256x256
3. Use business-appropriate colors (blues, grays, professional palette)
4. Include sales/management related imagery (charts, graphs, calculator)
5. Save as app-icon.ico in this directory

For now, the build will work without icons, but they enhance the professional appearance.
"@

# Create placeholder files with instructions
$iconContent | Out-File -FilePath "installer/icons/app-icon-instructions.txt" -Encoding UTF8

Write-Host "Icon placeholder files created." -ForegroundColor Yellow
Write-Host "Please replace with actual icon files:" -ForegroundColor Yellow
Write-Host "  - app-icon.ico (Windows)" -ForegroundColor White
Write-Host "  - app-icon.icns (macOS)" -ForegroundColor White  
Write-Host "  - app-icon.png (Linux)" -ForegroundColor White

# Create a simple batch file to generate basic icons using ImageMagick (if available)
$imageMagickScript = @"
@echo off
echo Creating basic application icons...

REM Check if ImageMagick is available
where magick >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ImageMagick not found. Please install ImageMagick or create icons manually.
    echo Download from: https://imagemagick.org/script/download.php#windows
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
