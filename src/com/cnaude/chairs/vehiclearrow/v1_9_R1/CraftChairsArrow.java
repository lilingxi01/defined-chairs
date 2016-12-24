package com.cnaude.chairs.vehiclearrow.v1_9_R1;

import net.minecraft.server.v1_9_R1.EntityArrow;

import java.util.Collections;
import java.util.Set;

import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

import com.cnaude.chairs.api.ChairsAPI;

public class CraftChairsArrow extends CraftArrow implements Vehicle {

	public CraftChairsArrow(CraftServer server, EntityArrow entity) {
		super(server, entity);
	}

	@Override
	public void remove() {
		Entity passenger = getPassenger();
		if (passenger != null && passenger instanceof Player) {
			if (ChairsAPI.isSitting((Player) passenger)) {
				return;
			}
		}
		super.remove();
	}


	@Override
	public void setInvulnerable(boolean invulnerable) {
	}

	@Override
	public boolean isInvulnerable() {
		return true;
	}

	@Override
	public boolean isSilent() {
		return true;
	}

	@Override
	public void setSilent(boolean silent) {
	}

	@Override
	public boolean hasGravity() {
		return false;
	}

	@Override
	public void setGravity(boolean gravity) {
	}

	@Override
	public int getPortalCooldown() {
		return 0;
	}

	@Override
	public void setPortalCooldown(int val) {
	}

	@Override
	public Set<String> getScoreboardTags() {
		return Collections.emptySet();
	}

	@Override
	public boolean addScoreboardTag(String tag) {
		return false;
	}

	@Override
	public boolean removeScoreboardTag(String tag) {
		return false;
	}

}
