package com.darksoldier1404.dht.events;

import com.darksoldier1404.dht.HotTime;
import com.darksoldier1404.dht.functions.DHTFunction;
import com.darksoldier1404.dht.functions.OpenType;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.utils.Tuple;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;

@SuppressWarnings("all")
public class DHTEvent implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if(e.getInventory() instanceof DInventory) {
            DInventory inv = (DInventory) e.getInventory();
            if(inv.getObj() != null) {
                if(inv.isValidHandler(HotTime.getInstance())) {
                    DHTFunction.saveInventory(p, inv);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getClickedInventory() == null) {
            return;
        }
        if(e.getView().getTopInventory() instanceof DInventory) {
            DInventory inv = (DInventory) e.getView().getTopInventory();
            if(inv.isValidHandler(HotTime.getInstance())) {
                if(inv.getObj() != null) {
                    Tuple<OpenType, String> tuple = (Tuple<OpenType, String>) inv.getObj();
                    if(tuple.getA() == OpenType.Reward) {
                        if(e.getClickedInventory() instanceof PlayerInventory || e.getClick() != ClickType.LEFT){
                            e.setCancelled(true);
                            return;
                        }
                        if(e.getCurrentItem() != null) {
                            Player p = (Player) e.getWhoClicked();
                            if(p.getInventory().firstEmpty() != -1) {
                                p.getInventory().addItem(e.getCurrentItem().clone());
                                e.getCurrentItem().setAmount(0);
                            }else{
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        DHTFunction.initUserData(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        DHTFunction.saveAndLeave(e.getPlayer().getUniqueId());
    }
}
