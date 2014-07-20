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

	private HashMap<Player, SitData> sit = new HashMap<Player, SitData>();
	private HashMap<Block, Player> sitblock = new HashMap<Block, Player>();

	public boolean isSitting(Player player) {
		return sit.containsKey(player) && sit.get(player).sitting;
	}

	public boolean isBlockOccupied(Block block) {
		return sitblock.containsKey(block);
	}

	public Player getPlayerOnChair(Block chair) {
		return sitblock.get(chair);
	}

	public boolean sitPlayer(final Player player,  Block blocktooccupy, Location sitlocation) {
		PlayerChairSitEvent playersitevent = new PlayerChairSitEvent(player, sitlocation.clone());
		Bukkit.getPluginManager().callEvent(playersitevent);
		if (playersitevent.isCancelled()) {
			return false;
		}
		sitlocation = playersitevent.getSitLocation().clone();
		if (plugin.notifyplayer) {
			player.sendMessage(plugin.msgSitting);
		}
		SitData sitdata = new SitData();
		Location arrowloc = sitlocation.getBlock().getLocation().add(0.5, 0 , 0.5);
		Entity arrow = plugin.getNMSAccess().spawnArrow(arrowloc);
		sitdata.arrow = arrow;
		sitdata.teleportloc = player.getLocation();
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
		sitdata.resittask = task;
		player.teleport(sitlocation);
		arrow.setPassenger(player);
		sitdata.sitting = true;
		sit.put(player, sitdata);
		sitblock.put(blocktooccupy, player);
		return true;
	}

	public void reSitPlayer(final Player player) {
		SitData sitdata = sit.get(player);
		sitdata.sitting = false;
		final Entity prevarrow = sit.get(player).arrow;
		player.eject();
		Entity arrow = plugin.getNMSAccess().spawnArrow(prevarrow.getLocation());
		arrow.setPassenger(player);
		sitdata.arrow = arrow;
		sitdata.sitting = true;
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
		SitData sitdata = sit.get(player);
		final PlayerChairUnsitEvent playerunsitevent = new PlayerChairUnsitEvent(player, sitdata.teleportloc.clone(), canCancel);
		Bukkit.getPluginManager().callEvent(playerunsitevent);
		if (playerunsitevent.isCancelled() && playerunsitevent.canBeCancelled()) {
			return false;
		}
		sitdata.sitting = false;
		if (params.eject()) {
			player.eject();
		}
		sitdata.arrow.remove();
		if (params.restorePostion()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(
				plugin,
				new Runnable() {
					@Override
					public void run() {
						player.teleport(playerunsitevent.getTeleportLocation().clone());
						player.setSneaking(false);
					}
				},
				1
			);
		} else if (params.correctLeavePosition()) {
			player.teleport(playerunsitevent.getTeleportLocation());
		}
		sitblock.values().remove(player);
		Bukkit.getScheduler().cancelTask(sitdata.resittask);
		sit.remove(player);
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

	private class SitData {

		private boolean sitting;
		private Entity arrow;
		private Location teleportloc;
		private int resittask;

	}

}
