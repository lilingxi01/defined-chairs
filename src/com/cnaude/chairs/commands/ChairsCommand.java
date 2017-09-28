package com.cnaude.chairs.commands;

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.cnaude.chairs.core.Chairs;
import com.cnaude.chairs.core.Utils;

public class ChairsCommand implements CommandExecutor {

	private final Chairs plugin;

	public ChairsCommand(Chairs instance) {
		this.plugin = instance;
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
				sender.sendMessage(plugin.msgReloaded);
			} else {
				sender.sendMessage(plugin.msgNoPerm);
			}
		}
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args[0].equalsIgnoreCase("off")) {
				plugin.sitDisabled.add(player.getUniqueId());
				player.sendMessage(plugin.msgDisabled);
			} else if (args[0].equalsIgnoreCase("on")) {
				plugin.sitDisabled.remove(player.getUniqueId());
				player.sendMessage(plugin.msgEnabled);
			} else if (args[0].equalsIgnoreCase("sit")) {
				if (sender.hasPermission("chairs.sit.command")) {
					Block block = player.getTargetBlock((Set<Material>)null, (int) plugin.distance);
					if (plugin.utils.sitAllowed(player, block, false)) {
						Location sitLocation = plugin.utils.getSitLocation(block, player.getLocation().getYaw());
						plugin.getPlayerSitData().sitPlayer(player, block, sitLocation);
					} else {
						player.sendMessage(plugin.msgCantSitThere);
					}
				} else {
					sender.sendMessage(plugin.msgNoPerm);
				}
			}
		}
		return true;
	}

}
