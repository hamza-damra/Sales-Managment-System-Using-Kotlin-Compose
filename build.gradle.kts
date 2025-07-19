import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization") version "1.9.21"
}

group = "com.hamzadamra.salesmanagement"
version = "2.1.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

    // HTTP Client and Networking
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")
    implementation("io.ktor:ktor-client-auth:2.3.7")

    // JSON Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Preferences/Settings Storage
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Excel Export - Apache POI (consistent versions)
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")
    // Removed poi-ooxml-schemas as it's included in poi-ooxml 5.2.4

    // PDF Generation - iText
    implementation("com.itextpdf:itext7-core:7.2.5")
    implementation("com.itextpdf:html2pdf:4.0.5")

    // PDF Rendering - Apache PDFBox for PDF viewing
    implementation("org.apache.pdfbox:pdfbox:2.0.29")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.29")

    // File operations - Desktop integration for file dialogs

    // Logging dependencies to fix warnings
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("org.apache.logging.log4j:log4j-core:2.21.1")

    // Color Picker - Skydoves Compose Color Picker for Kotlin Multiplatform
    implementation("com.github.skydoves:colorpicker-compose:1.1.2")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.ktor:ktor-client-mock:2.3.7")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.21")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Dmg, TargetFormat.Deb)
            packageName = "SalesManagementSystem"
            packageVersion = "2.1.0"
            description = "Professional Sales Management System with Enhanced Update Functionality"
            copyright = "© 2024 Hamza Damra. All rights reserved."
            vendor = "Hamza Damra"
            licenseFile.set(project.file("installer/LICENSE.txt"))

            // Application metadata
            modules("java.base", "java.desktop", "java.logging", "java.naming", "java.security.jgss", "java.sql")

            // Windows MSI specific configuration
            windows {
                // iconFile.set(project.file("installer/icons/app-icon.ico")) // TODO: Add icon file
                menuGroup = "Sales Management System"
                // Upgrade UUID for proper MSI upgrade handling
                upgradeUuid = "B8C9D0E1-F2A3-4B5C-6D7E-8F9A0B1C2D3E"

                // MSI installer properties
                msiPackageVersion = "2.1.0"
                dirChooser = true
                perUserInstall = false
                shortcut = true

                // Additional MSI properties
                console = false
            }

            // macOS DMG configuration
            macOS {
                // iconFile.set(project.file("installer/icons/app-icon.icns")) // TODO: Add icon file
                bundleID = "com.hamzadamra.salesmanagement"
                appCategory = "public.app-category.business"
                // entitlementsFile.set(project.file("installer/macOS/entitlements.plist"))
                // runtimeEntitlementsFile.set(project.file("installer/macOS/runtime-entitlements.plist"))
            }

            // Linux DEB configuration
            linux {
                // iconFile.set(project.file("installer/icons/app-icon.png")) // TODO: Add icon file
                packageName = "sales-management-system"
                debMaintainer = "hamza.damra@example.com"
                menuGroup = "Office"
                appRelease = "1"
                appCategory = "Office"
                shortcut = true
            }

            // Include additional resources
            includeAllModules = true
        }

        buildTypes.release.proguard {
            configurationFiles.from("installer/proguard-rules.pro")
        }
    }
}

// ================================
// WiX Toolset MSI Build Integration
// ================================

// WiX Toolset configuration
val wixToolsetPath = System.getenv("WIX") ?: "C:\\Program Files (x86)\\WiX Toolset v3.11\\bin"
val wixCandle = "$wixToolsetPath\\candle.exe"
val wixLight = "$wixToolsetPath\\light.exe"

// MSI build directories
val msiOutputDir = layout.buildDirectory.dir("msi")
val msiWorkDir = layout.buildDirectory.dir("msi-work")
val distributionDir = layout.buildDirectory.dir("compose/binaries/main/msi")

// Task to prepare MSI build environment
tasks.register("prepareMsiBuild") {
    group = "installer"
    description = "Prepares the MSI build environment and copies required files"

    dependsOn("packageDistributionForCurrentOS")

    doLast {
        // Create MSI work directories
        msiWorkDir.get().asFile.mkdirs()
        msiOutputDir.get().asFile.mkdirs()

        // Copy WiX source files
        copy {
            from("installer/wix")
            into(msiWorkDir.get().asFile)
        }

        // Copy PowerShell scripts
        copy {
            from("installer/scripts")
            into(msiWorkDir.get().dir("scripts").asFile)
        }

        // Copy icons if they exist
        if (file("installer/icons").exists()) {
            copy {
                from("installer/icons")
                into(msiWorkDir.get().dir("icons").asFile)
            }
        }

        // Copy license file
        copy {
            from("installer/LICENSE.txt")
            into(msiWorkDir.get().asFile)
            rename { "LICENSE.rtf" }
        }

        println("MSI build environment prepared in: ${msiWorkDir.get().asFile}")
    }
}

// Task to compile WiX source files
tasks.register<Exec>("compileWixSources") {
    group = "installer"
    description = "Compiles WiX source files using candle.exe"

    dependsOn("prepareMsiBuild")

    workingDir = msiWorkDir.get().asFile

    commandLine = listOf(
        wixCandle,
        "-nologo",
        "-arch", "x64",
        "-dSourceDir=${distributionDir.get().asFile.absolutePath}",
        "-dVersion=2.1.0",
        "-dManufacturer=Hamza Damra",
        "-dProductName=Sales Management System",
        "-dUpgradeCode=B8C9D0E1-F2A3-4B5C-6D7E-8F9A0B1C2D3E",
        "-ext", "WixUIExtension",
        "-ext", "WixUtilExtension",
        "-loc", "Arabic.wxl",
        "SalesManagementSystem.wxs"
    )

    doFirst {
        println("Compiling WiX sources...")
        println("Working directory: $workingDir")
        println("Distribution directory: ${distributionDir.get().asFile.absolutePath}")
    }
}

// Task to link and create MSI package
tasks.register<Exec>("createMsiPackage") {
    group = "installer"
    description = "Links compiled WiX objects and creates the final MSI package"

    dependsOn("compileWixSources")

    workingDir = msiWorkDir.get().asFile

    commandLine = listOf(
        wixLight,
        "-nologo",
        "-ext", "WixUIExtension",
        "-ext", "WixUtilExtension",
        "-cultures:ar-SA",
        "-loc", "Arabic.wxl",
        "-out", "${msiOutputDir.get().asFile.absolutePath}\\SalesManagementSystem-2.1.0.msi",
        "SalesManagementSystem.wixobj"
    )

    doFirst {
        println("Creating MSI package...")
    }

    doLast {
        println("MSI package created: ${msiOutputDir.get().asFile.absolutePath}\\SalesManagementSystem-2.1.0.msi")
    }
}

// Task to validate WiX installation
tasks.register("validateWixInstallation") {
    group = "installer"
    description = "Validates that WiX Toolset is properly installed"

    doLast {
        val candleFile = file(wixCandle)
        val lightFile = file(wixLight)

        if (!candleFile.exists()) {
            throw GradleException("WiX candle.exe not found at: $wixCandle\nPlease install WiX Toolset v3.11 or set WIX environment variable")
        }

        if (!lightFile.exists()) {
            throw GradleException("WiX light.exe not found at: $wixLight\nPlease install WiX Toolset v3.11 or set WIX environment variable")
        }

        println("✅ WiX Toolset found at: $wixToolsetPath")
    }
}

// Task to create application icons (placeholder)
tasks.register("createApplicationIcons") {
    group = "installer"
    description = "Creates application icons for the installer"

    doLast {
        val iconsDir = file("installer/icons")
        iconsDir.mkdirs()

        // Create placeholder icon files if they don't exist
        val iconFiles = listOf("app-icon.ico", "banner.bmp", "dialog.bmp")
        iconFiles.forEach { iconFile ->
            val iconPath = file("installer/icons/$iconFile")
            if (!iconPath.exists()) {
                println("⚠️  Icon file missing: $iconFile")
                println("   Please add this file to installer/icons/ directory")
            }
        }
    }
}

// Task to sign MSI package (requires certificate)
tasks.register<Exec>("signMsiPackage") {
    group = "installer"
    description = "Signs the MSI package with digital certificate"

    dependsOn("createMsiPackage")

    val certificatePath = project.findProperty("msi.certificate.path") as String?
    val certificatePassword = project.findProperty("msi.certificate.password") as String?
    val timestampUrl = project.findProperty("msi.timestamp.url") as String? ?: "http://timestamp.digicert.com"

    onlyIf { certificatePath != null && file(certificatePath).exists() }

    workingDir = msiOutputDir.get().asFile

    commandLine = listOf(
        "signtool.exe",
        "sign",
        "/f", certificatePath ?: "",
        "/p", certificatePassword ?: "",
        "/t", timestampUrl,
        "/d", "Sales Management System",
        "/du", "https://github.com/hamzadamra/sales-management-system",
        "SalesManagementSystem-2.1.0.msi"
    )

    doFirst {
        if (certificatePath == null) {
            println("⚠️  No certificate path specified. Set msi.certificate.path property to sign the MSI.")
        } else {
            println("🔐 Signing MSI package with certificate: $certificatePath")
        }
    }

    doLast {
        println("✅ MSI package signed successfully")
    }
}

// Task to create silent installation script
tasks.register("createSilentInstaller") {
    group = "installer"
    description = "Creates a silent installation script"

    dependsOn("createMsiPackage")

    doLast {
        val silentInstallScript = """
@echo off
echo Installing Sales Management System silently...
echo.

REM Check for administrator privileges
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Administrator privileges confirmed.
) else (
    echo This installer requires administrator privileges.
    echo Please run as administrator.
    pause
    exit /b 1
)

REM Install the MSI package silently
msiexec /i "SalesManagementSystem-2.1.0.msi" /quiet /norestart FULLSCREEN_MODE=1 CREATE_DESKTOP_SHORTCUT=1 CREATE_STARTMENU_SHORTCUT=1

if %errorLevel% == 0 (
    echo.
    echo ✅ Sales Management System installed successfully!
    echo.
    echo The application has been configured for:
    echo   - Fullscreen mode
    echo   - Desktop shortcut
    echo   - Start Menu shortcut
    echo.
    echo You can now launch the application from the desktop or Start Menu.
) else (
    echo.
    echo ❌ Installation failed with error code: %errorLevel%
    echo Please check the Windows Event Log for more details.
)

echo.
pause
        """.trimIndent()

        val scriptFile = file("${msiOutputDir.get().asFile.absolutePath}/install-silent.bat")
        scriptFile.writeText(silentInstallScript)

        println("📝 Silent installation script created: ${scriptFile.absolutePath}")
    }
}

// Task to create uninstall script
tasks.register("createUninstaller") {
    group = "installer"
    description = "Creates an uninstall script"

    dependsOn("createMsiPackage")

    doLast {
        val uninstallScript = """
@echo off
echo Uninstalling Sales Management System...
echo.

REM Check for administrator privileges
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Administrator privileges confirmed.
) else (
    echo This uninstaller requires administrator privileges.
    echo Please run as administrator.
    pause
    exit /b 1
)

REM Uninstall using product code
msiexec /x {B8C9D0E1-F2A3-4B5C-6D7E-8F9A0B1C2D3E} /quiet /norestart

if %errorLevel% == 0 (
    echo.
    echo ✅ Sales Management System uninstalled successfully!
    echo.
    echo All application files and registry entries have been removed.
    echo User data and configuration files have been preserved.
) else (
    echo.
    echo ❌ Uninstallation failed with error code: %errorLevel%
    echo Please try uninstalling through Windows Add/Remove Programs.
)

echo.
pause
        """.trimIndent()

        val scriptFile = file("${msiOutputDir.get().asFile.absolutePath}/uninstall.bat")
        scriptFile.writeText(uninstallScript)

        println("📝 Uninstall script created: ${scriptFile.absolutePath}")
    }
}

// Main MSI build task
tasks.register("buildMsi") {
    group = "installer"
    description = "Builds the complete MSI installer package with all components"

    dependsOn(
        "validateWixInstallation",
        "createApplicationIcons",
        "createMsiPackage",
        "createSilentInstaller",
        "createUninstaller"
    )

    // Optionally sign if certificate is available
    finalizedBy("signMsiPackage")

    doLast {
        println("\n🎉 MSI installer build completed successfully!")
        println("=".repeat(60))
        println("📦 MSI Package: ${msiOutputDir.get().asFile.absolutePath}\\SalesManagementSystem-2.1.0.msi")
        println("🔧 Build Artifacts: ${msiWorkDir.get().asFile.absolutePath}")
        println("📝 Silent Installer: ${msiOutputDir.get().asFile.absolutePath}\\install-silent.bat")
        println("🗑️  Uninstaller: ${msiOutputDir.get().asFile.absolutePath}\\uninstall.bat")
        println("=".repeat(60))
        println("\n📋 Installation Features:")
        println("   ✅ Arabic RTL UI support")
        println("   ✅ Fullscreen mode configuration")
        println("   ✅ Desktop and Start Menu shortcuts")
        println("   ✅ Windows registry integration")
        println("   ✅ File associations (.sms files)")
        println("   ✅ Automatic backend configuration")
        println("   ✅ Professional uninstaller")
        println("   ✅ Silent installation support")
        println("\n🚀 Ready for deployment!")
    }
}
