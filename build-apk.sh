#!/bin/bash

# UNLIMITED DATA BYPASS APK BUILD SCRIPT
# Created by T3rmuxk1ng
# GOD TIER EDITION

echo "=========================================="
echo "⚡ UNLIMITED DATA BYPASS - BUILD SCRIPT"
echo "=========================================="
echo ""

# Check for required tools
check_requirements() {
    echo "Checking requirements..."
    
    # Check Java
    if command -v java &> /dev/null; then
        echo "✅ Java: $(java -version 2>&1 | head -1)"
    else
        echo "❌ Java not found! Please install JDK 17+"
        exit 1
    fi
    
    # Check Android SDK
    if [ -z "$ANDROID_HOME" ]; then
        echo "⚠️ ANDROID_HOME not set!"
        echo "Please install Android SDK and set ANDROID_HOME"
        echo ""
        echo "Example:"
        echo "  export ANDROID_HOME=/path/to/android-sdk"
        echo "  export PATH=\$PATH:\$ANDROID_HOME/tools:\$ANDROID_HOME/platform-tools"
        exit 1
    else
        echo "✅ Android SDK: $ANDROID_HOME"
    fi
    
    # Check build tools
    if [ -d "$ANDROID_HOME/build-tools" ]; then
        BUILD_TOOLS=$(ls $ANDROID_HOME/build-tools | sort -V | tail -1)
        echo "✅ Build Tools: $BUILD_TOOLS"
    else
        echo "❌ Build tools not found!"
        echo "Run: sdkmanager 'build-tools;34.0.0'"
        exit 1
    fi
    
    # Check platform tools
    if [ -d "$ANDROID_HOME/platforms" ]; then
        PLATFORM=$(ls $ANDROID_HOME/platforms | sort -V | tail -1)
        echo "✅ Platform: $PLATFORM"
    else
        echo "❌ Platform not found!"
        echo "Run: sdkmanager 'platforms;android-34'"
        exit 1
    fi
}

# Build debug APK
build_debug() {
    echo ""
    echo "🔨 Building Debug APK..."
    
    # Make gradlew executable
    chmod +x gradlew 2>/dev/null
    
    # Build using Gradle
    if [ -f "gradlew" ]; then
        ./gradlew assembleDebug
    elif command -v gradle &> /dev/null; then
        gradle assembleDebug
    else
        echo "❌ Gradle not found!"
        echo "Please install Gradle or use Android Studio"
        exit 1
    fi
    
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        echo ""
        echo "✅ Build successful!"
        echo "📱 APK Location: app/build/outputs/apk/debug/app-debug.apk"
    else
        echo "❌ Build failed!"
        exit 1
    fi
}

# Build release APK (signed)
build_release() {
    echo ""
    echo "🔨 Building Release APK..."
    
    # Check for keystore
    if [ ! -f "release.keystore" ]; then
        echo "Creating release keystore..."
        keytool -genkey -v -keystore release.keystore \
            -alias unlimited_bypass \
            -keyalg RSA -keysize 2048 -validity 10000 \
            -storepass t3rmuxk1ng \
            -keypass t3rmuxk1ng \
            -dname "CN=T3rmuxk1ng, OU=Security, O=GODTIER, L=India, ST=India, C=IN"
    fi
    
    # Make gradlew executable
    chmod +x gradlew 2>/dev/null
    
    # Build using Gradle
    if [ -f "gradlew" ]; then
        ./gradlew assembleRelease
    elif command -v gradle &> /dev/null; then
        gradle assembleRelease
    else
        echo "❌ Gradle not found!"
        exit 1
    fi
    
    if [ -f "app/build/outputs/apk/release/app-release-unsigned.apk" ]; then
        # Sign the APK
        echo "Signing APK..."
        BUILD_TOOLS=$(ls $ANDROID_HOME/build-tools | sort -V | tail -1)
        
        # Align
        $ANDROID_HOME/build-tools/$BUILD_TOOLS/zipalign -v -p 4 \
            app/build/outputs/apk/release/app-release-unsigned.apk \
            app/build/outputs/apk/release/app-aligned.apk
        
        # Sign
        $ANDROID_HOME/build-tools/$BUILD_TOOLS/apksigner sign \
            --ks release.keystore \
            --ks-key-alias unlimited_bypass \
            --ks-pass pass:t3rmuxk1ng \
            --key-pass pass:t3rmuxk1ng \
            --out app/build/outputs/apk/release/UNLIMITED-DATA-BYPASS-v1.0-SIGNED.apk \
            app/build/outputs/apk/release/app-aligned.apk
        
        echo ""
        echo "✅ Release build successful!"
        echo "📱 Signed APK: app/build/outputs/apk/release/UNLIMITED-DATA-BYPASS-v1.0-SIGNED.apk"
    else
        echo "❌ Build failed!"
        exit 1
    fi
}

# Main menu
show_menu() {
    echo ""
    echo "Select build type:"
    echo "1. Debug APK (unsigned, for testing)"
    echo "2. Release APK (signed, for distribution)"
    echo "3. Check requirements only"
    echo "4. Exit"
    echo ""
    read -p "Enter choice (1-4): " choice
    
    case $choice in
        1) check_requirements && build_debug ;;
        2) check_requirements && build_release ;;
        3) check_requirements ;;
        4) echo "Goodbye!"; exit 0 ;;
        *) echo "Invalid choice!"; show_menu ;;
    esac
}

# Run
if [ "$1" == "debug" ]; then
    check_requirements && build_debug
elif [ "$1" == "release" ]; then
    check_requirements && build_release
elif [ "$1" == "check" ]; then
    check_requirements
else
    show_menu
fi
