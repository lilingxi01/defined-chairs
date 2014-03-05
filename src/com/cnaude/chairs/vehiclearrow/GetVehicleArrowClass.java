package com.cnaude.chairs.vehiclearrow;

import org.bukkit.Bukkit;

public class GetVehicleArrowClass {

	private String pkgname = this.getClass().getPackage().getName();
	private String vehiclearrowclassname = "VehicleArrow";
	public Class<?> getVehicleArrowClass(String arrowclass, Class<?> entityarrow, Class<?> craftserver) throws Exception {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String nmspackageversion = packageName.substring(packageName.lastIndexOf('.') + 1);
        if (nmspackageversion.equals("v1_7_R1")) {
        	return Class.forName(pkgname+"."+"nms172"+"."+vehiclearrowclassname);
        } else if (nmspackageversion.equals("v1_6_R3")) {
        	return Class.forName(pkgname+"."+"nms164"+"."+vehiclearrowclassname);
        }
        throw new Exception("ChairsReloaded is not compatible with your server version");
	}

}
