# Eras 二游框架

一个基于 JavaFX 的 AI 二游框架，支持乙游和 Galgame 风格的聊天交互。

## 快速开始

双击 `dist/exe/Eras_APP/Eras.exe` 即可运行。

## 配置 API Key

编辑 `dist/exe/Eras_APP/options/api.json`，把 `api_key` 改成你的 Key：

```json
{
  "provider": "openai",
  "api_key": "sk-你的key",
  "api_url": "https://api.openai.com/v1",
  "model": "gpt-4"
}
```

也支持本地 Ollama：

```json
{
  "provider": "ollama",
  "api_key": "",
  "api_url": "http://localhost:11434",
  "model": "qwen2.5"
}
```

## 目录结构

```
Eras 二游框架/
├── javasrc/          ← Java 源代码
│   └── src/main/java/com/eras/
│       ├── ErasApp.java         # 入口
│       ├── controller/          # 控制器
│       ├── model/               # 数据模型
│       ├── service/             # 服务层
│       ├── ui/                  # 界面组件
│       └── util/                # 工具类
├── 构建/
│   ├── 构建exe.bat   ← 双击重新编译+打包EXE
│   ├── eras-framework-1.0.0.jar
│   └── javafx-lib/   ← JavaFX 运行时库
└── dist/exe/Eras_APP/
    ├── Eras.exe       ← 主程序（双击运行）
    ├── app/           ← JAR
    ├── runtime/       ← 内置JRE
    ├── options/       ← 配置文件
    ├── memory/        ← 记忆文件
    ├── picture/       ← 背景/角色图片
    └── prompt/        ← AI提示词
```

## 功能

- 角色立绘即背景（全屏显示，支持PNG/JPG/GIF/Live2D预留）
- 沉浸式半透明聊天界面（无人头像）
- 左上角菜单：换角色、调颜色、改称呼
- 记忆系统：长期记忆 + 短期自动总结
- 识图功能：上传图片给 AI 分析
- 提示词系统：编辑 `prompt/core.md` 自定义角色设定

## 构建方法

1. 安装 JDK 21 和 Maven
2. 双击 `构建/构建exe.bat` 自动编译+打包EXE
