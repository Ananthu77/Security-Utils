# Security Utils

[![](https://jitpack.io/v/yourusername/RootIntegritySDK.svg)](https://jitpack.io/#yourusername/RootIntegritySDK)

A lightweight Android SDK for detecting root, emulator and runtime hooking to enhance app security.

---

## 🚀 **Integration**

### **Step 1. Add JitPack repository**

In your **project-level build.gradle/settings.gradle**:

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
### **Step 2. Add Dependency**

In your **Module-level build.gradle**:

```gradle
implementation 'com.github.Ananthu77:Security-Utils:v1.0.0'
