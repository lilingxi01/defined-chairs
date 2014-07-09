package com.cnaude.chairs.core;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.cnaude.chairs.api.PlayerChairSitEvent;
import com.cnaude.chairs.api.PlayerChairUnsitEvent;

public class PlayerSitData {

	private Chairs plugin;
	public PlayerSitData(Chairs plugin) {
		this.plugin = plugin;
	}

	private HashMap<String, Entity> sit = new HashMap<String, Entity>();
	private HashMap<Block, String> sitblock = new HashMap<Block, String>();
	private HashMap<String, Location> sitstopteleportloc = new HashMap<String, Location>();
	private HashMap<String, Integer> sittask = new HashMap<String, Integer>();

	public boolean isSitting(Player player) {
		return sit.containsKey(player.getName());
	}

	public boolean isBlockOccupied(Block block) {
		return sitblock.containsKey(block);
	}

	public Player getPlayerOnChair(Block chair) {
		return Bukkit.getPlayerExact(sitblock.get(chair));
	}

	public boolean sitPlayer(Player player,  Block blocktooccupy, Location sitlocation) {
		PlayerChairSitEvent playersitevent = new PlayerChairSitEvent(player, sitlocation);
		Bukkit.getPluginManager().callEvent(playersitevent);
		if (playersitevent.isCancelled()) {
			return false;
		}
		sitlocation = playersitevent.getSitLocation();
		try {
			if (plugin.notifyplayer) {
				player.sendMessage(plugin.msgSitting);
			}
			sitstopteleportloc.put(player.getName(), player.getLocation());
			player.teleport(sitlocation);
			Location arrowloc = sitlocation.getBlock().getLocation().add(0.5, 0 , 0.5);
			Entity arrow = plugin.getNMSAccess().spawnArrow(arrowloc);
			arrow.setPassenger(player);
			sit.put(player.getName(), arrow);
			sitblock.put(blocktooccupy, player.getName());
			startReSitTask(player);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public void startReSitTask(final Player player) {
		int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(
			plugin,
			new Runnable() {
				@Override
				public void run() {
					reSitPlayer(player);
				}
			},
			1000, 1000
		);
		sittask.put(player.getName(), task);
	}

	public void reSitPlayer(final Player player) {
		try {
			final Entity prevarrow = sit.get(player.getName());
			sit.remove(player.getName());
			player.eject();
			Entity arrow = plugin.getNMSAccess().spawnArrow(prevarrow.getLocation());
			arrow.setPassenger(player);
			sit.put(player.getName(), arrow);
			Bukkit.getScheduler().scheduleSyncDelayedTask(
				plugin,
				new Runnable() {
					@Override
					public void run() {
						prevarrow.remove();
					}
				},
				100
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean unsitPlayerNormal(Player player) {
		UnsitParams params = new UnsitParams(false, true, false);
		return unsitPlayer(player, true, params);
	}

	public void unsitPlayerForce(Player player) {
		UnsitParams params = new UnsitParams(true, true, false);
		unsitPlayer(player, false, params);
	}

	public void unsitPlayerNow(Player player) {
		UnsitParams params = new UnsitParams(true, false, true);
		unsitPlayer(player, false, params);
	}

	private boolean unsitPlayer(final Player player, boolean canCancel, UnsitParams params) {
		final PlayerChairUnsitEvent playerunsitevent = new PlayerChairUnsitEvent(player, sitstopteleportloc.get(player.getName()), canCancel);
		Bukkit.getPluginManager().callEvent(playerunsitevent);
		if (playerunsitevent.isCancelled() && playerunsitevent.canBeCancelled()) {
			return false;
		}
		final Entity arrow = sit.get(player.getName());
		sit.remove(player.getName());
		if (params.eject()) {
			player.eject();
		}
		arrow.remove();
		if (params.restorePostion()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(
				plugin,
				new Runnable() {
					@Override
					public void run() {
						player.teleport(playerunsitevent.getTeleportLocation());
						player.setSneaking(false);
					}
				},
				1
			);
		} else if (params.correctLeavePosition()) {
			player.teleport(playerunsitevent.getTeleportLocation());
		}
		sitblock.values().remove(player.getName());
		sitstopteleportloc.remove(player.getName());
		Bukkit.getScheduler().cancelTask(sittask.get(player.getName()));
		sittask.remove(player.getName());
		if (plugin.notifyplayer) {
			player.sendMessage(plugin.msgStanding);
		}
		return true;
	}

	private class UnsitParams {

		private boolean eject;
		private boolean restoreposition;
		private boolean correctleaveposition;

		public UnsitParams(boolean eject, boolean restoreposition, boolean correctleaveposition) {
			this.eject = eject;
			this.restoreposition = restoreposition;
			this.correctleaveposition = correctleaveposition;
		}

		public boolean eject() {
			return eject;
		}

		public boolean restorePostion() {
			return restoreposition;
		}

		public boolean correctLeavePosition() {
			return correctleaveposition;
		}

	}

}
