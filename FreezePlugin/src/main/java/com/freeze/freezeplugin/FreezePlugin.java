package com.freeze.freezeplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class FreezePlugin extends JavaPlugin implements CommandExecutor, TabCompleter, Listener {

    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final Map<UUID, Location> frozenLocations = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("fz").setExecutor(this);
        getCommand("fz").setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("FreezePlugin enabled!");
    }

    @Override
    public void onDisable() {
        for (UUID uuid : frozenPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                unfreezePlayer(player);
            }
        }
        getLogger().info("FreezePlugin disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§c⛔ Использование: /fz <ник>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§c✖ Игрок не найден!");
            return true;
        }

        String adminName = sender.getName();

        if (frozenPlayers.contains(target.getUniqueId())) {
            unfreezePlayer(target);
            sender.sendMessage("§a❄ Игрок §e" + target.getName() + " §aразморожен.");
            target.sendMessage("§a✅ Вас разморозил админ: §e" + adminName + "§a.");
        } else {
            freezePlayer(target);
            sender.sendMessage("§a❄ Игрок §e" + target.getName() + " §aзаморожен.");
            target.sendMessage("§c⛔ Вас заморозил админ: §e" + adminName + "§c.");
        }

        return true;
    }

    private void freezePlayer(Player player) {
        frozenPlayers.add(player.getUniqueId());
        frozenLocations.put(player.getUniqueId(), player.getLocation().clone());

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, false, false, false));

        player.setVelocity(player.getVelocity().setX(0).setY(0).setZ(0));
    }

    private void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId());
        frozenLocations.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.SLOWNESS);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!frozenPlayers.contains(uuid)) {
            return;
        }

        Location frozenLocation = frozenLocations.get(uuid);
        if (frozenLocation == null) {
            frozenLocation = player.getLocation().clone();
            frozenLocations.put(uuid, frozenLocation);
        }

        Location to = event.getTo();
        if (to == null) {
            return;
        }

        boolean moved = to.getX() != frozenLocation.getX()
                || to.getY() != frozenLocation.getY()
                || to.getZ() != frozenLocation.getZ()
                || to.getYaw() != frozenLocation.getYaw()
                || to.getPitch() != frozenLocation.getPitch()
                || !to.getWorld().equals(frozenLocation.getWorld());

        if (moved) {
            event.setTo(frozenLocation.clone());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (frozenPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        frozenPlayers.remove(uuid);
        frozenLocations.remove(uuid);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        }
        return Collections.emptyList();
    }
}
