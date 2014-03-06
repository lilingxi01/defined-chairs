package com.cnaude.chairs.vehiclearrow;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;

public class NMSAccess {

	private Class<?> nmsArrowClass;

	public void setupVehicleArrow() throws Exception {
		String pkgname = getClass().getPackage().getName();
		String packageName = Bukkit.getServer().getClass().getPackage().getName();
		String nmspackageversion = packageName.substring(packageName.lastIndexOf('.') + 1);
		switch (nmspackageversion) {
			case "v1_7_R1": {
				nmsArrowClass = Class.forName(pkgname+"."+"nms172"+".NMSChairsArrow");
				return;
			}
			case "v1_6_R3": {
				nmsArrowClass = Class.forName(pkgname+"."+"nms164"+".NMSChairsArrow");
				return;
			}
		}
		throw new Exception("ChairsReloaded is not compatible with your server version");
	}

	public Arrow spawnArrow(Location location) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		World world = location.getWorld();
		Method getHandle = world.getClass().getDeclaredMethod("getHandle");
		getHandle.setAccessible(true);
		Object nmsworld = getHandle.invoke(world);
		Constructor<?> ct = nmsArrowClass.getConstructor(nmsworld.getClass().getSuperclass());
		ct.setAccessible(true);
		NMSChairsArrowInterface vehiclearrow = (NMSChairsArrowInterface) ct.newInstance(nmsworld);
		vehiclearrow.setArrowLocation(location);
		vehiclearrow.addToWorld();
		vehiclearrow.setBukkitEntity(Bukkit.getServer());
		return vehiclearrow.getBukkitArrow();
	}

}
