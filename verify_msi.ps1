# Sales Management System MSI Installer Verification Script
# This script verifies the generated MSI installer

Write-Host "🔍 Sales Management System MSI Installer Verification" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Cyan

$msiPath = "build\compose\binaries\main\msi\SalesManagementSystem-2.1.0.msi"

# Check if MSI file exists
if (Test-Path $msiPath) {
    Write-Host "✅ MSI file found: $msiPath" -ForegroundColor Green
    
    # Get file information
    $fileInfo = Get-Item $msiPath
    Write-Host "📁 File Size: $([math]::Round($fileInfo.Length / 1MB, 2)) MB" -ForegroundColor Yellow
    Write-Host "📅 Created: $($fileInfo.CreationTime)" -ForegroundColor Yellow
    Write-Host "📅 Modified: $($fileInfo.LastWriteTime)" -ForegroundColor Yellow
    
    # Verify MSI properties using Windows Installer
    try {
        $windowsInstaller = New-Object -ComObject WindowsInstaller.Installer
        $database = $windowsInstaller.GetType().InvokeMember("OpenDatabase", "InvokeMethod", $null, $windowsInstaller, @($msiPath, 0))
        
        # Query MSI properties
        $query = "SELECT Property, Value FROM Property WHERE Property IN ('ProductName', 'ProductVersion', 'Manufacturer', 'ProductCode', 'UpgradeCode')"
        $view = $database.GetType().InvokeMember("OpenView", "InvokeMethod", $null, $database, $query)
        $view.GetType().InvokeMember("Execute", "InvokeMethod", $null, $view, $null)
        
        Write-Host "`n📋 MSI Properties:" -ForegroundColor Cyan
        
        do {
            $record = $view.GetType().InvokeMember("Fetch", "InvokeMethod", $null, $view, $null)
            if ($record) {
                $property = $record.GetType().InvokeMember("StringData", "GetProperty", $null, $record, 1)
                $value = $record.GetType().InvokeMember("StringData", "GetProperty", $null, $record, 2)
                Write-Host "   $property`: $value" -ForegroundColor White
            }
        } while ($record)
        
        # Clean up COM objects
        [System.Runtime.Interopservices.Marshal]::ReleaseComObject($view) | Out-Null
        [System.Runtime.Interopservices.Marshal]::ReleaseComObject($database) | Out-Null
        [System.Runtime.Interopservices.Marshal]::ReleaseComObject($windowsInstaller) | Out-Null
        
    } catch {
        Write-Host "⚠️  Could not read MSI properties: $($_.Exception.Message)" -ForegroundColor Yellow
    }
    
    # Check digital signature (if present)
    try {
        $signature = Get-AuthenticodeSignature $msiPath
        if ($signature.Status -eq "Valid") {
            Write-Host "Digital Signature: Valid" -ForegroundColor Green
            Write-Host "   Signer: $($signature.SignerCertificate.Subject)" -ForegroundColor White
        } elseif ($signature.Status -eq "NotSigned") {
            Write-Host "Digital Signature: Not signed (normal for development)" -ForegroundColor Yellow
        } else {
            Write-Host "Digital Signature: $($signature.Status)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "Could not check digital signature: $($_.Exception.Message)" -ForegroundColor Yellow
    }
    
    Write-Host "`n🎯 Installation Commands:" -ForegroundColor Cyan
    Write-Host "   Interactive Install: msiexec /i `"$msiPath`"" -ForegroundColor White
    Write-Host "   Silent Install:      msiexec /i `"$msiPath`" /quiet /norestart" -ForegroundColor White
    Write-Host "   Uninstall:           msiexec /x `"$msiPath`" /quiet /norestart" -ForegroundColor White
    
    Write-Host "`nMSI Installer Verification Complete!" -ForegroundColor Green
    Write-Host "The installer is ready for distribution and testing." -ForegroundColor Green

} else {
    Write-Host "MSI file not found: $msiPath" -ForegroundColor Red
    Write-Host "Please run: ./gradlew packageMsi" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n🚀 Next Steps:" -ForegroundColor Cyan
Write-Host "1. Test installation on a clean Windows system" -ForegroundColor White
Write-Host "2. Verify all application features work correctly" -ForegroundColor White
Write-Host "3. Test the update system functionality" -ForegroundColor White
Write-Host "4. Deploy to production environment" -ForegroundColor White

# Optional: Prompt to test installation
$response = Read-Host "`nWould you like to test install the MSI now? (y/N)"
if ($response -eq "y" -or $response -eq "Y") {
    Write-Host "Starting MSI installation..." -ForegroundColor Green
    Start-Process "msiexec" -ArgumentList "/i", "`"$msiPath`"" -Wait
    Write-Host "Installation process completed." -ForegroundColor Green
}
