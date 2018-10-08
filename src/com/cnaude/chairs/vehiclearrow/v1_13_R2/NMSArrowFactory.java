package com.cnaude.chairs.vehiclearrow.v1_13_R2;

import com.cnaude.chairs.vehiclearrow.NMSArrowFactoryInterface;
import net.minecraft.server.v1_13_R2.EntityArrow;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.entity.Arrow;

public class NMSArrowFactory implements NMSArrowFactoryInterface {

	@Override
	public Arrow spawnArrow(Location location) {
		CraftWorld world = (CraftWorld) location.getWorld();
		EntityArrow arrow = new NMSChairsArrow(world, location);
		return (Arrow) arrow.getBukkitEntity();
	}

}
