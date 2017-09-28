package com.cnaude.chairs.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.material.WoodenStep;

import com.cnaude.chairs.core.Chairs;
import com.cnaude.chairs.core.Utils;

public class TrySitEventListener implements Listener {

	public Chairs plugin;

	public TrySitEventListener(Chairs plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
			Player player = event.getPlayer();
			Block block = event.getClickedBlock();
			if (plugin.utils.sitAllowed(player, block, plugin.signCheck)) {
				Location sitLocation = plugin.utils.getSitLocation(block, player.getLocation().getYaw());
				if (plugin.getPlayerSitData().sitPlayer(player, block, sitLocation)) {
					event.setCancelled(true);
				}
			}
		}
	}
}