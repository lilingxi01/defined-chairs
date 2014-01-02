/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.chairs;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class ChairsCommand implements CommandExecutor {

    private final Chairs plugin;
    public ChairsIgnoreList ignoreList;

    public ChairsCommand(Chairs instance, ChairsIgnoreList ignoreList) {
        this.plugin = instance;
        this.ignoreList = ignoreList;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        } 
        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("chairs.reload") || !(sender instanceof Player)) {
                plugin.loadConfig();
                if (plugin.sitEffectsEnabled) {
                	plugin.chairEffects.restartHealing();
                } else {
                	plugin.chairEffects.cancelHealing();
                }
                if (!plugin.msgReloaded.isEmpty()) {
                    sender.sendMessage(plugin.msgReloaded);
                }
            } else {
                if (!plugin.msgNoPerm.isEmpty()) {
                    sender.sendMessage(plugin.msgNoPerm);
                }
            }
        }
        if (args[0].equalsIgnoreCase("removearrows")) {
        	if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender || (sender instanceof Player && sender.hasPermission("chairs.removearrows"))) {
        		if (args.length == 2) {
        			World world = Bukkit.getWorld(args[1]);
        			int removed = removeArrows(world);
        			sender.sendMessage("Removed "+removed+" unused arrows");
        		} else if (args.length == 1) {
        			for (World world : Bukkit.getWorlds()) {
            			int removed = removeArrows(world);
            			sender.sendMessage("Removed "+removed+" unused arrows");
        			}
        		}
        	} 
        }
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args[0].equalsIgnoreCase("on")) {
                if (p.hasPermission("chairs.self")) {
                    ignoreList.removePlayer(p.getName());
                    if (!plugin.msgEnabled.isEmpty()) {
                        p.sendMessage(plugin.msgEnabled);
                    }
                } else {
                    if (!plugin.msgNoPerm.isEmpty()) {
                        p.sendMessage(plugin.msgNoPerm);
                    }
                }
            }
            if (args[0].equalsIgnoreCase("off")) {
                if (p.hasPermission("chairs.self")) {
                    ignoreList.addPlayer(p.getName());
                    if (!plugin.msgDisabled.isEmpty()) {
                        p.sendMessage(plugin.msgDisabled);
                    }
                } else {
                    if (!plugin.msgNoPerm.isEmpty()) {
                        p.sendMessage(plugin.msgNoPerm);
                    }
                }
            }
        }
        return true;
    }
    
    private int removeArrows(World world) {
		Iterator<Entity> entityit = world.getEntities().iterator();
		int removed = 0;
		while (entityit.hasNext()) {
			Entity entity = entityit.next();
			if (entity instanceof Arrow) {
				if (!plugin.getPlayerSitData().isAroowOccupied(entity)) {
					entity.remove();
					removed++;
				}
			}
		}
		return removed;
    }
    
}
