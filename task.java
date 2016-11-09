package com.icloud.fredde.SpawnCtrl;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class task
extends BukkitRunnable {
    Player p;
    Location l;
    HashMap h;
    HashMap t;
    String onTeleport;
    int delay;
    private final JavaPlugin plugin;

    public task(JavaPlugin plugin, Player sender, Location target, HashMap db, String msg, int d, HashMap ts) {
        this.plugin = plugin;
        this.p = sender;
        this.l = target;
        this.h = db;
        this.onTeleport = msg;
        this.delay = d;
        this.t = ts;
    }

    public void run() {
        this.h.put(this.p.getUniqueId(), this.p.getLocation());
        this.p.teleport(this.l);
        this.t.remove(this.p.getUniqueId());
        String msg = this.onTeleport.replaceAll("\\{time\\}", String.valueOf(this.delay));
        this.p.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)msg));
        Location e = new Location(this.p.getWorld(), this.p.getLocation().getX(), this.p.getLocation().getY() + 2.0, this.p.getLocation().getZ());
        this.p.getWorld().playEffect(e, Effect.SPELL, 0);
    }
