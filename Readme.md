# Raiden

基于 **Cocos2d-x** 的 Android 游戏项目。

## 项目结构

```
├── app/                # Android 应用模块
├── cocos2dx/           # Cocos2d-x 引擎库
├── gradle/             # Gradle Wrapper 配置
├── build.gradle        # 项目级构建脚本
├── settings.gradle     # Gradle 模块配置
├── gradle.properties   # Gradle 属性
├── local.properties    # 本地 SDK/NDK 路径配置
└── project.properties  # 项目属性
```

## 环境要求

- Android SDK
- Android NDK (r17)
- Gradle

## 构建

```bash
# 使用 Gradle Wrapper 构建
./gradlew assembleDebug
```

## Crash 日志解析

使用 NDK 工具解析 native crash 地址：

```bash
%NDK17%\toolchains\aarch64-linux-android-4.9\prebuilt\windows-x86_64\bin\aarch64-linux-android-addr2line -C -f -e ..\jniLibs\x86\libcocos2dcpp.so <address>
```

## License

详见项目源码。
