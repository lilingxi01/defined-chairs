package com.cnaude.chairs.vehiclearrow.v1_13_R1;

import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_13_R1.CraftServer;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.BoundingBox;

import com.cnaude.chairs.api.ChairsAPI;

import net.minecraft.server.v1_13_R1.EntityArrow;

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
	public BlockFace getFacing() {
		return BlockFace.DOWN;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return new BoundingBox();
	}

}
