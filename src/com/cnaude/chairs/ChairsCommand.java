/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.chairs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
                if (plugin.sitHealEnabled) {
                	plugin.chairEffects.restartHealing();
                } else {
                	plugin.chairEffects.cancelHealing();
                }
                if (plugin.sitPickupEnabled) {
                	plugin.chairEffects.restartPickup();
                } else {
                	plugin.chairEffects.cancelPickup();
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
    
}
