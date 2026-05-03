package com.example.servercommandmonitor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ServerCommandMonitor extends JavaPlugin implements Listener, CommandExecutor {

    private boolean logEnabled;
    private File logFile;
    private DateTimeFormatter dateFormatter;
    private String logFormat;

    private String monitorFormat;
    private String minimessageFormat;
    private boolean useMiniMessage;
    private MiniMessage miniMessage = MiniMessage.miniMessage();

    private boolean blacklistEnabled;
    private List<Pattern> blacklistPatterns;

    private boolean sudoEnabled;
    private boolean sudoNotifyTarget;
    private boolean monitorSudo;

    // BossBar 相关
    private boolean bossBarEnabled;
    private BarColor bossBarColor;
    private BarStyle bossBarStyle;
    private int bossBarDuration;
    private String bossBarMessage;

    // ActionBar 相关
    private boolean actionBarEnabled;
    private String actionBarMessage;

    private boolean papiEnabled = false;

    // 为每个管理员存储当前显示的 BossBar，避免堆积
    private final Map<Player, BossBar> activeBossBars = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiEnabled = true;
            getLogger().info("检测到 PlaceholderAPI，将支持 PAPI 变量。");
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("servercommandmonitor") != null) {
            getCommand("servercommandmonitor").setExecutor(this);
        }
        getLogger().info("ServerCommandMonitor v2.1 已启动！");
    }

    @Override
    public void onDisable() {
        // 移除所有 BossBar
        for (BossBar bar : activeBossBars.values()) {
            bar.removeAll();
        }
        activeBossBars.clear();
        getLogger().info("ServerCommandMonitor 已卸载。");
    }

    private void loadConfig() {
        reloadConfig();

        // 日志
        logEnabled = getConfig().getBoolean("logging.enabled", true);
        logFile = new File(getDataFolder(), getConfig().getString("logging.file", "logs/commands.log"));
        logFormat = getConfig().getString("logging.format", "[%date%] %player% 执行: %command%");
        String dateFmt = getConfig().getString("logging.date-format", "yyyy-MM-dd HH:mm:ss");
        try {
            dateFormatter = DateTimeFormatter.ofPattern(dateFmt);
        } catch (Exception e) {
            dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            getLogger().warning("日志日期格式无效，已使用默认格式。");
        }
        if (logEnabled) {
            File parent = logFile.getParentFile();
            if (!parent.exists()) parent.mkdirs();
        }

        // 监控消息
        String modeStr = getConfig().getString("monitor.mode", "CHATCOLOR").toUpperCase();
        useMiniMessage = modeStr.equals("MINIMESSAGE");
        monitorFormat = getConfig().getString("monitor.format", "&7[&cCMD&7] &f%player%&7: &b%command%");
        minimessageFormat = getConfig().getString("monitor.minimessage-format", "<gray>[<red>CMD</red>]</gray> <white>%player%</white>: <aqua>%command%</aqua>");

        // 黑名单
        blacklistEnabled = getConfig().getBoolean("blacklist.enabled", true);
        blacklistPatterns = getConfig().getStringList("blacklist.patterns").stream()
                .map(s -> Pattern.compile(s, Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toList());

        // sudo
        sudoEnabled = getConfig().getBoolean("sudo.enabled", true);
        sudoNotifyTarget = getConfig().getBoolean("sudo.notify-target", false);
        monitorSudo = getConfig().getBoolean("sudo.monitor-sudo-commands", false);

        // BossBar
        bossBarEnabled = getConfig().getBoolean("bossbar.enabled", false);
        String barColorStr = getConfig().getString("bossbar.color", "RED").toUpperCase();
        try {
            bossBarColor = BarColor.valueOf(barColorStr);
        } catch (IllegalArgumentException e) {
            bossBarColor = BarColor.RED;
            getLogger().warning("BossBar 颜色无效，使用默认 RED。");
        }
        String barStyleStr = getConfig().getString("bossbar.style", "SOLID").toUpperCase();
        try {
            bossBarStyle = BarStyle.valueOf(barStyleStr);
        } catch (IllegalArgumentException e) {
            bossBarStyle = BarStyle.SOLID;
            getLogger().warning("BossBar 样式无效，使用默认 SOLID。");
        }
        bossBarDuration = getConfig().getInt("bossbar.duration", 5);
        bossBarMessage = getConfig().getString("bossbar.message", "&c%player% &7执行了: &f%command%");

        // ActionBar
        actionBarEnabled = getConfig().getBoolean("actionbar.enabled", false);
        actionBarMessage = getConfig().getString("actionbar.message", "&c%player% &7执行了 &f%command%");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "用法: /scm reload | /scm sudo <玩家> <命令...>");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("servercommandmonitor.reload")) {
                sender.sendMessage(ChatColor.RED + "你没有权限重载配置。");
                return true;
            }
            loadConfig();
            sender.sendMessage(ChatColor.GREEN + "ServerCommandMonitor 配置已重载。");
            return true;
        }
        if (args[0].equalsIgnoreCase("sudo")) {
            if (!sudoEnabled) {
                sender.sendMessage(ChatColor.RED + "sudo 功能已在配置中禁用。");
                return true;
            }
            if (!sender.hasPermission("servercommandmonitor.sudo")) {
                sender.sendMessage(ChatColor.RED + "你没有权限使用 sudo。");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "用法: /scm sudo <玩家> <命令...>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "玩家 " + args[1] + " 不在线或不存在。");
                return true;
            }
            StringBuilder commandBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                commandBuilder.append(args[i]);
                if (i < args.length - 1) commandBuilder.append(" ");
            }
            String commandToRun = commandBuilder.toString();
            if (!monitorSudo) {
                target.setMetadata("scm_sudo", new FixedMetadataValue(this, true));
            }
            Bukkit.dispatchCommand(target, commandToRun);
            sender.sendMessage(ChatColor.GREEN + "已让 " + target.getName() + " 执行: /" + commandToRun);
            if (sudoNotifyTarget) {
                target.sendMessage(ChatColor.YELLOW + "管理员强制你执行了命令: /" + commandToRun);
            }
            return true;
        }
        sender.sendMessage(ChatColor.RED + "未知子命令。可用: reload, sudo");
        return true;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String fullCommand = event.getMessage();

        if (!monitorSudo && player.hasMetadata("scm_sudo")) {
            player.removeMetadata("scm_sudo", this);
            return;
        }

        if (blacklistEnabled) {
            for (Pattern pattern : blacklistPatterns) {
                if (pattern.matcher(fullCommand).matches()) {
                    return;
                }
            }
        }

        String nowStr = LocalDateTime.now().format(dateFormatter);

        // 1. 聊天监控消息
        String chatMsg = buildMonitorMessage(player, fullCommand, nowStr);
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("servercommandmonitor.see")) {
                if (useMiniMessage) {
                    admin.sendMessage(miniMessage.deserialize(chatMsg));
                } else {
                    admin.sendMessage(chatMsg);
                }
            }
        }

        // 2. BossBar 显示（仅向有监控权限的管理员）
        if (bossBarEnabled) {
            String bossMsg = replacePlaceholders(bossBarMessage, player, fullCommand, nowStr);
            // BossBar 文本不支持 MiniMessage，统一用 & 颜色代码转换
            bossMsg = ChatColor.translateAlternateColorCodes('&', bossMsg);
            showBossBarToAdmins(bossMsg);
        }

        // 3. ActionBar 显示
        if (actionBarEnabled) {
            String actMsg = replacePlaceholders(actionBarMessage, player, fullCommand, nowStr);
            sendActionBarToAdmins(actMsg);
        }

        // 4. 日志记录
        if (logEnabled) {
            String logEntry = buildLogEntry(player, fullCommand, nowStr);
            writeLog(logEntry);
        }
    }

    private String buildMonitorMessage(Player player, String command, String dateString) {
        String template = useMiniMessage ? minimessageFormat : monitorFormat;
        String msg = replacePlaceholders(template, player, command, dateString);
        if (!useMiniMessage) {
            msg = ChatColor.translateAlternateColorCodes('&', msg);
        }
        return msg;
    }

    private String buildLogEntry(Player player, String command, String dateString) {
        String entry = replacePlaceholders(logFormat, player, command, dateString);
        entry = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', entry));
        return entry;
    }

    private String replacePlaceholders(String text, Player player, String command, String dateString) {
        if (player == null) return text;
        String result = text
                .replace("%player%", player.getName())
                .replace("%displayname%", player.getDisplayName())
                .replace("%command%", command)
                .replace("%world%", player.getWorld().getName())
                .replace("%date%", dateString);
        if (papiEnabled) {
            result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
        }
        return result;
    }

    // ---- BossBar 处理 ----
    private void showBossBarToAdmins(String message) {
        // 移除所有管理员的旧 BossBar
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("servercommandmonitor.see")) {
                BossBar oldBar = activeBossBars.remove(admin);
                if (oldBar != null) {
                    oldBar.removeAll();
                }
                BossBar bar = Bukkit.createBossBar(message, bossBarColor, bossBarStyle);
                bar.setProgress(1.0);
                bar.addPlayer(admin);
                activeBossBars.put(admin, bar);
                // 设置定时器移除
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    bar.removeAll();
                    activeBossBars.remove(admin, bar); // 仅当还是同一个 bar 时移除
                }, bossBarDuration * 20L);
            }
        }
    }

    // ---- ActionBar 处理 ----
    private void sendActionBarToAdmins(String rawMessage) {
        // 根据消息模式生成 Component
        Component message;
        if (useMiniMessage) {
            // 注意：actionbar.message 在配置中用的是 & 颜色码，MiniMessage 模式下我们需要处理。
            // 简单做法：如果全局模式是 MINIMESSAGE，但 actionbar 仍然可能使用传统颜色码，
            // 我们统一先转换 & 码，再用 LegacyComponentSerializer 解析，以保证兼容性。
            // 然后你可以手动使用 MiniMessage 格式在配置中写 <...> 标签，本文不做强求。
            String legacy = ChatColor.translateAlternateColorCodes('&', rawMessage);
            message = LegacyComponentSerializer.legacySection().deserialize(legacy);
        } else {
            String legacy = ChatColor.translateAlternateColorCodes('&', rawMessage);
            message = LegacyComponentSerializer.legacySection().deserialize(legacy);
        }
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("servercommandmonitor.see")) {
                admin.sendActionBar(message);
            }
        }
    }

    private void writeLog(String line) {
        try {
            File parent = logFile.getParentFile();
            if (!parent.exists()) parent.mkdirs();
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(logFile, true), StandardCharsets.UTF_8))) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            getLogger().warning("无法写入命令日志: " + e.getMessage());
        }
    }
}