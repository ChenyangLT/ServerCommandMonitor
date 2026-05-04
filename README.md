<div align="center">

# 🛡️ ServerCommandMonitor

**全功能 Minecraft 命令审计 & 管理插件**  
*适用于 Paper 1.17.x - 1.21.x

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
   - Paper 1.17.x - 1.21.x（或兼容 API 的 Purpur 等）  
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
编辑 `config.yml`，将 `blacklist.enabled` 设为 `false`：

    blacklist:
      enabled: false

然后执行 `/scm reload` 即可。

### 3️⃣ 开启 BossBar / ActionBar
在 `config.yml` 中添加或修改以下节点：

    bossbar:
      enabled: true
      color: "GREEN"
      message: "&e%player% 执行了命令"

    actionbar:
      enabled: true
      message: "&e>> %player% 使用了 %command%"

### 4️⃣ 自定义黑名单
使用正则表达式匹配整条命令（包含 `/`），不区分大小写：

    blacklist:
      enabled: true
      patterns:
        - "/login.*"
        - "/l( .*)?"
        # 添加屏蔽私聊
        - "/msg.*"

### 5️⃣ 管理员 sudo
执行命令：  
`/scm sudo Alex say 这是管理员强制发送的话`

---

## 🔑 权限

| 节点 | 默认 | 说明 |
|------|------|------|
| `servercommandmonitor.see` | OP | 接收监控消息 (聊天,BossBar,ActionBar) |
| `servercommandmonitor.sudo` | OP | 使用 /scm sudo |
| `servercommandmonitor.reload` | OP | 重载配置 |

---

## 🕹️ 命令

| 指令 | 说明 |
|------|------|
| `/scm reload` | 重载配置 |
| `/scm sudo <玩家> <命令>` | 代理执行命令 |
| `/scm` 或 `/monitorme` | 查看帮助 |

---

## 🧩 PlaceholderAPI 变量

在任何消息格式中可使用 PAPI 变量，例：

    monitor:
      format: "&7[&cCMD&7] &f%player% &7(HP: &c%player_health%&7): &b%command%"

效果：`[CMD] Steve (HP: 20): /gamemode creative`

---

## 🎨 MiniMessage 高级样式

Paper 1.16.5+ 支持鼠标悬浮、点击复制等：

    monitor:
      mode: MINIMESSAGE
      minimessage-format: "<gray>[<red>CMD</red>]</gray> <white><hover:show_text:'<green>点击复制</green>'><click:copy_to_clipboard:'%command%'>%player%</click></hover></white>: <aqua>%command%</aqua>"

---

## ❓ FAQ

<details>
<summary>配置文件是空的？</summary>

删除 `plugins/ServerCommandMonitor` 文件夹，重启服务器让插件重新生成。
</details>

<details>
<summary>BossBar 不显示？</summary>

确保 `bossbar.enabled: true` 且你拥有 `servercommandmonitor.see` 权限。
</details>

<details>
<summary>某些命令没被监控？</summary>

检查黑名单正则是否误匹配；插件只监控玩家聊天框输入的命令，不捕获控制台或 API 调用。
</details>

<details>
<summary>如何关闭日志？</summary>

`logging.enabled: false`
</details>

---

## 🔄 升级

1. 备份原 `config.yml`
2. 删除 `plugins/ServerCommandMonitor` 文件夹
3. 更新插件 jar 并重启
4. 对照备份恢复个性化设置

---

## 📜 开源协议

本项目基于 **MIT** 许可证开源，允许自由使用、修改、分发，包括商业用途。详见 [LICENSE](LICENSE) 文件。

---

## 🤝 参与贡献

感谢 **DeepseekV4** 的智能生成和 **Chen_yang_** 的实地测试。  
欢迎提交 Issue 和 Pull Request。

---

<div align="center">

**让每一条命令，都暴露在阳光之下。** ☀️

</div>
```
