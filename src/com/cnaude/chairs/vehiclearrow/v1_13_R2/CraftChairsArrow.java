package com.cnaude.chairs.vehiclearrow.v1_13_R2;

import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.BoundingBox;

import com.cnaude.chairs.api.ChairsAPI;

import net.minecraft.server.v1_13_R2.EntityArrow;

public class CraftChairsArrow extends CraftArrow implements Vehicle {

	public CraftChairsArrow(CraftServer server, EntityArrow entity) {
		super(server, entity);
	}

	@Override
	public void remove() {
		Entity passenger = getPassenger();
		if ((passenger != null) && (passenger instanceof Player)) {
			if (ChairsAPI.isSitting((Player) passenger)) {
				return;
			}
		}
		super.remove();
	}

	@Override
	public BoundingBox getBoundingBox() {
		return new BoundingBox();
	}

}
