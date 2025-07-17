# Sales Management System - MSI Installer

This directory contains the configuration and resources for building a professional MSI installer for the Sales Management System using Kotlin Compose Desktop and WiX Toolset integration.

## 🚀 Quick Start

### Prerequisites

1. **Java 17+** - Required for building the application
2. **Gradle** - Build system (included via Gradle Wrapper)
3. **WiX Toolset v3.11** (Optional but recommended) - For advanced MSI features
   - Download from: https://wixtoolset.org/releases/
   - Install to default location: `C:\Program Files (x86)\WiX Toolset v3.11\`

### Building the MSI Installer

1. **Simple Build:**
   ```powershell
   .\build-msi.ps1
   ```

2. **Clean Build:**
   ```powershell
   .\build-msi.ps1 -Clean
   ```

3. **Manual Gradle Build:**
   ```powershell
   .\gradlew packageMsi
   ```

### Installation

The generated MSI installer will be located at:
```
build/compose/binaries/main/msi/SalesManagementSystem-2.1.0.msi
```

**Installation Commands:**
- **Interactive Install:** `msiexec /i "SalesManagementSystem-2.1.0.msi"`
- **Silent Install:** `msiexec /i "SalesManagementSystem-2.1.0.msi" /quiet /norestart`
- **Uninstall:** `msiexec /x "SalesManagementSystem-2.1.0.msi" /quiet /norestart`

## 📋 Features

### ✅ Implemented Features

1. **Professional MSI Installer**
   - Windows Installer (MSI) format
   - Proper versioning and upgrade handling
   - Clean installation and uninstallation

2. **Application Configuration**
   - Fullscreen-only mode support
   - Production backend URL configuration
   - Registry entries for application registration

3. **Desktop Integration**
   - Desktop shortcut creation
   - Start Menu integration
   - Proper Windows registry entries

4. **Branding & Metadata**
   - Professional installer appearance
   - Application metadata and descriptions
   - License agreement integration

5. **Error Handling**
   - Comprehensive error handling during installation
   - Rollback capabilities on installation failure
   - Proper cleanup on uninstallation

6. **Compatibility**
   - Windows 10/11 support
   - Both 32-bit and 64-bit systems
   - Proper dependency management

### 🔧 Configuration

#### Backend URL Configuration
The installer automatically configures the application to use the production backend:
```
https://sales-managment-system-backend-springboot.onrender.com
```

This is set in:
- Application configuration files
- Windows registry entries
- Build configuration

#### Fullscreen Mode
The application is configured to launch in fullscreen mode by default:
- Command-line argument: `--fullscreen`
- Registry setting: `HKLM\SOFTWARE\HamzaDamra\SalesManagementSystem\FullscreenMode = 1`
- System property: `app.fullscreen=true`

## 📁 Directory Structure

```
installer/
├── LICENSE.txt                 # License agreement
├── README.md                   # This file
├── icons/                      # Application icons
│   ├── app-icon.ico           # Windows icon
│   ├── app-icon.png           # Linux icon
│   ├── app-icon.icns          # macOS icon
│   └── create-icons.ps1       # Icon generation script
├── scripts/                    # Installation scripts
│   └── configure-application.ps1  # Post-install configuration
└── wix/                        # WiX Toolset configuration
    ├── SalesManagementSystem.wxs  # Main WiX source file
    └── Arabic.wxl              # Arabic localization (unused)
```

## 🛠️ Build Configuration

### Gradle Configuration (build.gradle.kts)

The MSI installer is configured in the `compose.desktop.application.nativeDistributions.windows` block:

```kotlin
windows {
    iconFile.set(project.file("installer/icons/app-icon.ico"))
    menuGroup = "Sales Management System"
    upgradeUuid = "B8C9D0E1-F2A3-4B5C-6D7E-8F9A0B1C2D3E"
    msiPackageVersion = "2.1.0"
    dirChooser = true
    perUserInstall = false
    shortcut = true
    console = false
    
    // Custom MSI properties
    msiProperty("BACKEND_URL", "https://sales-managment-system-backend-springboot.onrender.com")
    msiProperty("FULLSCREEN_MODE", "1")
    msiProperty("INSTALL_REGISTRY_ENTRIES", "1")
    msiProperty("CREATE_DESKTOP_SHORTCUT", "1")
}
```

### Registry Entries

The installer creates the following registry entries:

**Application Registration:**
```
HKLM\SOFTWARE\HamzaDamra\SalesManagementSystem\
├── InstallPath = "C:\Program Files\Sales Management System"
├── Version = "2.1.0"
├── DisplayName = "Sales Management System"
├── Publisher = "Hamza Damra"
├── BackendURL = "https://sales-managment-system-backend-springboot.onrender.com"
└── FullscreenMode = 1
```

**Uninstall Information:**
```
HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\SalesManagementSystem\
├── DisplayName = "Sales Management System"
├── DisplayVersion = "2.1.0"
├── Publisher = "Hamza Damra"
├── InstallLocation = "C:\Program Files\Sales Management System"
└── UninstallString = "msiexec /x {GUID}"
```

## 🎨 Icons

### Creating Application Icons

1. **Automatic Generation (Basic):**
   ```powershell
   .\installer\icons\create-icons.ps1
   ```

2. **Manual Creation:**
   - Create professional icons using design tools
   - Required formats: ICO (Windows), PNG (Linux), ICNS (macOS)
   - Recommended sizes: 16x16, 32x32, 48x48, 256x256

3. **Icon Requirements:**
   - Professional business appearance
   - Scalable design for all sizes
   - Consistent branding
   - High contrast for visibility

## 🧪 Testing

### Verification Script

Run the verification script to check the generated MSI:
```powershell
.\verify_msi.ps1
```

This script checks:
- MSI file existence and properties
- Digital signature status
- Installation commands
- File size and metadata

### Manual Testing

1. **Clean System Test:**
   - Test on a clean Windows system
   - Verify all features work correctly
   - Test fullscreen mode functionality

2. **Upgrade Testing:**
   - Install previous version
   - Install new version (should upgrade)
   - Verify settings are preserved

3. **Uninstall Testing:**
   - Complete uninstallation
   - Verify all files and registry entries are removed
   - Check for leftover artifacts

## 🚀 Deployment

### Distribution

1. **File Sharing:**
   - Upload MSI to file sharing service
   - Provide download links to users

2. **Enterprise Deployment:**
   - Use Group Policy for domain deployment
   - SCCM/WSUS integration
   - Silent installation scripts

3. **Digital Signing (Recommended):**
   - Sign MSI with code signing certificate
   - Improves trust and security
   - Reduces Windows SmartScreen warnings

### System Requirements

- **Operating System:** Windows 10 version 1809 or later
- **Architecture:** x64 (64-bit)
- **Memory:** 4 GB RAM minimum, 8 GB recommended
- **Storage:** 500 MB available space
- **Network:** Internet connection for backend connectivity
- **Java Runtime:** Bundled with application (no separate installation required)

## 🔧 Troubleshooting

### Common Issues

1. **Build Fails:**
   - Ensure Java 17+ is installed
   - Check Gradle wrapper permissions
   - Verify all dependencies are available

2. **WiX Toolset Not Found:**
   - Install WiX Toolset v3.11
   - Verify installation path
   - Update build configuration if needed

3. **Icons Missing:**
   - Run icon creation script
   - Manually create icon files
   - Build will work without icons (reduced visual appeal)

4. **Installation Fails:**
   - Run as Administrator
   - Check Windows Installer service
   - Verify system requirements

### Support

For technical support or issues:
- Email: hamza.damra@example.com
- GitHub Issues: [Repository URL]
- Documentation: This README file

## 📝 License

This installer and application are proprietary software. See `LICENSE.txt` for full license terms.

---

**Sales Management System v2.1.0**  
© 2024 Hamza Damra. All rights reserved.
