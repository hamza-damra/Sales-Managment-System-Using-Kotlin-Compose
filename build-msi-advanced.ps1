# Advanced MSI Builder for Sales Management System
# This script builds a professional MSI installer using WiX Toolset integration

param(
    [switch]$Clean,
    [switch]$Sign,
    [switch]$Verbose,
    [string]$CertificatePath = "",
    [string]$CertificatePassword = "",
    [string]$OutputDir = "build\msi"
)

# Script configuration
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

# Colors for output
$ColorSuccess = "Green"
$ColorWarning = "Yellow"
$ColorError = "Red"
$ColorInfo = "Cyan"

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Write-Header {
    param([string]$Title)
    Write-Host ""
    Write-Host "=".PadRight(60, '=') -ForegroundColor $ColorInfo
    Write-Host " $Title" -ForegroundColor $ColorInfo
    Write-Host "=".PadRight(60, '=') -ForegroundColor $ColorInfo
    Write-Host ""
}

function Test-Prerequisites {
    Write-Header "Checking Prerequisites"
    
    # Check Java
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString() }
        Write-ColorOutput "✅ Java found: $javaVersion" $ColorSuccess
    } catch {
        Write-ColorOutput "❌ Java not found. Please install Java 17 or later." $ColorError
        exit 1
    }
    
    # Check Gradle
    if (Test-Path ".\gradlew.bat") {
        Write-ColorOutput "✅ Gradle Wrapper found" $ColorSuccess
    } else {
        Write-ColorOutput "❌ Gradle Wrapper not found" $ColorError
        exit 1
    }
    
    # Check WiX Toolset
    $wixPath = $env:WIX
    if (-not $wixPath) {
        $wixPath = "C:\Program Files (x86)\WiX Toolset v3.11"
    }
    
    if (Test-Path "$wixPath\bin\candle.exe") {
        Write-ColorOutput "✅ WiX Toolset found at: $wixPath" $ColorSuccess
        $env:WIX = $wixPath
    } else {
        Write-ColorOutput "⚠️  WiX Toolset not found. Enhanced features will be limited." $ColorWarning
        Write-ColorOutput "   Download from: https://wixtoolset.org/releases/" $ColorWarning
    }
    
    # Check PowerShell version
    $psVersion = $PSVersionTable.PSVersion
    if ($psVersion.Major -ge 5) {
        Write-ColorOutput "✅ PowerShell $($psVersion.ToString()) is supported" $ColorSuccess
    } else {
        Write-ColorOutput "⚠️  PowerShell 5.0+ recommended for best experience" $ColorWarning
    }
}

function Invoke-CleanBuild {
    Write-Header "Cleaning Build Environment"
    
    if (Test-Path "build") {
        Write-ColorOutput "🧹 Removing build directory..." $ColorInfo
        Remove-Item -Path "build" -Recurse -Force
    }
    
    if (Test-Path ".gradle") {
        Write-ColorOutput "🧹 Cleaning Gradle cache..." $ColorInfo
        Remove-Item -Path ".gradle" -Recurse -Force
    }
    
    Write-ColorOutput "✅ Clean completed" $ColorSuccess
}

function Build-Application {
    Write-Header "Building Application"
    
    $gradleArgs = @("clean", "build")
    if ($Verbose) {
        $gradleArgs += "--info"
    }
    
    Write-ColorOutput "🔨 Running Gradle build..." $ColorInfo
    & .\gradlew.bat $gradleArgs
    
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "❌ Gradle build failed" $ColorError
        exit 1
    }
    
    Write-ColorOutput "✅ Application build completed" $ColorSuccess
}

function Build-MsiPackage {
    Write-Header "Building MSI Package"
    
    # Check if WiX is available for enhanced build
    if ($env:WIX -and (Test-Path "$env:WIX\bin\candle.exe")) {
        Write-ColorOutput "🚀 Building enhanced MSI with WiX Toolset..." $ColorInfo
        
        $gradleArgs = @("buildMsi")
        if ($Verbose) {
            $gradleArgs += "--info"
        }
        
        & .\gradlew.bat $gradleArgs
        
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput "❌ Enhanced MSI build failed, falling back to standard build..." $ColorWarning
            Build-StandardMsi
        } else {
            Write-ColorOutput "✅ Enhanced MSI package created successfully" $ColorSuccess
            return
        }
    } else {
        Write-ColorOutput "📦 Building standard MSI package..." $ColorInfo
        Build-StandardMsi
    }
}

function Build-StandardMsi {
    $gradleArgs = @("packageMsi")
    if ($Verbose) {
        $gradleArgs += "--info"
    }
    
    & .\gradlew.bat $gradleArgs
    
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "❌ MSI package build failed" $ColorError
        exit 1
    }
    
    Write-ColorOutput "✅ Standard MSI package created successfully" $ColorSuccess
}

function Sign-MsiPackage {
    if (-not $Sign) {
        return
    }
    
    Write-Header "Signing MSI Package"
    
    if (-not $CertificatePath -or -not (Test-Path $CertificatePath)) {
        Write-ColorOutput "⚠️  Certificate path not specified or file not found" $ColorWarning
        Write-ColorOutput "   Use -CertificatePath parameter to specify certificate" $ColorWarning
        return
    }
    
    # Find MSI file
    $msiFiles = Get-ChildItem -Path "build" -Filter "*.msi" -Recurse
    if ($msiFiles.Count -eq 0) {
        Write-ColorOutput "❌ No MSI files found to sign" $ColorError
        return
    }
    
    foreach ($msiFile in $msiFiles) {
        Write-ColorOutput "🔐 Signing: $($msiFile.Name)" $ColorInfo
        
        $signArgs = @(
            "sign",
            "/f", $CertificatePath,
            "/t", "http://timestamp.digicert.com",
            "/d", "Sales Management System",
            $msiFile.FullName
        )
        
        if ($CertificatePassword) {
            $signArgs = $signArgs[0..1] + @("/p", $CertificatePassword) + $signArgs[2..($signArgs.Length-1)]
        }
        
        try {
            & signtool.exe $signArgs
            if ($LASTEXITCODE -eq 0) {
                Write-ColorOutput "✅ Successfully signed: $($msiFile.Name)" $ColorSuccess
            } else {
                Write-ColorOutput "❌ Failed to sign: $($msiFile.Name)" $ColorError
            }
        } catch {
            Write-ColorOutput "❌ Signing tool error: $($_.Exception.Message)" $ColorError
        }
    }
}

function Show-BuildSummary {
    Write-Header "Build Summary"
    
    # Find generated MSI files
    $msiFiles = Get-ChildItem -Path "build" -Filter "*.msi" -Recurse
    
    if ($msiFiles.Count -gt 0) {
        Write-ColorOutput "📦 Generated MSI Packages:" $ColorSuccess
        foreach ($msi in $msiFiles) {
            $size = [math]::Round($msi.Length / 1MB, 2)
            Write-ColorOutput "   📄 $($msi.Name) ($size MB)" $ColorInfo
            Write-ColorOutput "      📁 $($msi.FullName)" $ColorInfo
        }
        
        Write-Host ""
        Write-ColorOutput "🚀 Installation Commands:" $ColorInfo
        Write-ColorOutput "   Interactive: msiexec /i `"$($msiFiles[0].Name)`"" $ColorInfo
        Write-ColorOutput "   Silent:      msiexec /i `"$($msiFiles[0].Name)`" /quiet /norestart" $ColorInfo
        Write-ColorOutput "   Uninstall:   msiexec /x `"$($msiFiles[0].Name)`" /quiet /norestart" $ColorInfo
        
    } else {
        Write-ColorOutput "❌ No MSI packages were generated" $ColorError
        exit 1
    }
    
    # Show additional files
    $additionalFiles = @()
    if (Test-Path "build\msi\install-silent.bat") {
        $additionalFiles += "install-silent.bat"
    }
    if (Test-Path "build\msi\uninstall.bat") {
        $additionalFiles += "uninstall.bat"
    }
    
    if ($additionalFiles.Count -gt 0) {
        Write-Host ""
        Write-ColorOutput "📝 Additional Files:" $ColorSuccess
        foreach ($file in $additionalFiles) {
            Write-ColorOutput "   📄 $file" $ColorInfo
        }
    }
}

# Main execution
try {
    Write-Header "Sales Management System - Advanced MSI Builder"
    Write-ColorOutput "Building professional MSI installer with WiX Toolset integration" $ColorInfo
    
    # Check prerequisites
    Test-Prerequisites
    
    # Clean build if requested
    if ($Clean) {
        Invoke-CleanBuild
    }
    
    # Build application
    Build-Application
    
    # Build MSI package
    Build-MsiPackage
    
    # Sign package if requested
    Sign-MsiPackage
    
    # Show summary
    Show-BuildSummary
    
    Write-Host ""
    Write-ColorOutput "🎉 MSI build completed successfully!" $ColorSuccess
    Write-ColorOutput "Ready for deployment and distribution." $ColorSuccess
    
} catch {
    Write-Host ""
    Write-ColorOutput "❌ Build failed: $($_.Exception.Message)" $ColorError
    Write-ColorOutput "Check the error details above and try again." $ColorError
    exit 1
}
