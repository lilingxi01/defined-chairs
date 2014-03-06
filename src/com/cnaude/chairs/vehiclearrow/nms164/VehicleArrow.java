package com.cnaude.chairs.vehiclearrow.nms164;

import net.minecraft.server.v1_6_R3.EntityArrow;

import org.bukkit.craftbukkit.v1_6_R3.CraftServer;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftArrow;
import org.bukkit.entity.Vehicle;

public class VehicleArrow extends CraftArrow implements Vehicle {

	public VehicleArrow(CraftServer server, EntityArrow entity) {
		super(server, entity);
	}

	@Override
	public void remove() {
		if (isEmpty()) {
			super.remove();
		}
	}

}
