# Sales Management System - MSI Installer Implementation Guide

## 🎯 Overview

This guide documents the comprehensive MSI installer implementation for the Sales Management System using WiX Toolset with Gradle integration. The installer provides professional deployment capabilities with Arabic RTL support, digital signing, and enterprise-grade features.

## 🏗️ Architecture

### Components Implemented

1. **Enhanced Gradle Build System**
   - WiX Toolset integration tasks
   - Automated MSI compilation pipeline
   - Digital signing support
   - Silent installation script generation

2. **Professional WiX Configuration**
   - Multi-component MSI structure
   - Arabic RTL localization
   - Custom actions for post-installation setup
   - Registry integration and file associations

3. **Application Integration**
   - Fullscreen mode configuration
   - Backend URL setup
   - Desktop and Start Menu shortcuts
   - Windows Firewall exceptions

## 📋 Build Tasks Reference

### Core Build Tasks

```bash
# Validate WiX installation
./gradlew validateWixInstallation

# Build complete MSI package
./gradlew buildMsi

# Individual build steps
./gradlew prepareMsiBuild      # Prepare build environment
./gradlew compileWixSources    # Compile WiX sources
./gradlew createMsiPackage     # Create MSI package
./gradlew signMsiPackage       # Sign MSI (optional)
```

### PowerShell Build Scripts

```powershell
# Enhanced build with all features
.\build-msi-advanced.ps1

# Clean build
.\build-msi-advanced.ps1 -Clean

# Build with signing
.\build-msi-advanced.ps1 -Sign -CertificatePath "cert.p12"

# Verbose output
.\build-msi-advanced.ps1 -Verbose
```

### Batch File Shortcuts

```cmd
# Simple build
build-msi.bat

# Clean build with signing
build-msi.bat --clean --sign --cert "certificate.p12"
```

## 🌐 Arabic RTL Support

### Localization Features

- **Arabic UI**: Complete installer interface in Arabic
- **RTL Layout**: Proper right-to-left text direction
- **Arabic Fonts**: Tahoma font for optimal rendering
- **Cultural Adaptation**: Arabic naming conventions for shortcuts

### Localization Files

- `installer/wix/Arabic.wxl` - Arabic string resources
- Supports Arabic error messages and progress text
- RTL-aware dialog layouts

### Key Arabic Strings

```xml
<String Id="ApplicationName">نظام إدارة المبيعات</String>
<String Id="WelcomeTitle">مرحباً بك في معالج تثبيت نظام إدارة المبيعات</String>
<String Id="DesktopShortcut">نظام إدارة المبيعات</String>
```

## 🔐 Digital Signing

### Certificate Configuration

Set these Gradle properties for code signing:

```properties
# In gradle.properties
msi.certificate.path=path/to/certificate.p12
msi.certificate.password=certificate_password
msi.timestamp.url=http://timestamp.digicert.com
```

### Signing Process

1. **Automatic Signing**: Runs after MSI creation if certificate is configured
2. **Manual Signing**: Use `signMsiPackage` task
3. **Verification**: Check signature with `signtool verify`

### Certificate Requirements

- **Format**: PKCS#12 (.p12/.pfx)
- **Type**: Code signing certificate
- **Validation**: Extended Validation (EV) recommended
- **Timestamping**: Required for long-term validity

## 📦 Installation Features

### Professional MSI Features

- ✅ **Windows Installer Compliance**: Full MSI standard support
- ✅ **Upgrade Handling**: Automatic version upgrades
- ✅ **Registry Integration**: Proper Windows registry entries
- ✅ **Add/Remove Programs**: Standard uninstall support
- ✅ **File Associations**: .sms file type registration
- ✅ **URL Protocol**: sms:// protocol handler

### Application Configuration

- ✅ **Fullscreen Mode**: Automatic fullscreen setup
- ✅ **Backend Configuration**: Production URL setup
- ✅ **User Preferences**: Application data directory
- ✅ **Firewall Exception**: Network access permissions

### Desktop Integration

- ✅ **Desktop Shortcuts**: Arabic-labeled shortcuts
- ✅ **Start Menu**: Professional menu integration
- ✅ **File Explorer**: Context menu integration
- ✅ **System Tray**: Application registration

## 🛠️ Silent Installation

### Command Line Options

```cmd
# Basic silent installation
msiexec /i "SalesManagementSystem-2.1.0.msi" /quiet /norestart

# Silent installation with custom options
msiexec /i "SalesManagementSystem-2.1.0.msi" /quiet /norestart ^
  FULLSCREEN_MODE=1 ^
  CREATE_DESKTOP_SHORTCUT=1 ^
  CREATE_STARTMENU_SHORTCUT=1 ^
  BACKEND_URL="https://custom-backend.com"

# Silent uninstallation
msiexec /x {B8C9D0E1-F2A3-4B5C-6D7E-8F9A0B1C2D3E} /quiet /norestart
```

### Batch Scripts

Generated automatically during build:

- `install-silent.bat` - Silent installation with defaults
- `uninstall.bat` - Silent uninstallation

### Enterprise Deployment

```cmd
# Domain deployment via Group Policy
msiexec /i "\\server\share\SalesManagementSystem-2.1.0.msi" /quiet /norestart

# SCCM deployment
msiexec /i "SalesManagementSystem-2.1.0.msi" /quiet /norestart /l*v install.log
```

## 🔧 Customization

### Build-Time Configuration

Modify these variables in `build.gradle.kts`:

```kotlin
// Application metadata
val appVersion = "2.1.0"
val appManufacturer = "Hamza Damra"
val upgradeCode = "B8C9D0E1-F2A3-4B5C-6D7E-8F9A0B1C2D3E"

// Default configuration
val defaultBackendUrl = "https://sales-managment-system-backend-springboot.onrender.com"
val defaultFullscreenMode = true
```

### WiX Source Customization

Edit `installer/wix/SalesManagementSystem.wxs`:

- **Components**: Add/remove application components
- **Features**: Configure optional features
- **Registry**: Modify registry entries
- **Shortcuts**: Customize shortcut properties

### Post-Installation Scripts

Modify `installer/scripts/configure-application.ps1`:

- **Configuration**: Application settings
- **Registry**: Additional registry entries
- **Services**: Windows service setup
- **Permissions**: File/folder permissions

## 🧪 Testing

### Verification Checklist

- [ ] **MSI Creation**: Package builds successfully
- [ ] **Installation**: Installs without errors
- [ ] **Application Launch**: Starts in fullscreen mode
- [ ] **Shortcuts**: Desktop and Start Menu shortcuts work
- [ ] **Uninstallation**: Removes all components cleanly
- [ ] **Upgrade**: Handles version upgrades properly
- [ ] **Arabic Support**: UI displays correctly in Arabic
- [ ] **Registry**: All registry entries created
- [ ] **File Associations**: .sms files open correctly

### Test Environments

1. **Clean Windows 10/11**: Fresh installation
2. **Domain Environment**: Active Directory joined
3. **Limited User**: Non-administrator account
4. **Arabic Locale**: Arabic Windows installation
5. **Upgrade Scenario**: Previous version installed

### Automated Testing

```powershell
# Run verification script
.\verify-msi.ps1

# Test installation
.\test-installation.ps1 -MsiPath "build\msi\SalesManagementSystem-2.1.0.msi"
```

## 🚀 Deployment

### Distribution Methods

1. **Direct Download**: Web download portal
2. **Network Share**: Corporate file server
3. **SCCM**: System Center Configuration Manager
4. **Group Policy**: Active Directory deployment
5. **USB/Media**: Offline installation media

### Deployment Best Practices

- **Testing**: Thorough testing in target environment
- **Staging**: Pilot deployment to test group
- **Documentation**: User installation guides
- **Support**: Help desk preparation
- **Rollback**: Uninstallation procedures

## 📞 Troubleshooting

### Common Issues

1. **WiX Not Found**
   - Install WiX Toolset v3.11+
   - Set WIX environment variable

2. **Build Failures**
   - Check Java 17+ installation
   - Verify Gradle configuration
   - Review build logs

3. **Installation Errors**
   - Run as Administrator
   - Check Windows Installer service
   - Review Event Viewer logs

4. **Arabic Display Issues**
   - Verify Arabic language support
   - Check font availability
   - Test on Arabic Windows

### Support Resources

- **Build Logs**: `build/msi-work/` directory
- **Installation Logs**: Windows Event Viewer
- **MSI Logs**: Use `/l*v install.log` parameter
- **Registry**: Check application registry keys

## 📄 License and Credits

This MSI installer implementation is part of the Sales Management System project.

**© 2024 Hamza Damra. All rights reserved.**

### Technologies Used

- **WiX Toolset**: MSI creation framework
- **Gradle**: Build automation
- **PowerShell**: Scripting and automation
- **Windows Installer**: Microsoft installation technology
