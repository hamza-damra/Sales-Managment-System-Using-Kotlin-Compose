# Sales Management System MSI Installer Verification Script
# Simple version without Unicode characters

Write-Host "Sales Management System MSI Installer Verification" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$msiPath = "build\compose\binaries\main\msi\SalesManagementSystem-2.1.0.msi"

# Check if MSI file exists
if (Test-Path $msiPath) {
    Write-Host "SUCCESS: MSI file found: $msiPath" -ForegroundColor Green
    
    # Get file information
    $fileInfo = Get-Item $msiPath
    $fileSizeMB = [math]::Round($fileInfo.Length / 1MB, 2)
    
    Write-Host "File Size: $fileSizeMB MB" -ForegroundColor Yellow
    Write-Host "Created: $($fileInfo.CreationTime)" -ForegroundColor Yellow
    Write-Host "Modified: $($fileInfo.LastWriteTime)" -ForegroundColor Yellow
    
    # Check digital signature
    try {
        $signature = Get-AuthenticodeSignature $msiPath
        if ($signature.Status -eq "Valid") {
            Write-Host "Digital Signature: Valid" -ForegroundColor Green
        } elseif ($signature.Status -eq "NotSigned") {
            Write-Host "Digital Signature: Not signed (normal for development)" -ForegroundColor Yellow
        } else {
            Write-Host "Digital Signature: $($signature.Status)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "Could not check digital signature: $($_.Exception.Message)" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "Installation Commands:" -ForegroundColor Cyan
    Write-Host "  Interactive Install: msiexec /i `"$msiPath`"" -ForegroundColor White
    Write-Host "  Silent Install:      msiexec /i `"$msiPath`" /quiet /norestart" -ForegroundColor White
    Write-Host "  Uninstall:           msiexec /x `"$msiPath`" /quiet /norestart" -ForegroundColor White
    
    Write-Host ""
    Write-Host "SUCCESS: MSI Installer Verification Complete!" -ForegroundColor Green
    Write-Host "The installer is ready for distribution and testing." -ForegroundColor Green
    
} else {
    Write-Host "ERROR: MSI file not found: $msiPath" -ForegroundColor Red
    Write-Host "Please run: ./gradlew packageMsi" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Test installation on a clean Windows system" -ForegroundColor White
Write-Host "2. Verify all application features work correctly" -ForegroundColor White
Write-Host "3. Test the update system functionality" -ForegroundColor White
Write-Host "4. Deploy to production environment" -ForegroundColor White
