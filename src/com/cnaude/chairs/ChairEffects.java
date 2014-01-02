/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.chairs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class ChairEffects {

    private Chairs plugin;
    private int healTaskID = -1;
    private int pickupTaskID = -1;
    

    public ChairEffects(Chairs plugin) {
        this.plugin = plugin;
    }
    
    public void startHealing() {
        healEffectsTask();
    }

    public void cancelHealing() {
    	if (healTaskID != -1) {
    		plugin.getServer().getScheduler().cancelTask(healTaskID);
    		healTaskID = -1;
    	}
    }
    
    public void restartHealing() {
    	cancelHealing();
        startHealing();
    }

    private void healEffectsTask() {
        healTaskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getPlayerSitData().isSitting(p)) {
                        if (p.hasPermission("chairs.sit.health")) {
                            double pHealthPcnt = ((double) p.getHealth()) / (double) p.getMaxHealth() * 100d;
                            if ((pHealthPcnt < plugin.sitMaxHealth) && (p.getHealth() < p.getMaxHealth())) {
                                double newHealth = plugin.sitHealthPerInterval + p.getHealth();
                                if (newHealth > p.getMaxHealth()) {
                                    newHealth = p.getMaxHealth();
                                }
                                p.setHealth(newHealth);
                            }
                        }
                    }
                }
            }
        }, plugin.sitHealInterval, plugin.sitHealInterval);
    }
    
    public void startPickup() {
    	pickupEffectsTask();
    }

    public void cancelPickup() {
    	if (pickupTaskID != -1)
        plugin.getServer().getScheduler().cancelTask(pickupTaskID);
        pickupTaskID = -1;
    }
    
    public void restartPickup() {
    	cancelPickup();
    	startPickup();
    }
    
    private void pickupEffectsTask() {
    	pickupTaskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
    		public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                	if (plugin.getPlayerSitData().isSitting(p)) {
                		for (Entity entity : p.getNearbyEntities(1, 1, 1)) {
                			if (entity instanceof Item) {
                                if (p.getInventory().firstEmpty() != -1) {
                    				Item item = Item.class.cast(entity);
                    				if (item.getPickupDelay() == 0) {
                    					p.getInventory().addItem(item.getItemStack());
                                		entity.remove();
                    				}
                                }
                			}
                		}
                    }
                }
    		}
    	},0,1);
    }
    
}
