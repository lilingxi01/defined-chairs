package com.cnaude.chairs;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Chairs extends JavaPlugin {
    public ChairEffects chairEffects;
    public List<ChairBlock> allowedBlocks;
    public List<Material> validSigns;
    public boolean autoRotate, signCheck, notifyplayer;
    public boolean invertedStairCheck, invertedStepCheck, ignoreIfBlockInHand;
    public boolean sitEffectsEnabled;
    public double sittingHeightAdj, distance;
    public int maxChairWidth;
    public int sitMaxHealth;
    public int sitHealthPerInterval;
    public int sitEffectInterval;
    public HashSet<String> disabledRegions = new HashSet<String>();
    private Logger log;
    public PluginManager pm;
    public ChairsIgnoreList ignoreList; 
    public String msgSitting, msgStanding, msgOccupied, msgNoPerm, msgReloaded, msgDisabled, msgEnabled;
    
    private Class<?> vehiclearrowclass;

    @Override
    public void onEnable() {
    	log = this.getLogger();
		try {
	    	World world = getServer().getWorlds().get(0);
	    	Arrow arrow = world.spawnArrow(new Location(world, 0, 0, 0), new Vector(0, 0, 0), 0, 0);
	    	String arrowclass = arrow.getClass().getName();
	    	Method getHandle;
			getHandle = arrow.getClass().getDeclaredMethod("getHandle");
	    	getHandle.setAccessible(true);
	    	Class<?> entityarrow = getHandle.invoke(arrow).getClass();
	    	Class<?> craftserver = getServer().getClass();
	    	vehiclearrowclass = new GenVehicleArrowClass(this).genAndLoadClass(arrowclass, entityarrow, craftserver);
		} catch (Exception e) {
			e.printStackTrace();
			log.severe("Failed to generate VehicleArrow class, exiting");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
        ignoreList = new ChairsIgnoreList(this);
        ignoreList.load();
        pm = this.getServer().getPluginManager();
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(new EventListener(this, ignoreList), this);
        getCommand("chairs").setExecutor(new ChairsCommand(this, ignoreList));
        if (sitEffectsEnabled) {
            logInfo("Enabling sitting effects.");
            chairEffects = new ChairEffects(this);
        }
        
    }

    @Override
    public void onDisable() {
    	for (Player player : getServer().getOnlinePlayers()) { 
    		if (sit.containsKey(player.getName())) {
    			unSitPlayer(player, true);
    		}
    	}
        if (ignoreList != null) {
            ignoreList.save();
        }
        if (chairEffects != null) {
            chairEffects.cancel();     
        }
        log = null;
        vehiclearrowclass = null;
    }
    
    public void restartEffectsTask() {
        if (chairEffects != null) {
            chairEffects.restart();
        }
    }
    
    protected HashMap<String, Entity> sit = new HashMap<String, Entity>();
    protected HashMap<Block, String> sitblock = new HashMap<Block, String>();
    protected HashMap<String, Block> sitblockbr = new HashMap<String, Block>();
    protected HashMap<String, Location> sitstopteleportloc = new HashMap<String, Location>();
    protected HashMap<String, Integer> sittask = new HashMap<String, Integer>();
    protected void sitPlayer(Player player, Location sitlocation)
    {
    	try {
    		if (notifyplayer && !msgSitting.isEmpty()) 
    		{
	            player.sendMessage(msgSitting);
	        }
	        Block block = sitlocation.getBlock();
	        sitstopteleportloc.put(player.getName(), player.getLocation());
	        player.teleport(sitlocation);
	        player.setSneaking(false);
	        Location arrowloc = block.getLocation().add(0.5, 0 , 0.5);
	        Entity arrow = player.getWorld().spawnArrow(arrowloc, new Vector(0, 0.1 ,0), 0, 0);
	        Method getHandleMethod = arrow.getClass().getDeclaredMethod("getHandle");
	        getHandleMethod.setAccessible(true);
	        Object nmsarrow = getHandleMethod.invoke(arrow);
	        Field bukkitEntityField = nmsarrow.getClass().getSuperclass().getDeclaredField("bukkitEntity");
	        bukkitEntityField.setAccessible(true);
	        Constructor<?> ctor = vehiclearrowclass.getDeclaredConstructor(this.getServer().getClass(), nmsarrow.getClass());
	        ctor.setAccessible(true);
	        Object vehiclearrow = ctor.newInstance(this.getServer(), nmsarrow);
	        bukkitEntityField.set(nmsarrow, vehiclearrow);
	        arrow.setPassenger(player);
	        sit.put(player.getName(), (Entity) vehiclearrow);
	        sitblock.put(block, player.getName());
	        sitblockbr.put(player.getName(), block);
	        startReSitTask(player);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    protected void startReSitTask(final Player player)
    {
    	int task = 
    	Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
    	{
    		public void run()
    		{
    			reSitPlayer(player);
    		}    	
    	},1000,1000);
    	sittask.put(player.getName(), task);
    }
    protected void reSitPlayer(final Player player)
    {
    	try {
	    	final Entity prevarrow = sit.get(player.getName());
	    	sit.remove(player.getName());
	    	player.eject();
			Block block = sitblockbr.get(player.getName());
	        Location arrowloc = block.getLocation().add(0.5, 0 , 0.5);
	        Entity arrow = player.getWorld().spawnArrow(arrowloc, new Vector(0, 0.1 ,0), 0, 0);
	        Method getHandleMethod = arrow.getClass().getDeclaredMethod("getHandle");
	        getHandleMethod.setAccessible(true);
	        Object nmsarrow = getHandleMethod.invoke(arrow);
	        Field bukkitEntityField = nmsarrow.getClass().getSuperclass().getDeclaredField("bukkitEntity");
	        bukkitEntityField.setAccessible(true);
	        Constructor<?> ctor = vehiclearrowclass.getDeclaredConstructor(this.getServer().getClass(), nmsarrow.getClass());
	        ctor.setAccessible(true);
	        Object vehiclearrow = ctor.newInstance(this.getServer(), nmsarrow);
	        bukkitEntityField.set(nmsarrow, vehiclearrow);
	        arrow.setPassenger(player);
			sit.put(player.getName(), arrow);
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable()
			{
				public void run()
				{
					prevarrow.remove();
				}
			},100);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    protected void unSitPlayer(final Player player, boolean ignoretp) 
    {
    	final Entity arrow = sit.get(player.getName());
		sit.remove(player.getName());
    	player.eject();
    	player.eject();
    	arrow.remove();
    	final Location tploc = sitstopteleportloc.get(player.getName());
    	if (tploc != null && !ignoretp) 
    	{
    		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() 
    		{
    			public void run() 
    			{
    	    		player.teleport(tploc);
    	    		player.setSneaking(false);
    			}
    		},1);
    	}
		sitblock.remove(sitblockbr.get(player.getName()));
		sitblockbr.remove(player.getName());
		sitstopteleportloc.remove(player.getName());
		Bukkit.getScheduler().cancelTask(sittask.get(player.getName()));
		sittask.remove(player.getName());
		if (notifyplayer && !msgStanding.isEmpty()) 
		{
        	player.sendMessage(msgStanding);
    	}
    }
    
    public void loadConfig() {
    	FileConfiguration config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(),"config.yml"));
        autoRotate = config.getBoolean("auto-rotate");
        signCheck = config.getBoolean("sign-check");
        sittingHeightAdj = config.getDouble("sitting-height-adj");
        distance = config.getDouble("distance");
        maxChairWidth = config.getInt("max-chair-width");
        notifyplayer = config.getBoolean("notify-player");
        invertedStairCheck = config.getBoolean("upside-down-check");
        invertedStepCheck = config.getBoolean("upper-step-check");
        ignoreIfBlockInHand = config.getBoolean("ignore-if-item-in-hand");
        
        disabledRegions = new HashSet<String>(config.getStringList("disabledWGRegions"));
        
        sitEffectsEnabled = config.getBoolean("sit-effects.enabled", false);
        sitEffectInterval = config.getInt("sit-effects.interval",20);
        sitMaxHealth = config.getInt("sit-effects.healing.max-percent",100);
        sitHealthPerInterval = config.getInt("sit-effects.healing.amount",1);
        
        msgSitting = ChatColor.translateAlternateColorCodes('&',config.getString("messages.sitting"));
        msgStanding = ChatColor.translateAlternateColorCodes('&',config.getString("messages.standing"));
        msgOccupied = ChatColor.translateAlternateColorCodes('&',config.getString("messages.occupied"));
        msgNoPerm = ChatColor.translateAlternateColorCodes('&',config.getString("messages.no-permission"));
        msgEnabled = ChatColor.translateAlternateColorCodes('&',config.getString("messages.enabled"));
        msgDisabled = ChatColor.translateAlternateColorCodes('&',config.getString("messages.disabled"));
        msgReloaded = ChatColor.translateAlternateColorCodes('&',config.getString("messages.reloaded"));

        allowedBlocks = new ArrayList<ChairBlock>();
        for (String s : config.getStringList("sit-block-settings")) {
            String type;
            double sh = 0.7;
            String tmp[] = s.split("[:]");
            type = tmp[0];         
            if (tmp.length == 2) {
               sh = Double.parseDouble(tmp[1]);
            }
            try {                
                Material mat = Material.matchMaterial(type);
                if (mat != null) {                    
                    logInfo("Allowed block: " + mat.toString() + " => " + sh);
                    allowedBlocks.add(new ChairBlock(mat,sh));
                } else {
                    logError("Invalid block: " + type);
                }
            }
            catch (Exception e) {
                logError(e.getMessage());
            }
        }
        
        validSigns = new ArrayList<Material>();    
        for (String type : config.getStringList("valid-signs")) {            
            try {
            	validSigns.add(Material.matchMaterial(type));
            }
            catch (Exception e) {
                logError(e.getMessage());
            }
        }
    } 
    
    public void logInfo(String _message) {
        log.log(Level.INFO, _message);
    }

    public void logError(String _message) {
        log.log(Level.SEVERE, _message);
    }
     
}
