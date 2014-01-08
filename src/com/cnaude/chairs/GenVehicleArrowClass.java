package com.cnaude.chairs;

import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Vehicle;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewConstructor;
import javassist.NotFoundException;

public class GenVehicleArrowClass {

	public Class<?> genAndLoadClass(String arrowclass, Class<?> entityarrow, Class<?> craftserver) throws IOException, ClassNotFoundException, NotFoundException, CannotCompileException 
	{
		ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath
		(
				new ClassClassPath(Bukkit.class)
		);
		CtClass cc = pool.makeClass("com.cnaude.chairs.VehicleArrow");
		cc.setSuperclass(pool.getCtClass(arrowclass));
		cc.setInterfaces
		(
			new CtClass[]
			{
				pool.get(Vehicle.class.getName())
			}
		);
		String counstructorsource = "public VehicleArrow("+craftserver.getName()+" server, "+entityarrow.getName()+" entity)\n{\nsuper(server, entity);\n}";
		cc.addConstructor
		(
				CtNewConstructor.make(counstructorsource, cc)
		);
		return cc.toClass();
	}


	
}
