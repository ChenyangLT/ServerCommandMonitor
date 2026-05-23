package com.example.servercommandmonitor;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
import java.lang.reflect.Method;
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

    private boolean useMiniMessage = false;
    private Object miniMessageInstance;
    private String monitorFormat;          // 传统格式
    private String minimessageFormat;      // MiniMessage 格式

    private boolean blacklistEnabled;
    private List<Pattern> blacklistPatterns;

    private boolean sudoEnabled;
    private boolean sudoNotifyTarget;
    private boolean monitorSudo;

    // BossBar
    private boolean bossBarEnabled;
    private BarColor bossBarColor;
    private BarStyle bossBarStyle;
    private int bossBarDuration;
    private String bossBarMessage;
    private final Map<Player, BossBar> activeBossBars = new HashMap<>();

    // ActionBar
    private boolean actionBarEnabled;
    private String actionBarMessage;

    private boolean papiEnabled = false;
    private boolean debug = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiEnabled = true;
            getLogger().info("检测到 PlaceholderAPI，将支持 PAPI 变量。");
        }

        // 检测 MiniMessage (Paper)
        try {
            Class<?> miniMsgClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            Method miniMethod = miniMsgClass.getMethod("miniMessage");
            miniMessageInstance = miniMethod.invoke(null);
            useMiniMessage = getConfig().getString("monitor.mode", "CHATCOLOR").equalsIgnoreCase("MINIMESSAGE");
            if (useMiniMessage) {
                getLogger().info("MiniMessage 模式已启用。");
            }
        } catch (Exception e) {
            useMiniMessage = false;
            miniMessageInstance = null;
            if (getConfig().getString("monitor.mode", "").equalsIgnoreCase("MINIMESSAGE")) {
                getLogger().warning("MiniMessage 不可用，已自动切换为传统颜色格式。");
            }
        }

        debug = getConfig().getBoolean("debug", false);
        if (debug) {
            getLogger().info("调试模式已开启，所有命令转发将在控制台输出。");
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("servercommandmonitor") != null) {
            getCommand("servercommandmonitor").setExecutor(this);
        }
        getLogger().info("ServerCommandMonitor v2.2.0 已启动 (1.17 - 1.21)");
    }

    @Override
    public void onDisable() {
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
        if (logEnabled && !logFile.getParentFile().exists()) {
            logFile.getParentFile().mkdirs();
        }

        // 监控消息模板
        monitorFormat = getConfig().getString("monitor.format", "&7[&cCMD&7] &f%player%&7: &b%command%");
        minimessageFormat = getConfig().getString("monitor.minimessage-format",
                "<gray>[<red>CMD</red>]</gray> <white>%player%</white>: <aqua>%command%</aqua>");

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
        String colorStr = getConfig().getString("bossbar.color", "RED").toUpperCase();
        try {
            bossBarColor = BarColor.valueOf(colorStr);
        } catch (IllegalArgumentException e) {
            bossBarColor = BarColor.RED;
        }
        String styleStr = getConfig().getString("bossbar.style", "SOLID").toUpperCase();
        try {
            bossBarStyle = BarStyle.valueOf(styleStr);
        } catch (IllegalArgumentException e) {
            bossBarStyle = BarStyle.SOLID;
        }
        bossBarDuration = Math.max(1, getConfig().getInt("bossbar.duration", 5));
        bossBarMessage = getConfig().getString("bossbar.message", "&c%player% &7执行了: &f%command%");

        // ActionBar
        actionBarEnabled = getConfig().getBoolean("actionbar.enabled", false);
        actionBarMessage = getConfig().getString("actionbar.message", "&c%player% &7执行了 &f%command%");

        debug = getConfig().getBoolean("debug", false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.GREEN + "===== ServerCommandMonitor 帮助 =====");
            sender.sendMessage(ChatColor.YELLOW + "/scm help" + ChatColor.WHITE + " - 显示此帮助");
            sender.sendMessage(ChatColor.YELLOW + "/scm reload" + ChatColor.WHITE + " - 重载配置文件");
            sender.sendMessage(ChatColor.YELLOW + "/scm sudo <玩家> <命令>" + ChatColor.WHITE + " - 以某玩家身份执行命令");
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

        sender.sendMessage(ChatColor.RED + "未知子命令。请使用 /scm help 查看帮助。");
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
                    if (debug) {
                        getLogger().info("[DEBUG] 命令 " + fullCommand + " 被黑名单拦截。");
                    }
                    return;
                }
            }
        }

        String nowStr = LocalDateTime.now().format(dateFormatter);
        if (debug) {
            getLogger().info("[DEBUG] 玩家 " + player.getName() + " 执行命令: " + fullCommand);
        }

        // 构建聊天监控消息
        String chatMsg;
        boolean useMiniNow = useMiniMessage && miniMessageInstance != null;
        if (useMiniNow) {
            chatMsg = replacePlaceholders(minimessageFormat, player, fullCommand, nowStr);
        } else {
            chatMsg = replacePlaceholders(monitorFormat, player, fullCommand, nowStr);
            chatMsg = ChatColor.translateAlternateColorCodes('&', chatMsg);
        }

        Object adventureComponent = null;
        if (useMiniNow) {
            try {
                Method deserialize = miniMessageInstance.getClass().getMethod("deserialize", String.class);
                adventureComponent = deserialize.invoke(miniMessageInstance, chatMsg);
            } catch (Exception e) {
                getLogger().warning("MiniMessage 解析失败，回退传统格式。错误: " + e.getMessage());
                chatMsg = replacePlaceholders(monitorFormat, player, fullCommand, nowStr);
                chatMsg = ChatColor.translateAlternateColorCodes('&', chatMsg);
                useMiniNow = false;
            }
        }

        int recipients = 0;
        for (Player admin : Bukkit.getOnlinePlayers()) {
            // ★ 修复点：允许 OP 或拥有权限的玩家接收消息
            if (admin.isOp() || admin.hasPermission("servercommandmonitor.see")) {
                recipients++;
                if (useMiniNow && adventureComponent != null) {
                    try {
                        Class<?> advComponentClass = Class.forName("net.kyori.adventure.text.Component");
                        Method sendMsg = admin.getClass().getMethod("sendMessage", advComponentClass);
                        sendMsg.invoke(admin, adventureComponent);
                    } catch (Exception ex) {
                        admin.sendMessage(chatMsg);
                    }
                } else {
                    admin.sendMessage(chatMsg);
                }
            }
        }

        if (debug) {
            getLogger().info("[DEBUG] 监控消息已发送给 " + recipients + " 位管理员。");
        }

        // BossBar（同样加入 isOp() 判断）
        if (bossBarEnabled) {
            String bossMsg = replacePlaceholders(bossBarMessage, player, fullCommand, nowStr);
            bossMsg = ChatColor.translateAlternateColorCodes('&', bossMsg);
            showBossBarToAdmins(bossMsg);
        }

        // ActionBar（同样加入 isOp() 判断）
        if (actionBarEnabled) {
            String actMsg = replacePlaceholders(actionBarMessage, player, fullCommand, nowStr);
            actMsg = ChatColor.translateAlternateColorCodes('&', actMsg);
            sendActionBarToAdmins(actMsg);
        }

        // 日志
        if (logEnabled) {
            String logEntry = replacePlaceholders(logFormat, player, fullCommand, nowStr);
            logEntry = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', logEntry));
            writeLog(logEntry);
        }
    }

    private String replacePlaceholders(String text, Player player, String command, String dateString) {
        if (player == null) return text;
        String result = text;
        result = result.replace("%player%", player.getName());
        result = result.replace("%displayname%", player.getDisplayName());
        result = result.replace("%command%", command);
        result = result.replace("%world%", player.getWorld().getName());
        result = result.replace("%date%", dateString);
        if (papiEnabled) {
            result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
        }
        return result;
    }

    private void showBossBarToAdmins(String message) {
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.isOp() || admin.hasPermission("servercommandmonitor.see")) {
                BossBar oldBar = activeBossBars.remove(admin);
                if (oldBar != null) {
                    oldBar.removeAll();
                }
                BossBar bar = Bukkit.createBossBar(message, bossBarColor, bossBarStyle);
                bar.setProgress(1.0);
                bar.addPlayer(admin);
                activeBossBars.put(admin, bar);
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    bar.removeAll();
                    activeBossBars.remove(admin, bar);
                }, bossBarDuration * 20L);
            }
        }
    }

    private void sendActionBarToAdmins(String message) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(message));
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.isOp() || admin.hasPermission("servercommandmonitor.see")) {
                admin.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
            }
        }
    }

    private void writeLog(String line) {
        try {
            if (!logFile.getParentFile().exists()) {
                logFile.getParentFile().mkdirs();
            }
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