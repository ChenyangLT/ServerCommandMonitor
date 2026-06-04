<div align="center">

# 🛡️ ServerCommandMonitor 2.3.2

**全功能 Minecraft 命令审计 & 管理插件**  
*适用于 Spigot / Paper / Purpur / Leaves 1.17.x - 1.21.x*

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

**ServerCommandMonitor** 是一款强大的命令审计与管理工具，帮助服主实时掌控服务器内所有玩家指令，并提供了丰富的辅助管理功能。

- 📬 **聊天框** 实时转发命令
- 🖥️ **BossBar** 顶部进度条提醒
- 💬 **ActionBar** 物品栏上行轻提示
- 📝 **日志** 文件审计追踪（全局 + 按玩家分类）
- 👤 **sudo** 代理命令执行
- 🛡️ **配置化黑名单** 保护敏感指令
- 🚫 **指令封禁系统** 阻止玩家使用特定命令
- 🔍 **监视列表** 灵活指定监控对象
- 📜 **命令历史查询（分页）** 随时回溯玩家操作
- 🤖 **AI 分析** 智能评估命令风险并给出建议
- 🔄 **自动更新检测** 启动及手动检查新版本
- 🧩 **PlaceholderAPI** 集成
- 🎨 **双显示引擎**（经典 & MiniMessage）
- ⚡ **热重载** 无需重启

---

## 📦 安装

1. **环境**  
   - Spigot / Paper / Purpur / Leaves 1.17.x - 1.21.x  
   - Java 17 或更高版本  
   - (可选) [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) 以使用额外变量  
   - (可选) OpenAI 兼容 API 密钥 用于 AI 分析

2. **下载**  
   从 [Releases](https://github.com/你的用户名/ServerCommandMonitor/releases) 获取最新 `ServerCommandMonitor-x.x.x.jar`

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

然后执行 `/scm reload`。

### 3️⃣ 开启 BossBar / ActionBar
在 `config.yml` 中修改：

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
`/scm sudo Alex say 这是管理员强制发送的话`

### 6️⃣ 监视指定玩家
`/scm monitor Steve Alex` — 只监视 Steve 和 Alex  
`/scm monitor` — 查看当前监视列表  
`/scm monitor all` — 恢复监视所有人

### 7️⃣ 查询命令历史
`/scm lookup Steve 1` — 查看 Steve 的命令记录第 1 页（每页 10 条）  
点击页码按钮翻页，命令可点击复制，每条记录均附带 `[询问AI]` 与 `[封禁该指令]` 按钮。

### 8️⃣ AI 分析命令
点击 `[询问AI]` 或手动执行 `/scm ai Steve /give @p diamond 64`  
AI 将返回：
【指令作用】 ...
【风险评分】 85/100
【处理建议】 ...


### 9️⃣ 封禁指令
- **手动**：`/scm blockcmd give` 封禁 `/give`
- **交互**：在查询结果中点击 `[封禁该指令]`，聊天框会自动填入 `./give`，回车确认即可。  
被封禁的指令只有 OP 或拥有 `servercommandmonitor.block.bypass` 权限的玩家可使用。

### 🔟 自动更新检测
插件启动时会自动检查 [Releases](https://github.com/你的用户名/ServerCommandMonitor/releases)，若有新版本会向管理员发送可点击下载链接。  
也可随时使用 `/scm update` 手动检查。

---

## 🔑 权限

| 节点 | 默认 | 说明 |
|------|------|------|
| `servercommandmonitor.see` | OP | 接收监控消息 (聊天,BossBar,ActionBar) |
| `servercommandmonitor.sudo` | OP | 使用 /scm sudo |
| `servercommandmonitor.reload` | OP | 重载配置 |
| `servercommandmonitor.lookup` | OP | 查询玩家命令历史 |
| `servercommandmonitor.monitor` | OP | 管理监视列表 |
| `servercommandmonitor.ai` | OP | 使用 AI 分析 |
| `servercommandmonitor.block.bypass` | OP | 绕过指令封禁 |

---

## 🕹️ 命令

| 指令 | 说明 |
|------|------|
| `/scm help` | 查看帮助 |
| `/scm reload` | 重载配置 |
| `/scm sudo <玩家> <命令>` | 代理执行命令 |
| `/scm monitor [玩家列表 / all]` | 设置或查看监视列表 |
| `/scm lookup <玩家> [页码]` | 查询玩家命令历史（分页） |
| `/scm ai <玩家> <命令>` | AI 分析命令 |
| `/scm blockcmd <指令>` | 封禁指令 |
| `/scm update` | 手动检查更新 |

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
<summary>BossBar / ActionBar 不显示？</summary>

确保配置中对应 `enabled: true`，且你拥有 `servercommandmonitor.see` 权限。
</details>

<details>
<summary>某些命令没被监控？</summary>

检查黑名单正则是否误匹配，以及监视列表设置。插件只监控玩家聊天框输入的命令，不捕获控制台或 API 调用。
</details>

<details>
<summary>如何关闭日志？</summary>

`logging.enabled: false`
</details>

<details>
<summary>日志文件太多怎么办？</summary>

设置 `log-retention-days` 为期望保留的天数，过期玩家日志会自动清理。全局日志 `commands.log` 请手动管理。
</details>

<details>
<summary>AI 分析没有反应？</summary>

确认 `ai.enabled: true`，已填写正确的 `api-key` 和 `api-url`，服务器能正常访问外网。
</details>

<details>
<summary>更新检测提示失败？</summary>

服务器可能无法访问 GitHub API，可设置 `update-checker.enabled: false` 关闭。
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
