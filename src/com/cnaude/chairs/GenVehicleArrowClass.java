package com.cnaude.chairs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

public class GenVehicleArrowClass {

	
	public Class<?> genAndLoadClass(String arrowclass, Class<?> entityarrow, Class<?> craftserver) throws IOException, ClassNotFoundException {
		  ClassGen cg = new ClassGen(
				  "VehicleArrow", 
				  arrowclass, 
				  "<generated>", 
				  Constants.ACC_PUBLIC | Constants.ACC_SUPER, 
				  new String[]{"org.bukkit.entity.Vehicle"}
				  );
		  ConstantPoolGen cp = cg.getConstantPool();
		  InstructionList il = new InstructionList();
		  MethodGen  mg = new MethodGen(
				  Constants.ACC_PUBLIC, 
				  Type.VOID, 
				  new Type[] { Type.getType(craftserver), Type.getType(entityarrow) },
                  new String[] { "server", "entity" },
                  "<init>", 
                  "VehicleArrow",
                  il, 
                  cp
                  );
		  InstructionFactory factory = new InstructionFactory(cg);
		  il.append(new ALOAD(0));
		  il.append(new ALOAD(1));
		  il.append(new ALOAD(2));
		  InvokeInstruction ii = factory.createInvoke(
				  arrowclass, 
				  "<init>", 
				  Type.VOID, 
				  new Type[] {Type.getType(craftserver), Type.getType(entityarrow)},  
				  Constants.INVOKESPECIAL
				  );
		  il.append(ii);
		  il.append(InstructionConstants.RETURN);
		  mg.setMaxStack();
		  cg.addMethod(mg.getMethod());
		  il.dispose();
		  cg.getJavaClass().dump("VehicleArrow.class");
		  File arrowfile = new File("VehicleArrow.class");
		  InputStream arrwoinputstrean = new FileInputStream(arrowfile);
		  File jarfile = new File("VehicleArrow.jar");
		  final ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(jarfile));
		  ZipEntry entry = new ZipEntry(arrowfile.getName());
		  zipout.putNextEntry(entry);
	      byte[] buffer = new byte[1024];
	      int bytesRead;
          while ((bytesRead = arrwoinputstrean.read(buffer)) != -1) {
        	  zipout.write(buffer, 0, bytesRead);
          }
          zipout.closeEntry();
          arrwoinputstrean.close();
          zipout.close();
		  URL url = jarfile.toURI().toURL();
		  URL[] urls = new URL[]{url};
		  URLClassLoader cl = new URLClassLoader(urls);
		  Class<?> vehiclearrowclass = cl.loadClass("VehicleArrow");
		  cl.close();
		  arrowfile.delete();
		  jarfile.delete();
		  return vehiclearrowclass;
	}


	
}
