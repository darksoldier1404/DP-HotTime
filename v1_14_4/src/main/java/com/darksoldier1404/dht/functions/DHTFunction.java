package com.darksoldier1404.dht.functions;

import com.darksoldier1404.dht.HotTime;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.utils.ConfigUtils;
import com.darksoldier1404.dppc.utils.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static java.lang.Thread.sleep;

@SuppressWarnings("all")
public class DHTFunction {
    private static final HotTime plugin = HotTime.getInstance();

    public static void createHotTime(CommandSender sender, String name, String type, String time) {
        if (plugin.hottimes.containsKey(name)) {
            sender.sendMessage(plugin.prefix + "이미 존재하는 핫타임 입니다.");
            return;
        }
        if (!type.equalsIgnoreCase("f") && !type.equalsIgnoreCase("p")) {
            sender.sendMessage(plugin.prefix + "올바른 타입이 아닙니다.");
            return;
        }
        if (!time.matches("((?i)[0-9]{1,2}:??[0-9]{0,2})")) {
            sender.sendMessage(plugin.prefix + "올바른 시간이 아닙니다.");
        }
        HotTimeType ht = HotTimeType.valueOf(type.toUpperCase());
        plugin.hottimes.put(name, Tuple.of(ht, time));
        plugin.config.set("Settings.HotTimes." + name + ".Type", type);
        plugin.config.set("Settings.HotTimes." + name + ".Time", time);
        sender.sendMessage(plugin.prefix + "핫타임이 생성되었습니다.");
        saveConfig();
    }

    public static void deleteHotTime(CommandSender sender, String name) {
        if (!plugin.hottimes.containsKey(name)) {
            sender.sendMessage(plugin.prefix + "해당 핫타임이 존재하지 않습니다.");
            return;
        }
        plugin.hottimes.remove(name);
        plugin.config.set("Settings.HotTimes." + name, null);
        sender.sendMessage(plugin.prefix + "핫타임이 삭제되었습니다.");
        saveConfig();
    }

    public static void listHotTimes(CommandSender sender) {
        sender.sendMessage(plugin.prefix + "핫타임 목록");
        for (String name : plugin.hottimes.keySet()) {
            sender.sendMessage(name + " : " + plugin.hottimes.get(name).getA() + " : " + plugin.hottimes.get(name).getB());
        }
    }

    public static void switchHotTime(CommandSender sender, String name) {
        if (!plugin.hottimes.containsKey(name)) {
            sender.sendMessage(plugin.prefix + "해당 핫타임이 존재하지 않습니다.");
            return;
        }
        if (!plugin.enabled.containsKey(name)) {
            if (plugin.hottimes.get(name).getA() == HotTimeType.F) {
                String stime = plugin.hottimes.get(name).getB() + ":00";
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    Date time = new Date();
                    if (new SimpleDateFormat("HH:mm:ss").format(time).equals(stime)) {
                        startHotTime(name);
                    }
                }, 0L, 20L);
                plugin.enabled.put(name, task);
                plugin.config.set("Settings.HotTimes." + name + ".Enabled", true);
            }
            if (plugin.hottimes.get(name).getA() == HotTimeType.P) {
                String stime = plugin.hottimes.get(name).getB() + ":00";
                Date time = new Date();
                SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    Date d = f.parse(stime);
                    long secs = d.getTime() / 1000;
                    BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        startHotTime(name);
                    }, 20 * secs, secs * 20L);
                    plugin.enabled.put(name, task);
                    plugin.config.set("Settings.HotTimes." + name + ".Enabled", true);
                } catch (ParseException e) {
                    e.printStackTrace();
                    System.out.println(name + "핫타임의 시간이 옳바르지 않습니다.");
                }
            }
            sender.sendMessage(plugin.prefix + "핫타임이 활성화되었습니다.");
        } else {
            plugin.enabled.get(name).cancel();
            plugin.enabled.remove(name);
            sender.sendMessage(plugin.prefix + "핫타임이 비활성화되었습니다.");
            plugin.config.set("Settings.HotTimes." + name + ".Enabled", false);
        }
        saveConfig();
    }

    public static void startHotTime(String name) {
        if(plugin.config.getBoolean("Settings.enableCountDown")) {
            List<String> cooldown = plugin.config.getStringList("Settings.CountDown");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    for (String s : cooldown) {
                        if(s.contains("delay:")) {
                            String[] split = s.split(":");
                            int delay = Integer.parseInt(split[1].replace(">", ""));
                            try {
                                sleep(delay*1000);
                                continue;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else{
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', s));
                        }
                    }
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        giveReward(p, name);
                    });
                }
            });
        }
    }

    public static void giveReward(Player p, String name) {
        if(p.getOpenInventory().getTopInventory() instanceof DInventory) {
            DInventory inv = (DInventory) p.getOpenInventory().getTopInventory();
            if(inv.isValidHandler(plugin)) {
                saveRewardInventory(p, inv);
                for(String key : plugin.config.getConfigurationSection("Settings.HotTimes." + name + ".Reward").getKeys(false)) {
                    inv.addItem(plugin.config.getItemStack("Settings.HotTimes." + name + ".Reward." + key));
                }
                YamlConfiguration data = plugin.udata.get(p.getUniqueId());
                for (int i = 0; i < inv.getSize(); i++) {
                    data.set("Rewards." + i, inv.getItem(i));
                }
                return;
            }
        }
        int line = plugin.config.getInt("Settings.RewardSotrageLine");
        Inventory inv = Bukkit.createInventory(null, line * 9);
        for(String key : plugin.udata.get(p.getUniqueId()).getConfigurationSection("Rewards").getKeys(false)) {
            inv.setItem(Integer.parseInt(key), plugin.udata.get(p.getUniqueId()).getItemStack("Rewards." + key));
        }
        for(String key : plugin.config.getConfigurationSection("Settings.HotTimes." + name + ".Reward").getKeys(false)) {
            inv.addItem(plugin.config.getItemStack("Settings.HotTimes." + name + ".Reward." + key));
        }
        YamlConfiguration data = plugin.udata.get(p.getUniqueId());
        for (int i = 0; i < inv.getSize(); i++) {
            data.set("Rewards." + i, inv.getItem(i));
        }
    }

    public static void openRewardStorage(Player p) {
        int line = plugin.config.getInt("Settings.RewardSotrageLine");
        DInventory inv = new DInventory(null, "§6핫타임 보상 보관함", line * 9, plugin);
        for(String key : plugin.udata.get(p.getUniqueId()).getConfigurationSection("Rewards").getKeys(false)) {
            inv.setItem(Integer.parseInt(key), plugin.udata.get(p.getUniqueId()).getItemStack("Rewards." + key));
        }
        inv.setObj(Tuple.of(OpenType.Reward, "RewardStorage"));
        p.openInventory(inv);
    }

    public static void openRewardSettings(CommandSender sender, String name) {
        if (!plugin.hottimes.containsKey(name)) {
            sender.sendMessage(plugin.prefix + "해당 핫타임이 존재하지 않습니다.");
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.prefix + "인게임에서만 사용할 수 있습니다.");
            return;
        }
        Player p = (Player) sender;
        DInventory inv = new DInventory(null, name + " 핫타임 보상 설정", 27, plugin);
        if(plugin.config.get("Settings.HotTimes." + name + ".Reward") != null) {
            for (String key : plugin.config.getConfigurationSection("Settings.HotTimes." + name + ".Reward").getKeys(false)) {
                inv.setItem(Integer.parseInt(key), plugin.config.getItemStack("Settings.HotTimes." + name + ".Reward." + key));
            }
        }
        inv.setObj(Tuple.of(OpenType.Setting, name));
        p.openInventory(inv);
    }

    public static void changeStorageLine(CommandSender sender, String sline) {
        try{
            if(plugin.config.get("Settings.RewardSotrageLine") == null) {
                plugin.config.set("Settings.RewardSotrageLine", Integer.parseInt(sline));
            }else{
                plugin.config.set("Settings.RewardSotrageLine", Integer.parseInt(sline));
            }
        }catch (NumberFormatException e) {
            sender.sendMessage(plugin.prefix + "옳바르지 않은 숫자입니다.");
            return;
        }
        plugin.saveConfig();
        sender.sendMessage(plugin.prefix + "핫타임 보상 보관함의 줄을 " + sline + "줄로 변경했습니다.");
    }

    public static void saveInventory(Player p, DInventory inv) {
        if (inv.isValidHandler(plugin)) {
            if (inv.getObj() != null) {
                Tuple<OpenType, String> obj = (Tuple<OpenType, String>) inv.getObj();
                if (obj.getA() == OpenType.Setting) {
                    String name = obj.getB();
                    for (int i = 0; i < inv.getSize(); i++) {
                        plugin.config.set("Settings.HotTimes." + name + ".Reward." + i, inv.getItem(i));
                    }
                    p.sendMessage(plugin.prefix + "핫타임 보상이 저장되었습니다.");
                    saveConfig();
                }
                if (obj.getA() == OpenType.Reward) {
                    for(int i = 0; i < inv.getSize(); i++) {
                        plugin.udata.get(p.getUniqueId()).set("Rewards." + i, inv.getItem(i));
                    }
                    saveUserData(p.getUniqueId());
                }
            }
        }
    }

    public static void saveRewardInventory(Player p, DInventory inv) {
        if (inv.isValidHandler(plugin)) {
            if (inv.getObj() != null) {
                Tuple<OpenType, String> obj = (Tuple<OpenType, String>) inv.getObj();
                if (obj.getA() == OpenType.Reward) {
                    YamlConfiguration data = plugin.udata.get(p.getUniqueId());
                    for (int i = 0; i < inv.getSize(); i++) {
                        data.set("Rewards." + i, inv.getItem(i));
                    }
                    saveConfig();
                }
            }
        }
    }

    public static void changeTime(CommandSender sender, String name, String type, String time) {
        if (!plugin.hottimes.containsKey(name)) {
            sender.sendMessage(plugin.prefix + "존재하지 않은 핫타임 입니다.");
            return;
        }
        if (!type.equalsIgnoreCase("f") && !type.equalsIgnoreCase("p")) {
            sender.sendMessage(plugin.prefix + "올바른 타입이 아닙니다.");
            return;
        }
        if (!time.matches("((?i)[0-9]{1,2}:??[0-9]{0,2})")) {
            sender.sendMessage(plugin.prefix + "올바른 시간이 아닙니다.");
        }
        HotTimeType ht = HotTimeType.valueOf(type.toUpperCase());
        Tuple<HotTimeType, String> tpl = plugin.hottimes.get(name);
        tpl.setA(ht);
        tpl.setB(time);
        plugin.hottimes.put(name, tpl);
        plugin.config.set("Settings.HotTimes." + name + ".Type", type);
        plugin.config.set("Settings.HotTimes." + name + ".Time", time);
        sender.sendMessage(plugin.prefix + "핫타임이 수정되었습니다.");
        if(plugin.enabled.containsKey(name)) {
            plugin.enabled.get(name).cancel();
            switchHotTime(sender, name);
        }
        saveConfig();

    }

    public static void saveConfig() {
        ConfigUtils.savePluginConfig(plugin, plugin.config);
    }

    public static void initUserData(UUID uuid) {
        YamlConfiguration data = ConfigUtils.initUserData(plugin, uuid.toString());
        if(data.get("Rewards") == null) {
            data.set("Rewards.0", new ItemStack(Material.AIR));
        }
        plugin.udata.put(uuid, data);
    }

    public static void saveUserData(UUID uuid) {
        ConfigUtils.saveCustomData(plugin, plugin.udata.get(uuid), uuid.toString(), "data");
    }

    public static void saveAndLeave(UUID uuid) {
        saveUserData(uuid);
        plugin.udata.remove(uuid);
    }

    public static void loadAllHotTimes() {
        for(String key : plugin.config.getConfigurationSection("Settings.HotTimes").getKeys(false)) {
            Tuple<HotTimeType, String> tpl = Tuple.of(HotTimeType.valueOf(plugin.config.getString("Settings.HotTimes." + key + ".Type").toUpperCase()), plugin.config.getString("Settings.HotTimes." + key + ".Time"));
            plugin.hottimes.put(key, tpl);
            System.out.println(key + " 핫타임 로드 : " + tpl.getA() + " " + tpl.getB());
            if(plugin.config.getBoolean("Settings.HotTimes." + key + ".Enabled")) {
                if (plugin.hottimes.get(key).getA() == HotTimeType.F) {
                    String stime = plugin.hottimes.get(key).getB() + ":00";
                    BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        Date time = new Date();
                        if (new SimpleDateFormat("HH:mm:ss").format(time).equals(stime)) {
                            startHotTime(key);
                        }
                    }, 0L, 20L);
                    plugin.enabled.put(key, task);
                }
                if (plugin.hottimes.get(key).getA() == HotTimeType.P) {
                    String stime = plugin.hottimes.get(key).getB() + ":00";
                    Date time = new Date();
                    SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
                    f.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date d = f.parse(stime);
                        long secs = d.getTime() / 1000;
                        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                            startHotTime(key);
                        }, 20 * secs, secs * 20L);
                        plugin.enabled.put(key, task);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        System.out.println(key + " 핫타임의 시간이 옳바르지 않습니다.");
                    }
                }
            }
        }
    }
}
