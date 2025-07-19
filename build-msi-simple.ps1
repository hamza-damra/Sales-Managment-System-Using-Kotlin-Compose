# Simple MSI Builder for Sales Management System
# This script builds the MSI installer using standard Gradle tasks

param(
    [switch]$Clean,
    [switch]$Verbose
)

# Script configuration
$ErrorActionPreference = "Stop"

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
    Write-Host ("=" * 60) -ForegroundColor $ColorInfo
    Write-Host " $Title" -ForegroundColor $ColorInfo
    Write-Host ("=" * 60) -ForegroundColor $ColorInfo
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
    
    # Check WiX Toolset (optional)
    $wixPath = $env:WIX
    if (-not $wixPath) {
        $wixPath = "C:\Program Files (x86)\WiX Toolset v3.11"
    }
    
    if (Test-Path "$wixPath\bin\candle.exe") {
        Write-ColorOutput "✅ WiX Toolset found at: $wixPath" $ColorSuccess
        $env:WIX = $wixPath
    } else {
        Write-ColorOutput "⚠️  WiX Toolset not found. Using standard MSI build." $ColorWarning
        Write-ColorOutput "   Download from: https://wixtoolset.org/releases/" $ColorWarning
    }
}

function Invoke-CleanBuild {
    Write-Header "Cleaning Build Environment"
    
    if (Test-Path "build") {
        Write-ColorOutput "Removing build directory..." $ColorInfo
        Remove-Item -Path "build" -Recurse -Force
    }
    
    Write-ColorOutput "✅ Clean completed" $ColorSuccess
}

function Build-Application {
    Write-Header "Building Application"
    
    $gradleArgs = @("clean", "build")
    if ($Verbose) {
        $gradleArgs += "--info"
    }
    
    Write-ColorOutput "Running Gradle build..." $ColorInfo
    & .\gradlew.bat $gradleArgs
    
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "❌ Gradle build failed" $ColorError
        exit 1
    }
    
    Write-ColorOutput "✅ Application build completed" $ColorSuccess
}

function Build-MsiPackage {
    Write-Header "Building MSI Package"
    
    $gradleArgs = @("packageMsi")
    if ($Verbose) {
        $gradleArgs += "--info"
    }
    
    Write-ColorOutput "Building MSI package..." $ColorInfo
    & .\gradlew.bat $gradleArgs
    
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "❌ MSI package build failed" $ColorError
        exit 1
    }
    
    Write-ColorOutput "✅ MSI package created successfully" $ColorSuccess
}

function Show-BuildSummary {
    Write-Header "Build Summary"
    
    # Find generated MSI files
    $msiFiles = Get-ChildItem -Path "build" -Filter "*.msi" -Recurse
    
    if ($msiFiles.Count -gt 0) {
        Write-ColorOutput "Generated MSI Packages:" $ColorSuccess
        foreach ($msi in $msiFiles) {
            $size = [math]::Round($msi.Length / 1MB, 2)
            Write-ColorOutput "   File: $($msi.Name) ($size MB)" $ColorInfo
            Write-ColorOutput "   Path: $($msi.FullName)" $ColorInfo
        }

        Write-Host ""
        Write-ColorOutput "Installation Commands:" $ColorInfo
        $msiName = $msiFiles[0].Name
        Write-ColorOutput "   Interactive: msiexec /i `"$msiName`"" $ColorInfo
        Write-ColorOutput "   Silent:      msiexec /i `"$msiName`" /quiet /norestart" $ColorInfo
        Write-ColorOutput "   Uninstall:   msiexec /x `"$msiName`" /quiet /norestart" $ColorInfo

    } else {
        Write-ColorOutput "No MSI packages were generated" $ColorError
        exit 1
    }
}

# Main execution
try {
    Write-Header "Sales Management System - MSI Builder"
    Write-ColorOutput "Building MSI installer for professional deployment" $ColorInfo
    
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
    
    # Show summary
    Show-BuildSummary
    
    Write-Host ""
    Write-ColorOutput "MSI build completed successfully!" $ColorSuccess
    Write-ColorOutput "Ready for deployment and distribution." $ColorSuccess
    
} catch {
    Write-Host ""
    Write-ColorOutput "❌ Build failed: $($_.Exception.Message)" $ColorError
    Write-ColorOutput "Check the error details above and try again." $ColorError
    exit 1
}
