package com.freeze.freezeplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class FreezePlugin extends JavaPlugin implements CommandExecutor, TabCompleter, Listener {

    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final Map<UUID, Location> frozenLocations = new HashMap<>();
    private final Map<UUID, List<Block>> iceCages = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        createDataFile();
        loadFrozenPlayers();

        getCommand("fz").setExecutor(this);
        getCommand("fz").setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);

        getServer().getConsoleSender().sendMessage(
                ChatColor.AQUA + "❄❄❄ " + ChatColor.WHITE + "FreezePlugin" + ChatColor.AQUA + " ❄❄❄"
        );
        getServer().getConsoleSender().sendMessage(
                ChatColor.GRAY + "Loaded" + ChatColor.DARK_GRAY + " | " + ChatColor.GREEN + "Ready to freeze."
        );
    }

    @Override
    public void onDisable() {
        for (UUID uuid : iceCages.keySet()) {
            removeIceCage(uuid);
        }
        saveFrozenPlayers();
        getLogger().info("FreezePlugin disabled!");
    }

    private void createDataFile() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void loadFrozenPlayers() {
        List<String> uuids = dataConfig.getStringList("frozen-players");
        for (String s : uuids) {
            frozenPlayers.add(UUID.fromString(s));
        }
    }

    private void saveFrozenPlayers() {
        List<String> uuids = new ArrayList<>();
        for (UUID uuid : frozenPlayers) {
            uuids.add(uuid.toString());
        }
        dataConfig.set("frozen-players", uuids);
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("freezeplugin.use")) {
            sender.sendMessage(format(getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(format(getConfig().getString("messages.usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(format(getConfig().getString("messages.player-not-found")));
            return true;
        }

        String adminName = sender.getName();
        String prefix = getConfig().getString("messages.prefix");

        if (frozenPlayers.contains(target.getUniqueId())) {
            unfreezePlayer(target);
            sender.sendMessage(format(prefix + getConfig().getString("messages.unfreeze.admin-notify").replace("%player%", target.getName())));
            target.sendMessage(format(prefix + getConfig().getString("messages.unfreeze.target-notify")));
        } else {
            freezePlayer(target);
            sender.sendMessage(format(prefix + getConfig().getString("messages.freeze.admin-notify").replace("%player%", target.getName())));
            for (String msg : getConfig().getStringList("messages.freeze.target-notify")) {
                target.sendMessage(format(msg.replace("%admin%", adminName)));
            }
        }

        return true;
    }

    private void freezePlayer(Player player) {
        frozenPlayers.add(player.getUniqueId());
        frozenLocations.put(player.getUniqueId(), player.getLocation().clone());

        int slownessLevel = getConfig().getInt("effects.slowness-level", 255);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, slownessLevel, false, false, false));
        
        if (getConfig().getBoolean("effects.blindness", true)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false, false));
        }

        if (getConfig().getBoolean("effects.ice-cage.enabled", true)) {
            createIceCage(player);
        }

        if (getConfig().getBoolean("effects.titles.enabled", true)) {
            String title = format(getConfig().getString("effects.titles.title"));
            String subtitle = format(getConfig().getString("effects.titles.subtitle"));
            int fadeIn = getConfig().getInt("effects.titles.fade-in");
            int stay = getConfig().getInt("effects.titles.stay");
            int fadeOut = getConfig().getInt("effects.titles.fade-out");
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }

        player.setVelocity(player.getVelocity().setX(0).setY(0).setZ(0));
    }

    private void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId());
        frozenLocations.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        removeIceCage(player.getUniqueId());
    }

    private void createIceCage(Player player) {
        List<Block> blocks = new ArrayList<>();
        Location loc = player.getLocation().clone().add(0, -1, 0); // Start from floor
        Material material = Material.valueOf(getConfig().getString("effects.ice-cage.material", "BLUE_STAINED_GLASS"));

        // Simple cube 3x4x3
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == -1 || x == 1 || z == -1 || z == 1 || y == 0 || y == 3) {
                        Block b = loc.clone().add(x, y, z).getBlock();
                        if (b.getType() == Material.AIR) {
                            b.setType(material);
                            blocks.add(b);
                        }
                    }
                }
            }
        }
        iceCages.put(player.getUniqueId(), blocks);
    }

    private void removeIceCage(UUID uuid) {
        List<Block> blocks = iceCages.remove(uuid);
        if (blocks != null) {
            for (Block b : blocks) {
                b.setType(Material.AIR);
            }
        }
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
        if (to == null) return;

        if (to.getX() != frozenLocation.getX() || to.getY() != frozenLocation.getY() || to.getZ() != frozenLocation.getZ() ||
            to.getYaw() != frozenLocation.getYaw() || to.getPitch() != frozenLocation.getPitch() ||
            !to.getWorld().equals(frozenLocation.getWorld())) {
            
            event.setTo(frozenLocation.clone());
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (frozenPlayers.contains(player.getUniqueId())) {
            freezePlayer(player);
            String prefix = getConfig().getString("messages.prefix");
            for (String msg : getConfig().getStringList("messages.freeze.target-notify")) {
                player.sendMessage(format(msg.replace("%admin%", "Console/System")));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (frozenPlayers.contains(uuid)) {
            if (getConfig().getBoolean("quit-check.enabled", true)) {
                String cmd = getConfig().getString("quit-check.command").replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                
                String adminNotify = getConfig().getString("quit-check.notify-admins").replace("%player%", player.getName());
                Bukkit.broadcast(format(getConfig().getString("messages.prefix") + adminNotify), "freezeplugin.use");
                
                frozenPlayers.remove(uuid);
                frozenLocations.remove(uuid);
                removeIceCage(uuid);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    playerNames.add(player.getName());
                }
            }
            return playerNames;
        }
        return Collections.emptyList();
    }

    private String format(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}


