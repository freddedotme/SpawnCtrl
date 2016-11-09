package com.icloud.fredde.SpawnCtrl;

import com.icloud.fredde.SpawnCtrl.task;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class main
extends JavaPlugin
implements Listener {
    HashMap<UUID, Location> moving = new HashMap();
    HashMap<UUID, Integer> tasks = new HashMap();
    String cancelTeleport;
    String whileTeleport;
    String onTeleport;
    String commenceTeleport;
    int delay;
    String SPAWN_NOT_FOUND = (Object)ChatColor.RED + "No spawn defined.";
    String SPAWN_REMOVED = (Object)ChatColor.YELLOW + "Spawn removed.";
    String CONFIG_RELOAD = (Object)ChatColor.GREEN + "Config reloaded!";
    String WORLD_SPAWN = (Object)ChatColor.GREEN + "World-spawn defined.";
    String MAIN_SPAWN = (Object)ChatColor.GREEN + "Main-spawn defined.";
    File dataF;
    FileConfiguration data;

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.loadConfiguration();
    }

    public void onDisable() {
    }

    public void updateConfiguration() {
        this.saveConfig();
        try {
            this.data.save(this.dataF);
        }
        catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + this.dataF, ex);
        }
    }

    public void reloadConfiguration() {
        this.reloadConfig();
        this.updateGlobalVariables();
    }

    public void loadConfiguration() {
        this.getConfig().addDefault("SpawnCtrl.cancelTeleport", (Object)"&cYou moved, teleportation cancelled.");
        this.getConfig().addDefault("SpawnCtrl.whileTeleport", (Object)"&eTeleporting in {time} seconds, don't move.");
        this.getConfig().addDefault("SpawnCtrl.onTeleport", (Object)"&aTeleported to spawn.");
        this.getConfig().addDefault("SpawnCtrl.commenceTeleport", (Object)"&eCommencing...");
        this.getConfig().addDefault("SpawnCtrl.delay", (Object)3);
        this.getConfig().options().copyDefaults(true);
        this.dataF = new File(this.getDataFolder(), "data.yml");
        if (!this.dataF.exists()) {
            try {
                this.dataF.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.data = YamlConfiguration.loadConfiguration((File)this.dataF);
        try {
            this.data.save(this.dataF);
        }
        catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save data to " + this.dataF, ex);
        }
        this.updateConfiguration();
        this.updateGlobalVariables();
    }

    @EventHandler
    public void playerMovement(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location pLoc = p.getLocation();
        Location cLoc = this.moving.get(p.getUniqueId());
        Double pX = pLoc.getX();
        Double pZ = pLoc.getZ();
        Double cX = cLoc.getX();
        Double cZ = cLoc.getZ();
        if (pX.compareTo(cX) != 0 && pZ.compareTo(cZ) != 0 && this.tasks.get(p.getUniqueId()) != null) {
            Bukkit.getScheduler().cancelTask(this.tasks.get(p.getUniqueId()).intValue());
            this.tasks.remove(p.getUniqueId());
            String msg = this.cancelTeleport.replaceAll("\\{time\\}", String.valueOf(this.delay));
            p.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)msg));
        }
    }

    @EventHandler
    public void playerLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        this.moving.put(p.getUniqueId(), p.getLocation());
    }

    public void updateGlobalVariables() {
        this.cancelTeleport = this.getConfig().getString("SpawnCtrl.cancelTeleport");
        this.whileTeleport = this.getConfig().getString("SpawnCtrl.whileTeleport");
        this.commenceTeleport = this.getConfig().getString("SpawnCtrl.commenceTeleport");
        this.onTeleport = this.getConfig().getString("SpawnCtrl.onTeleport");
        this.delay = this.getConfig().getInt("SpawnCtrl.delay");
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player)sender;
            Location l = p.getLocation();
            World w = p.getWorld();
            double x = l.getX();
            double y = l.getY() + 1.0;
            double z = l.getZ();
            ArrayList<String> positions = new ArrayList<String>();
            positions.add(String.valueOf(x));
            positions.add(String.valueOf(y));
            positions.add(String.valueOf(z));
            positions.add(w.getName());
            if (commandLabel.equalsIgnoreCase("spawn") && args.length > 0) {
                if (args[0].equalsIgnoreCase("sw") && args.length == 1 && p.hasPermission("spawnctrl.world")) {
                    this.data.set("SpawnCtrl.worlds." + w.getName() + ".pos", positions);
                    this.data.set("SpawnCtrl.worlds." + w.getName() + ".misc.direction", (Object)l.getDirection());
                    this.data.set("SpawnCtrl.worlds." + w.getName() + ".misc.pitch", (Object)String.valueOf(l.getPitch()));
                    Bukkit.getWorld((String)w.getName()).setSpawnLocation((int)x, (int)y, (int)z);
                    this.updateConfiguration();
                    p.sendMessage(this.WORLD_SPAWN);
                    return true;
                }
                if (args[0].equalsIgnoreCase("sm") && args.length == 1 && p.hasPermission("spawnctrl.main")) {
                    this.data.set("SpawnCtrl.world.main.pos", positions);
                    this.data.set("SpawnCtrl.world.main.misc.direction", (Object)l.getDirection());
                    this.data.set("SpawnCtrl.world.main.misc.pitch", (Object)Float.valueOf(l.getPitch()));
                    Bukkit.getWorld((String)w.getName()).setSpawnLocation((int)x, (int)y, (int)z);
                    p.sendMessage(this.MAIN_SPAWN);
                    this.updateConfiguration();
                    return true;
                }
                if (args[0].equalsIgnoreCase("rw") && args.length == 1 && p.hasPermission("spawnctrl.remove") && this.data.get("SpawnCtrl.worlds." + w.getName()) != null) {
                    this.data.set("SpawnCtrl.worlds." + w.getName(), (Object)null);
                    p.sendMessage(this.SPAWN_REMOVED);
                    this.updateConfiguration();
                    return true;
                }
                if (args[0].equalsIgnoreCase("rm") && args.length == 1 && p.hasPermission("spawnctrl.remove") && this.data.get("SpawnCtrl.world.main") != null) {
                    this.data.set("SpawnCtrl.world.main", (Object)null);
                    p.sendMessage(this.SPAWN_REMOVED);
                    this.updateConfiguration();
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload") && args.length == 1 && p.hasPermission("spawnctrl.reload")) {
                    this.reloadConfiguration();
                    p.sendMessage(this.CONFIG_RELOAD);
                    return true;
                }
            }
            if (commandLabel.equalsIgnoreCase("spawn") && args.length == 0 && p.hasPermission("spawnctrl.spawn")) {
                if (this.data.get("SpawnCtrl.worlds." + w.getName() + ".pos") != null) {
                    List wrapper = this.data.getStringList("SpawnCtrl.worlds." + w.getName() + ".pos");
                    x = Double.parseDouble((String)wrapper.get(0));
                    y = Double.parseDouble((String)wrapper.get(1));
                    z = Double.parseDouble((String)wrapper.get(2));
                    Vector d = this.data.getVector("SpawnCtrl.worlds." + w.getName() + ".misc.direction");
                    float pi = Float.parseFloat(this.data.getString("SpawnCtrl.worlds." + w.getName() + ".misc.pitch"));
                    l = new Location(w, x, y, z);
                    l.setDirection(d);
                    l.setPitch(pi);
                    this.handleTeleport(p, l);
                    return true;
                }
                if (this.data.get("SpawnCtrl.world.main.pos") != null) {
                    List wrapper = this.data.getStringList("SpawnCtrl.world.main.pos");
                    x = Double.parseDouble((String)wrapper.get(0));
                    y = Double.parseDouble((String)wrapper.get(1));
                    z = Double.parseDouble((String)wrapper.get(2));
                    Vector d = this.data.getVector("SpawnCtrl.world.main.misc.direction");
                    float pi = Float.parseFloat(this.data.getString("SpawnCtrl.world.main.misc.pitch"));
                    w = Bukkit.getWorld((String)((String)wrapper.get(3)));
                    l = new Location(w, x, y, z);
                    l.setDirection(d);
                    l.setPitch(pi);
                    this.handleTeleport(p, l);
                    return true;
                }
                p.sendMessage(this.SPAWN_NOT_FOUND);
            }
        }
        return true;
    }

    public void handleTeleport(Player p, Location l) {
        if (p.hasPermission("spawnctrl.bypassdelay")) {
            new task(this, p, l, this.moving, this.onTeleport, this.delay, this.tasks).runTaskLater((Plugin)this, 0);
        } else {
            String msg = this.whileTeleport.replaceAll("\\{time\\}", String.valueOf(this.delay));
            p.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)msg));
            msg = this.commenceTeleport.replaceAll("\\{time\\}", String.valueOf(this.delay));
            p.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)msg));
            this.moving.put(p.getUniqueId(), p.getLocation());
            BukkitTask task2 = new task(this, p, l, this.moving, this.onTeleport, this.delay, this.tasks).runTaskLater((Plugin)this, (long)(20 * this.delay));
            this.tasks.put(p.getUniqueId(), task2.getTaskId());
        }
    }
}
