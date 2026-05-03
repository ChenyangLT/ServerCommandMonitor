🛡️ ServerCommandMonitor
全功能 Minecraft 命令审计 & 管理插件
适用于 Paper 1.21.x（1.21.4 及未来小版本）
实时监控玩家指令 · BossBar 提示 · ActionBar 提示 · 日志记录 · sudo 代理 · 黑名单 · PAPI 支持

https://img.shields.io/badge/license-MIT-blue.svg
https://img.shields.io/badge/Minecraft-1.21.x-brightgreen

🧠 开发与致谢
本插件由 DeepseekV4 （深度求索新一代大语言模型）核心代码生成，
经 Chen_yang_ 在实际 Paper 1.21.8 环境中全面调试、功能补全、配置测试与优化后发布。

✨ 代码骨架与设计思路源于 DeepseekV4 的智能编程能力，人工打磨确保生产环境稳定可靠。

📖 简介
ServerCommandMonitor 是一款面向服主与管理员的命令审计工具。
当你需要了解玩家在服务器上输入了什么指令、防止滥用、或代替玩家执行特定操作时，本插件提供了一站式解决方案。

核心能力一览
功能	描述
🔍 实时监控	所有玩家输入的命令（以 / 开头）立刻转发给拥有权限的管理员
🖥️ BossBar 提示	管理员顶部血条显示命令信息，颜色/样式/时长可配置
💬 ActionBar 提示	物品栏上方轻量提示，不遮挡视野
📝 日志记录	支持将所有命令写入文件，带时间戳，可开关
👤 sudo 代理	管理员可强制任意在线玩家执行命令 (/scm sudo)
🛡️ 智能黑名单	正则表达式过滤敏感指令（如 /login, /l），保护隐私
🧩 PlaceholderAPI	全面支持 PAPI 变量，消息格式无限扩展
🎨 双消息引擎	传统 & 颜色代码 + Paper 原生 MiniMessage 任选
⚡ 热重载	修改配置后 /scm reload 即时生效，无需重启
📦 安装
环境要求

Paper 1.21.x 服务端（或兼容 Paper API 的 Purpur 等）

Java 21

（可选）PlaceholderAPI – 用于拓展变量

获取插件
从 Releases 下载最新版 ServerCommandMonitor-2.1.0.jar。

部署
将 jar 放入服务器的 plugins/ 文件夹，重启服务器。
首次启动会自动生成 plugins/ServerCommandMonitor/config.yml。

⚠️ 注意：如果之前残留了空的 config.yml，请先删除 plugins/ServerCommandMonitor 文件夹再重启，否则不会生成默认配置。

⚙️ 快速使用指南
1️⃣ 基础监控
安装并重启后，无需任何额外设置。
默认情况下，所有 OP 都会在聊天框看到玩家命令：

text
[CMD] Steve: /gamemode creative
关闭黑名单（让所有命令都公开）只需在 config.yml 中设置：

yaml
blacklist:
  enabled: false
2️⃣ 自定义监控消息
编辑 plugins/ServerCommandMonitor/config.yml 中的 monitor 部分：

yaml
monitor:
  mode: CHATCOLOR   # 或 MINIMESSAGE 以使用 Paper 高级样式
  format: "&7[&cCMD&7] &f%player% &7→ &b%command%"
  minimessage-format: "<gray>[<red>CMD</red>]</gray> <white>%player%</white>: <aqua>%command%</aqua>"
可用变量：%player%, %displayname%, %command%, %world% 及所有 PAPI 变量。

重载生效：/scm reload

3️⃣ 开启 BossBar / ActionBar
在配置文件中找到对应节点并改为 true：

yaml
bossbar:
  enabled: true
  color: "GREEN"
  duration: 5
  message: "&e玩家 %player% 执行了命令"

actionbar:
  enabled: true
  message: "&e>> %player% 使用了 %command%"
4️⃣ 使用黑名单保护敏感命令
默认已屏蔽常见登录指令。你也可以用正则表达式自定义：

yaml
blacklist:
  enabled: true
  patterns:
    - "/login.*"
    - "/register.*"
    - "/l( .*)?"
    # 添加屏蔽私聊
    - "/msg.*"
    - "/tell.*"
匹配规则是整个指令字符串（包含开头的 /），不区分大小写。

5️⃣ 管理员 sudo 代理
让玩家 Steve 执行任何命令（例如给予创造模式）：

text
/scm sudo Steve gamemode creative
想要通知目标玩家？设置 sudo.notify-target: true。

6️⃣ 日志审计
默认会在 plugins/ServerCommandMonitor/logs/commands.log 记录每一条未被黑名单过滤的命令。

日志格式示例：

text
[2025-07-21 14:35:12] Steve 执行: /gamemode creative
🔑 权限
权限节点	默认	描述
servercommandmonitor.see	OP	接收所有监控信息（聊天/BossBar/ActionBar）
servercommandmonitor.sudo	OP	允许使用 /scm sudo
servercommandmonitor.reload	OP	允许使用 /scm reload
你可以通过 LuckPerms 等插件将上述权限授予非 OP 管理员。

🕹️ 命令
命令	说明
/scm reload	重载配置文件
/scm sudo <玩家> <命令...>	以某玩家身份执行命令
/servercommandmonitor 或 /monitorme	查看帮助
🧩 PlaceholderAPI 集成
如果你安装了 PlaceholderAPI，可以在任何消息格式中使用 PAPI 变量，例如：

yaml
monitor:
  format: "&7[&cCMD&7] &f%player% &7(HP: &c%player_health%&7): &b%command%"
效果：[CMD] Steve (HP: 20): /gamemode creative

🎨 高级样式：MiniMessage
如果使用 Paper 1.16.5+，开启 MiniMessage 模式可实现鼠标悬浮、点击复制等交互：

yaml
monitor:
  mode: MINIMESSAGE
  minimessage-format: "<gray>[<red>CMD</red>]</gray> <white><hover:show_text:'<green>点击复制</green>'><click:copy_to_clipboard:'%command%'>%player%</click></hover></white>: <aqua>%command%</aqua>"
❓ 常见问题
Q：config.yml 是空的怎么办？
A：删除 plugins/ServerCommandMonitor 文件夹，再重启服务器即可生成完整默认配置。

Q：BossBar 不显示？
A：请确保 bossbar.enabled: true，且你拥有 servercommandmonitor.see 权限。

Q：某些命令没有被监控？
A：检查黑名单是否误伤了该命令。另外，插件只能监控玩家在聊天框输入的指令，控制台或其他插件直接执行的命令无法捕获。

Q：如何关闭日志？
A：设置 logging.enabled: false。

🔄 升级说明
从旧版升级时，建议先备份原 config.yml，然后删掉配置文件目录，重启重新生成包含最新选项的配置，再对照备份恢复自定义部分。

📜 开源协议
本项目采用 MIT 许可证，你可以自由使用、修改、分发本插件，甚至用于商业项目。只需保留原始版权声明。
详见 LICENSE 文件。

🤝 贡献
由 AI 生成+人工调校，仍可能有未发现的问题。欢迎提交 Issue 或 Pull Request。

💡 致谢

深度求索 DeepseekV4 提供代码生成

调试者：Chen_yang_（实际环境测试与修复）

让每一条命令，都暴露在阳光之下。 ☀️
