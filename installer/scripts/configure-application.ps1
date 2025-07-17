# Sales Management System Post-Installation Configuration Script
# This script configures the application for fullscreen mode and production backend

param(
    [string]$InstallPath = "",
    [string]$BackendURL = "https://sales-managment-system-backend-springboot.onrender.com",
    [bool]$FullscreenMode = $true
)

Write-Host "🔧 Configuring Sales Management System..." -ForegroundColor Cyan

# Validate install path
if (-not $InstallPath -or -not (Test-Path $InstallPath)) {
    # Try to find installation path from registry
    try {
        $regPath = "HKLM:\SOFTWARE\HamzaDamra\SalesManagementSystem"
        if (Test-Path $regPath) {
            $InstallPath = (Get-ItemProperty -Path $regPath -Name "InstallPath").InstallPath
            Write-Host "✅ Found installation path from registry: $InstallPath" -ForegroundColor Green
        }
    } catch {
        Write-Host "❌ Could not determine installation path" -ForegroundColor Red
        exit 1
    }
}

# Create application configuration directory
$configDir = Join-Path $env:APPDATA "SalesManagementSystem"
if (-not (Test-Path $configDir)) {
    New-Item -Path $configDir -ItemType Directory -Force | Out-Null
    Write-Host "📁 Created configuration directory: $configDir" -ForegroundColor Green
}

# Create application configuration file
$configFile = Join-Path $configDir "app.config"
$configContent = @"
# Sales Management System Configuration
# Generated during installation

# Backend Configuration
backend.url=$BackendURL
backend.timeout=30000
backend.retries=3

# Application Settings
app.fullscreen=$($FullscreenMode.ToString().ToLower())
app.version=2.1.0
app.auto-update=true

# UI Settings
ui.theme=system
ui.language=en
ui.rtl-support=true

# Window Settings (when not in fullscreen)
window.width=1400
window.height=900
window.center=true
window.resizable=true

# Performance Settings
performance.cache-enabled=true
performance.lazy-loading=true
performance.batch-size=50

# Security Settings
security.auto-logout=false
security.session-timeout=3600000

# Update Settings
update.check-interval=30
update.auto-download=true
update.notify-user=true

# Logging Settings
logging.level=INFO
logging.file-enabled=true
logging.max-size=10MB
logging.max-files=5
"@

$configContent | Out-File -FilePath $configFile -Encoding UTF8
Write-Host "📝 Created application configuration: $configFile" -ForegroundColor Green

# Create Windows registry entries for application registration
Write-Host "📋 Creating registry entries..." -ForegroundColor Yellow

try {
    # Main application registry key
    $appRegPath = "HKLM:\SOFTWARE\HamzaDamra\SalesManagementSystem"
    if (-not (Test-Path $appRegPath)) {
        New-Item -Path $appRegPath -Force | Out-Null
    }
    
    # Set application properties
    Set-ItemProperty -Path $appRegPath -Name "InstallPath" -Value $InstallPath
    Set-ItemProperty -Path $appRegPath -Name "Version" -Value "2.1.0"
    Set-ItemProperty -Path $appRegPath -Name "DisplayName" -Value "Sales Management System"
    Set-ItemProperty -Path $appRegPath -Name "Publisher" -Value "Hamza Damra"
    Set-ItemProperty -Path $appRegPath -Name "InstallDate" -Value (Get-Date -Format "yyyy-MM-dd")
    Set-ItemProperty -Path $appRegPath -Name "BackendURL" -Value $BackendURL
    Set-ItemProperty -Path $appRegPath -Name "FullscreenMode" -Value ([int]$FullscreenMode)
    Set-ItemProperty -Path $appRegPath -Name "ConfigPath" -Value $configDir
    
    Write-Host "✅ Registry entries created successfully" -ForegroundColor Green
    
} catch {
    Write-Host "⚠️  Could not create registry entries: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host "   Application will still work, but some features may be limited" -ForegroundColor Yellow
}

# Create uninstall registry entries
try {
    $uninstallRegPath = "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\SalesManagementSystem"
    if (-not (Test-Path $uninstallRegPath)) {
        New-Item -Path $uninstallRegPath -Force | Out-Null
    }
    
    Set-ItemProperty -Path $uninstallRegPath -Name "DisplayName" -Value "Sales Management System"
    Set-ItemProperty -Path $uninstallRegPath -Name "DisplayVersion" -Value "2.1.0"
    Set-ItemProperty -Path $uninstallRegPath -Name "Publisher" -Value "Hamza Damra"
    Set-ItemProperty -Path $uninstallRegPath -Name "InstallLocation" -Value $InstallPath
    Set-ItemProperty -Path $uninstallRegPath -Name "EstimatedSize" -Value 150000
    Set-ItemProperty -Path $uninstallRegPath -Name "NoModify" -Value 1
    Set-ItemProperty -Path $uninstallRegPath -Name "NoRepair" -Value 1
    
    Write-Host "✅ Uninstall registry entries created" -ForegroundColor Green
    
} catch {
    Write-Host "⚠️  Could not create uninstall registry entries" -ForegroundColor Yellow
}

# Create desktop shortcut with proper configuration
$desktopPath = [Environment]::GetFolderPath("Desktop")
$shortcutPath = Join-Path $desktopPath "Sales Management System.lnk"

try {
    $WshShell = New-Object -ComObject WScript.Shell
    $Shortcut = $WshShell.CreateShortcut($shortcutPath)
    $Shortcut.TargetPath = Join-Path $InstallPath "SalesManagementSystem.exe"
    $Shortcut.WorkingDirectory = $InstallPath
    $Shortcut.Description = "Professional Sales Management System"
    $Shortcut.WindowStyle = if ($FullscreenMode) { 3 } else { 1 }  # 3 = Maximized, 1 = Normal
    
    # Add arguments for fullscreen mode if enabled
    if ($FullscreenMode) {
        $Shortcut.Arguments = "--fullscreen"
    }
    
    $Shortcut.Save()
    Write-Host "✅ Desktop shortcut created: $shortcutPath" -ForegroundColor Green
    
} catch {
    Write-Host "⚠️  Could not create desktop shortcut: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Create Start Menu shortcut
$startMenuPath = Join-Path $env:ProgramData "Microsoft\Windows\Start Menu\Programs"
$startMenuShortcut = Join-Path $startMenuPath "Sales Management System.lnk"

try {
    $WshShell = New-Object -ComObject WScript.Shell
    $Shortcut = $WshShell.CreateShortcut($startMenuShortcut)
    $Shortcut.TargetPath = Join-Path $InstallPath "SalesManagementSystem.exe"
    $Shortcut.WorkingDirectory = $InstallPath
    $Shortcut.Description = "Professional Sales Management System"
    $Shortcut.WindowStyle = if ($FullscreenMode) { 3 } else { 1 }
    
    if ($FullscreenMode) {
        $Shortcut.Arguments = "--fullscreen"
    }
    
    $Shortcut.Save()
    Write-Host "✅ Start Menu shortcut created: $startMenuShortcut" -ForegroundColor Green
    
} catch {
    Write-Host "⚠️  Could not create Start Menu shortcut: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Verify backend connectivity
Write-Host "🌐 Testing backend connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BackendURL/actuator/health" -TimeoutSec 10 -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Backend is accessible at: $BackendURL" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Backend responded with status: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Could not connect to backend: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host "   The application will still work, but may have limited functionality until backend is available" -ForegroundColor Yellow
}

Write-Host "`n🎉 Configuration completed successfully!" -ForegroundColor Green
Write-Host "📋 Configuration Summary:" -ForegroundColor Cyan
Write-Host "   Install Path: $InstallPath" -ForegroundColor White
Write-Host "   Backend URL: $BackendURL" -ForegroundColor White
Write-Host "   Fullscreen Mode: $FullscreenMode" -ForegroundColor White
Write-Host "   Config Directory: $configDir" -ForegroundColor White

Write-Host "`n🚀 The Sales Management System is ready to use!" -ForegroundColor Green
