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
```
### **Step 3. Add configuration**

In your **starting Activity/Fragment/Application class add the following
```kotlin
IntegrityScanner.Builder(this)
    .enableRootCheck(true) // enabling the root check
    .enableEmulatorCheck(true) // enabling the emulator detection
    .enableRuntimeHookCheck(true) // enabling if the device with hooks to bypass
    .setCallBack(object : IntegrityCallback {
        override fun onResult(result: DeviceIntegrityCheck) {
            when {
                result.isCompromised -> {
                    // device is not safe
                }
                result.isEmulator -> {
                    // emulator device
                }
                result.isRooted -> {
                    // device is rooted
                }
                result.isRuntimeHooked -> {
                    // device has hooked with dangerous software
                }
                else -> {
                    // handle the case where none of the above conditions are met
                }
            }
        }

    }).build()
    .start()
```

