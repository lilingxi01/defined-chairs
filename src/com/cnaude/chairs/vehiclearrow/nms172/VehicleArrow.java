package com.cnaude.chairs.vehiclearrow.nms172;

import net.minecraft.server.v1_7_R1.EntityArrow;

import org.bukkit.craftbukkit.v1_7_R1.CraftServer;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftArrow;
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
