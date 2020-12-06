package com.entiv.sakuramove;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class PlayerListener implements Listener {

    private static List<UUID> coolDownPlayers;
    private final FileConfiguration config = Main.getInstance().getConfig();

    PlayerListener() {
        int coolDown = config.getInt("CoolDown");
        if (coolDown != 0) {
            coolDownPlayers = new ArrayList<java.util.UUID>();
        }
    }

    @EventHandler
    public void onPlayerToggleSprintEvent(PlayerToggleSprintEvent event) {

        Player player = event.getPlayer();

        if (!player.hasPermission("SakuraMove.Sprinting")) return;

        int coolDown = config.getInt("CoolDown");

        if (coolDown <= 0) {
            playerSprinting(player);
            return;
        }

        if (player.isSprinting() && !coolDownPlayers.contains(player.getUniqueId())) {
            playerSprinting(player);
            setCoolDown(player);
        } else {
            String message = config.getString("Message.CoolDown");
            Message.sendActionBar(player, message);
        }
    }

    private void playerSprinting(Player player) {
        Location location = player.getLocation();
        double power = config.getDouble("Power");
        player.setVelocity(location.getDirection().setY(0).multiply(power));
    }

    private void setCoolDown(Player player) {
        long coolDown = config.getLong("CoolDown") * 20;
        coolDownPlayers.add(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                coolDownPlayers.remove(player.getUniqueId());
            }
        }.runTaskLaterAsynchronously(Main.getInstance(), coolDown);

    }
}