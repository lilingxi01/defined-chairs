package com.cnaude.chairs.vehiclearrow;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Vehicle;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class GenVehicleArrowClass {

	public Class<?> genAndLoadClass(String arrowclass, Class<?> entityarrow, Class<?> craftserver) throws IOException, ClassNotFoundException, NotFoundException, CannotCompileException 
	{
		ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath
		(
				new ClassClassPath(Bukkit.class)
		);
		CtClass vehiclearrow = pool.makeClass("com.cnaude.chairs.VehicleArrow");
		vehiclearrow.setSuperclass(pool.getCtClass(arrowclass));
		vehiclearrow.setInterfaces
		(
			new CtClass[]
			{
				pool.get(Vehicle.class.getName())
			}
		);
		String counstructorsource = "public VehicleArrow("+craftserver.getName()+" server, "+entityarrow.getName()+" entity)\n{\nsuper(server, entity);\n}";
		vehiclearrow.addConstructor
		(
				CtNewConstructor.make(counstructorsource, vehiclearrow)
		);
		String removemethodsource = "public void remove()\n{\nif (this.getPassenger() == null)\n{\nsuper.remove();\n}\n}";
		vehiclearrow.addMethod
		(
			CtNewMethod.make(removemethodsource, vehiclearrow)
		);
		return vehiclearrow.toClass();
	}


	
}
