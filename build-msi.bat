@echo off
REM Advanced MSI Builder for Sales Management System
REM This batch file provides easy access to the PowerShell MSI builder

setlocal EnableDelayedExpansion

echo.
echo ========================================
echo  Sales Management System MSI Builder
echo ========================================
echo.

REM Check for PowerShell
powershell -Command "Get-Host" >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: PowerShell is required but not found.
    echo Please install PowerShell 5.0 or later.
    pause
    exit /b 1
)

REM Parse command line arguments
set CLEAN_BUILD=0
set SIGN_MSI=0
set VERBOSE=0
set CERTIFICATE_PATH=
set CERTIFICATE_PASSWORD=

:parse_args
if "%~1"=="" goto :execute
if /i "%~1"=="--clean" set CLEAN_BUILD=1
if /i "%~1"=="-clean" set CLEAN_BUILD=1
if /i "%~1"=="--sign" set SIGN_MSI=1
if /i "%~1"=="-sign" set SIGN_MSI=1
if /i "%~1"=="--verbose" set VERBOSE=1
if /i "%~1"=="-verbose" set VERBOSE=1
if /i "%~1"=="--cert" (
    shift
    set CERTIFICATE_PATH=%~1
)
if /i "%~1"=="-cert" (
    shift
    set CERTIFICATE_PATH=%~1
)
if /i "%~1"=="--help" goto :show_help
if /i "%~1"=="-help" goto :show_help
if /i "%~1"=="-h" goto :show_help
if /i "%~1"=="/?" goto :show_help
shift
goto :parse_args

:execute
REM Build PowerShell command
set PS_COMMAND=.\build-msi-simple.ps1

if %CLEAN_BUILD%==1 (
    set PS_COMMAND=!PS_COMMAND! -Clean
)

if %SIGN_MSI%==1 (
    set PS_COMMAND=!PS_COMMAND! -Sign
)

if %VERBOSE%==1 (
    set PS_COMMAND=!PS_COMMAND! -Verbose
)

if not "%CERTIFICATE_PATH%"=="" (
    set PS_COMMAND=!PS_COMMAND! -CertificatePath "%CERTIFICATE_PATH%"
)

REM Execute PowerShell script
echo Executing: %PS_COMMAND%
echo.
powershell -ExecutionPolicy Bypass -File %PS_COMMAND%

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo  MSI Build Completed Successfully!
    echo ========================================
    echo.
    echo The MSI installer is ready for deployment.
    echo Check the build summary above for file locations.
) else (
    echo.
    echo ========================================
    echo  MSI Build Failed
    echo ========================================
    echo.
    echo Please check the error messages above.
    echo Try running with --verbose for more details.
)

echo.
pause
exit /b %errorlevel%

:show_help
echo.
echo Usage: build-msi.bat [options]
echo.
echo Options:
echo   --clean, -clean       Clean build environment before building
echo   --sign, -sign         Sign the MSI package (requires certificate)
echo   --verbose, -verbose   Enable verbose output
echo   --cert PATH, -cert    Path to code signing certificate
echo   --help, -help, -h, /? Show this help message
echo.
echo Examples:
echo   build-msi.bat                           # Standard build
echo   build-msi.bat --clean                   # Clean build
echo   build-msi.bat --sign --cert cert.p12   # Build and sign
echo   build-msi.bat --clean --verbose         # Clean build with verbose output
echo.
echo For more advanced options, use the PowerShell script directly:
echo   powershell -File build-msi-advanced.ps1 -Help
echo.
pause
exit /b 0
