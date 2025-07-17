# Application Icons

This directory should contain the application icons for the Sales Management System installer.

## Required Icon Files:

### Windows MSI Installer:
- `app-icon.ico` - Main application icon (16x16, 32x32, 48x48, 256x256 pixels)
  - Used for desktop shortcuts, taskbar, and installer dialogs
  - Should be in ICO format with multiple resolutions

### macOS DMG Installer:
- `app-icon.icns` - macOS application icon bundle
  - Contains multiple resolutions for Retina displays
  - Should follow Apple's icon design guidelines

### Linux DEB Installer:
- `app-icon.png` - PNG format icon (512x512 pixels recommended)
  - Used for application launchers and desktop environments

## Icon Design Guidelines:

1. **Brand Consistency**: Icons should reflect the Sales Management System branding
2. **Arabic RTL Support**: Consider Arabic text direction in icon design
3. **Professional Appearance**: Use business-appropriate colors and styling
4. **Scalability**: Ensure icons look good at all required sizes
5. **Platform Guidelines**: Follow each platform's specific icon guidelines

## Creating Icons:

You can create these icons using:
- Adobe Illustrator/Photoshop
- GIMP (free alternative)
- Online icon generators
- Professional design tools

## Placeholder Icons:

For development purposes, you can use placeholder icons or generate simple ones.
The build system will work without icons, but they enhance the professional appearance.

## Installation:

Once you have the icon files, place them in this directory:
- `installer/icons/app-icon.ico` (Windows)
- `installer/icons/app-icon.icns` (macOS)  
- `installer/icons/app-icon.png` (Linux)

Then uncomment the icon configuration lines in `build.gradle.kts`:
```kotlin
// Windows
iconFile.set(project.file("installer/icons/app-icon.ico"))

// macOS  
iconFile.set(project.file("installer/icons/app-icon.icns"))

// Linux
iconFile.set(project.file("installer/icons/app-icon.png"))
```

## Icon Specifications:

### Windows ICO Format:
- 16x16 pixels (small icons)
- 32x32 pixels (standard icons)
- 48x48 pixels (large icons)
- 256x256 pixels (extra large icons)
- 32-bit color depth with alpha channel

### macOS ICNS Format:
- 16x16@1x, 16x16@2x
- 32x32@1x, 32x32@2x
- 128x128@1x, 128x128@2x
- 256x256@1x, 256x256@2x
- 512x512@1x, 512x512@2x

### Linux PNG Format:
- 512x512 pixels (recommended)
- 32-bit PNG with alpha channel
- Optimized for various desktop environments
