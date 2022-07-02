package com.darksoldier1404.dht;

import com.darksoldier1404.dht.commands.DHTCommand;
import com.darksoldier1404.dht.events.DHTEvent;
import com.darksoldier1404.dht.functions.DHTFunction;
import com.darksoldier1404.dht.functions.HotTimeType;
import com.darksoldier1404.dppc.utils.ConfigUtils;
import com.darksoldier1404.dppc.utils.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HotTime extends JavaPlugin {
    private static HotTime plugin;
    public static YamlConfiguration config;
    public static Map<UUID, YamlConfiguration> udata = new HashMap<>();
    public static String prefix;
    public static Map<String, Tuple<HotTimeType, String>> hottimes = new HashMap<>();
    public static Map<String, BukkitTask> enabled = new HashMap<>();

    public static HotTime getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        config = ConfigUtils.loadDefaultPluginConfig(plugin);
        prefix = ChatColor.translateAlternateColorCodes('&', config.getString("Settings.prefix"));
        plugin.getServer().getPluginManager().registerEvents(new DHTEvent(), plugin);
        getCommand("핫타임").setExecutor(new DHTCommand());
        DHTFunction.loadAllHotTimes();
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(p -> DHTFunction.saveUserData(p.getUniqueId()));
        DHTFunction.saveConfig();
    }
}
