#!/bin/bash

# 设置Android Studio JBR路径作为JAVA_HOME
export JAVA_HOME="/home/xna/Downloads/android-studio-2025.1.4.8-linux/android-studio/jbr"

echo "Using JAVA_HOME: $JAVA_HOME"

# 执行原始的gradlew命令
./gradlew "$@"