package com.entiv.sakuramove;

import com.entiv.sakuramove.action.DoubleJump;
import com.entiv.sakuramove.action.Sprint;
import com.entiv.sakuramove.listener.PaperJumpListener;
import com.entiv.sakuramove.listener.SpigotJumpListener;
import com.entiv.sakuramove.listener.SprintListener;
import com.entiv.sakuramove.listener.StaminaChangeListener;
import com.entiv.sakuramove.manager.StaminaManager;
import com.entiv.sakuramove.task.DynamicProgressTask;
import com.entiv.sakuramove.task.RecoveryStaminaTask;
import com.entiv.sakuramove.task.TaskManager;
import com.entiv.sakuramove.utils.Message;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;

//TODO 增加后跳功能
public class Main extends JavaPlugin {

    private static Main plugin;

    @Getter
    private StaminaManager staminaManager;

    @Getter
    private TaskManager taskManager;
    private Listener doubleJump;

    @Override
    public void onEnable() {

        plugin = this;
        staminaManager = new StaminaManager();
        taskManager = new TaskManager();
        taskManager.start(this);

        String[] message = {
                "§a樱花移动插件§e v" + getDescription().getVersion() + " §a已启用",
                "§a插件制作作者:§e EnTIv §aQQ群:§e 600731934"
        };
        getServer().getConsoleSender().sendMessage(message);

        if (isPaperFork()) {
            doubleJump = new PaperJumpListener();
        } else {
            doubleJump = new SpigotJumpListener();
        }

        reload();
        saveDefaultConfig();

        if (getConfig().getBoolean("移动行为.冲刺.开启")) {
            Bukkit.getPluginManager().registerEvents(new SprintListener(), this);
        }

        addTask();
        registerPlaceholder();

        Bukkit.getPluginManager().registerEvents(new StaminaChangeListener(), this);
    }

    @Override
    public void onDisable() {
        String[] message = {
                "§a樱花移动插件§e v" + getDescription().getVersion() + " §a已卸载",
                "§a插件制作作者:§e EnTIv §aQQ群:§e 600731934"
        };
        getServer().getConsoleSender().sendMessage(message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender.isOp() && args[0].equalsIgnoreCase("reload")) {
            reload();
            Message.send(sender, plugin.getConfig().getString("Message.Reload"));
            return true;
        }

        Player player = sender instanceof Player ? ((Player) sender) : null;
        if (player == null) return true;

        if (args[0].equalsIgnoreCase("sprint") && getConfig().getBoolean("移动行为.冲刺.开启")) {
            Sprint sprint = Sprint.getInstance();
            if (sprint.canAccept(player)) {
                sprint.accept(player);
                sprint.setCoolDown(player);
            }
        } else if (args[0].equalsIgnoreCase("doublejump") && getConfig().getBoolean("移动行为.二段跳.开启")){

            DoubleJump doubleJump = DoubleJump.getInstance();

            if (doubleJump.canAccept(player) && player.isOnGround()) {
                doubleJump.accept(player);
            }
        } else if (args[0].equalsIgnoreCase("toggle")) {

            if (SprintListener.disablePlayers.contains(player.getUniqueId())) {
                SprintListener.disablePlayers.remove(player.getUniqueId());
                Message.send(player, plugin.getConfig().getString("Message.Toggle"));
            } else {
                SprintListener.disablePlayers.add(player.getUniqueId());
            }

        }
        return true;
    }

    public void reload() {

        reloadConfig();

        boolean enableFallDamage = getConfig().getBoolean("移动行为.二段跳.摔落伤害");

        HandlerList.unregisterAll(doubleJump);

        if (!getConfig().getBoolean("移动行为.二段跳.开启")) return;

        if (enableFallDamage) {
            Bukkit.getPluginManager().registerEvents(doubleJump, this);
        }

    }

    private void registerPlaceholder() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            SakuraMovePlaceholder placeholder = new SakuraMovePlaceholder();
            placeholder.register();
        }
    }

    private void addTask() {
        RecoveryStaminaTask recoveryStaminaTask = new RecoveryStaminaTask(getConfig().getInt("体力恢复设置.恢复速度", 1));
        DynamicProgressTask dynamicProgressTask = new DynamicProgressTask(1);

        taskManager.addTask(recoveryStaminaTask);
        taskManager.addTask(dynamicProgressTask);
    }

    public static Main getInstance() {
        return plugin;
    }

    private static boolean isPaperFork() {
        try {
            Class.forName("com.destroystokyo.paper.event.player.PlayerJumpEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
