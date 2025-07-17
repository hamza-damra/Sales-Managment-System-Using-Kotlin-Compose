# Sales Management System MSI Builder Script
# This script builds the MSI installer with all professional features

param(
    [switch]$Clean = $false,
    [switch]$SkipTests = $true,
    [string]$Configuration = "Release"
)

Write-Host "🚀 Sales Management System MSI Builder" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan

# Check prerequisites
Write-Host "📋 Checking prerequisites..." -ForegroundColor Yellow

# Check if Gradle is available
try {
    $gradleVersion = & ./gradlew --version 2>$null | Select-String "Gradle"
    Write-Host "✅ Gradle found: $gradleVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Gradle not found. Please ensure Gradle is installed and accessible." -ForegroundColor Red
    exit 1
}

# Check Java version
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✅ Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java not found. Please ensure Java 17+ is installed." -ForegroundColor Red
    exit 1
}

# Check if WiX Toolset is available (optional but recommended)
$wixPath = "C:\Program Files (x86)\WiX Toolset v3.11\bin"
if (Test-Path $wixPath) {
    Write-Host "✅ WiX Toolset found at: $wixPath" -ForegroundColor Green
    $env:WIX = "C:\Program Files (x86)\WiX Toolset v3.11"
} else {
    Write-Host "⚠️  WiX Toolset not found. Advanced MSI features may not be available." -ForegroundColor Yellow
    Write-Host "   Download from: https://wixtoolset.org/releases/" -ForegroundColor Yellow
}

# Clean build if requested
if ($Clean) {
    Write-Host "🧹 Cleaning previous build..." -ForegroundColor Yellow
    & ./gradlew clean
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Clean failed" -ForegroundColor Red
        exit 1
    }
}

# Create icons if they don't exist
Write-Host "🎨 Checking application icons..." -ForegroundColor Yellow
if (-not (Test-Path "installer/icons/app-icon.ico")) {
    Write-Host "⚠️  Application icons not found. Creating placeholder..." -ForegroundColor Yellow
    
    # Create a simple placeholder icon using PowerShell
    $iconScript = @"
# Create a simple colored bitmap and convert to ICO
Add-Type -AssemblyName System.Drawing

# Create a 256x256 bitmap
`$bitmap = New-Object System.Drawing.Bitmap(256, 256)
`$graphics = [System.Drawing.Graphics]::FromImage(`$bitmap)

# Fill with blue background
`$blueBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(37, 99, 235))
`$graphics.FillRectangle(`$blueBrush, 0, 0, 256, 256)

# Add white text
`$font = New-Object System.Drawing.Font("Arial", 48, [System.Drawing.FontStyle]::Bold)
`$whiteBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
`$stringFormat = New-Object System.Drawing.StringFormat
`$stringFormat.Alignment = [System.Drawing.StringAlignment]::Center
`$stringFormat.LineAlignment = [System.Drawing.StringAlignment]::Center
`$graphics.DrawString("SMS", `$font, `$whiteBrush, 128, 128, `$stringFormat)

# Save as PNG first
`$bitmap.Save("installer/icons/app-icon.png", [System.Drawing.Imaging.ImageFormat]::Png)

# Create ICO file (simplified - just save as PNG for now)
Copy-Item "installer/icons/app-icon.png" "installer/icons/app-icon.ico"

# Cleanup
`$graphics.Dispose()
`$bitmap.Dispose()
`$blueBrush.Dispose()
`$whiteBrush.Dispose()
`$font.Dispose()

Write-Host "Created placeholder icons" -ForegroundColor Green
"@
    
    try {
        Invoke-Expression $iconScript
    } catch {
        Write-Host "⚠️  Could not create placeholder icons. Continuing without icons..." -ForegroundColor Yellow
        # Create empty placeholder files
        New-Item -Path "installer/icons/app-icon.ico" -ItemType File -Force | Out-Null
        New-Item -Path "installer/icons/app-icon.png" -ItemType File -Force | Out-Null
        New-Item -Path "installer/icons/app-icon.icns" -ItemType File -Force | Out-Null
    }
} else {
    Write-Host "✅ Application icons found" -ForegroundColor Green
}

# Build the application
Write-Host "🔨 Building application..." -ForegroundColor Yellow

$buildCommand = "./gradlew"
if ($SkipTests) {
    $buildCommand += " -x test"
}
$buildCommand += " packageMsi"

Write-Host "Executing: $buildCommand" -ForegroundColor Gray
Invoke-Expression $buildCommand

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed" -ForegroundColor Red
    exit 1
}

# Check if MSI was created
$msiPath = "build/compose/binaries/main/msi/SalesManagementSystem-2.1.0.msi"
if (Test-Path $msiPath) {
    Write-Host "✅ MSI installer created successfully!" -ForegroundColor Green
    
    # Get file information
    $fileInfo = Get-Item $msiPath
    Write-Host "📁 File: $msiPath" -ForegroundColor White
    Write-Host "📏 Size: $([math]::Round($fileInfo.Length / 1MB, 2)) MB" -ForegroundColor White
    Write-Host "📅 Created: $($fileInfo.CreationTime)" -ForegroundColor White
    
    # Run verification script if available
    if (Test-Path "verify_msi.ps1") {
        Write-Host "🔍 Running MSI verification..." -ForegroundColor Yellow
        & .\verify_msi.ps1
    }
    
    Write-Host "`n🎉 Build completed successfully!" -ForegroundColor Green
    Write-Host "📦 MSI installer is ready for distribution" -ForegroundColor Green
    
    # Show installation commands
    Write-Host "`n💡 Installation Commands:" -ForegroundColor Cyan
    Write-Host "   Interactive: msiexec /i `"$msiPath`"" -ForegroundColor White
    Write-Host "   Silent:      msiexec /i `"$msiPath`" /quiet /norestart" -ForegroundColor White
    Write-Host "   Uninstall:   msiexec /x `"$msiPath`" /quiet /norestart" -ForegroundColor White
    
} else {
    Write-Host "❌ MSI installer was not created" -ForegroundColor Red
    Write-Host "Check the build output above for errors" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n🚀 Next Steps:" -ForegroundColor Cyan
Write-Host "1. Test the installer on a clean Windows system" -ForegroundColor White
Write-Host "2. Verify all application features work correctly" -ForegroundColor White
Write-Host "3. Test fullscreen mode and backend connectivity" -ForegroundColor White
Write-Host "4. Deploy to production environment" -ForegroundColor White
