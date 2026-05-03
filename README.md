<div align="center">

# 🛡️ ServerCommandMonitor

**全功能 Minecraft 命令审计 & 管理插件**  
*适用于 Paper 1.21.x（1.21.4 及未来小版本）*

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![MC](https://img.shields.io/badge/Minecraft-1.21.x-brightgreen)](https://papermc.io)
[![Issues](https://img.shields.io/github/issues/你的用户名/ServerCommandMonitor)](https://github.com/你的用户名/ServerCommandMonitor/issues)

</div>

---

## 🧠 开发与致谢

> 🧬 本插件核心代码由 **[DeepseekV4](https://deepseek.com)** 生成  
> 🔧 经 **Chen_yang_** 在实际服务器环境中**调试、配置补全与性能优化**  

AI 灵感 + 人工打磨，保障生产可用。

---

## 📖 简介

随时掌握玩家在服务器上执行了哪些命令，是服主和管理员的基本需求。  
**ServerCommandMonitor** 提供一套全方位的解决方案：

- 📬 **聊天框** 实时转发命令
- 🖥️ **BossBar** 顶部进度条提醒
- 💬 **ActionBar** 物品栏上行轻提示
- 📝 **日志** 文件审计追踪
- 👤 **sudo** 代理命令执行
- 🛡️ **配置化黑名单** 保护敏感指令
- 🧩 **PlaceholderAPI** 集成
- 🎨 **双显示引擎**（经典 & MiniMessage）
- ⚡ **热重载** 无需重启

---

## 📦 安装

1. **环境**  
   - Paper 1.21.x（或兼容 API 的 Purpur 等）  
   - Java 21  
   - (可选) [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

2. **下载**  
   从 [Releases](https://github.com/你的用户名/ServerCommandMonitor/releases) 获取最新 `.jar`

3. **部署**  
   放入 `plugins/` 后重启服务器。  
   首次运行将自动生成 `plugins/ServerCommandMonitor/config.yml`

> ⚠️ 若配置文件为空，请删除 `plugins/ServerCommandMonitor` 文件夹后重启

---

## ⚙️ 快速上手

### 1️⃣ 开箱即用
安装后，所有 OP 都会在聊天框看到命令监控，格式如下：  
`[CMD] Steve: /gamemode creative`

### 2️⃣ 关闭黑名单（完全公开）
编辑 `config.yml`：
```yaml
blacklist:
  enabled: false
