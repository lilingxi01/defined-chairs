package com.cnaude.chairs.vehiclearrow.nms164;

import net.minecraft.server.v1_6_R3.EntityArrow;
import net.minecraft.server.v1_6_R3.World;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_6_R3.CraftServer;
import org.bukkit.entity.Arrow;

import com.cnaude.chairs.vehiclearrow.NMSChairsArrowInterface;

public class NMSChairsArrow extends EntityArrow implements NMSChairsArrowInterface {

	public NMSChairsArrow(World world) {
		super(world);
	}

	@Override
	public void l_() {
	}

	@Override
	public void setBukkitEntity(Server server) {
		bukkitEntity = new CraftChairsArrow((CraftServer) server, this);
	}

	@Override
	public Arrow getBukkitArrow() {
		return (Arrow) bukkitEntity;
	}

	@Override
	public void setArrowLocation(Location location) {
		setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

	@Override
	public void addToWorld() {
		world.addEntity(this);
	}

}
