package com.cnaude.chairs;

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
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

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
    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
    	log = this.getLogger();
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
        protocolManager = ProtocolLibrary.getProtocolManager();
        new PacketListener(protocolManager, this);
    }

    @Override
    public void onDisable() {
    	protocolManager.getAsynchronousManager().unregisterAsyncHandlers(this);
    	protocolManager = null;
        for (String pName : new HashSet<String>(sit.keySet())) {
        	ejectPlayerOnDisable(Bukkit.getPlayerExact(pName));
        }
        if (ignoreList != null) {
            ignoreList.save();
        }
        if (chairEffects != null) {
            chairEffects.cancel();     
        }
        HandlerList.unregisterAll(this);
        log = null;
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
    protected void reSitPlayer(final Player player)
    {
    	player.eject();
    	final Entity prevarrow = sit.get(player.getName());
		Block block = sitblockbr.get(player.getName());
		final Entity arrow = block.getWorld().spawnArrow(block.getLocation().add(0.5, 0, 0.5), new Vector(0, 0.01, 0), 0, 0);
		arrow.setPassenger(player);
		sit.put(player.getName(), arrow);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable()
		{
			public void run()
			{
				prevarrow.remove();
			}
		},100);
    }
    protected void unSitPlayer(final Player player, boolean ignoretp)
    {
    	player.eject();
    	final Location tploc = sitstopteleportloc.get(player.getName());
    	if (tploc != null && !ignoretp)
    	{
    		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
    			public void run()
    			{
    	    		player.teleport(tploc);
    	    		player.setSneaking(false);
    			}
    		},1);
    	}
    	clearSitInfo(player);
    }
    private void ejectPlayerOnDisable(Player player)
    {
    	player.eject();
    	clearSitInfo(player);
    }
    protected void clearSitInfo(Player player) {
    	if (sit.containsKey(player.getName()))
    	{
    		sit.get(player.getName()).remove();
    		sitblock.remove(sitblockbr.get(player.getName()));
    		sitblockbr.remove(player.getName());
    		sitstopteleportloc.remove(player.getName());
    		sit.remove(player.getName());
    		Bukkit.getScheduler().cancelTask(sittask.get(player.getName()));
    		sittask.remove(player.getName());
    		if (notifyplayer && !msgStanding.isEmpty()) {
            	player.sendMessage(msgStanding);
        	}
    	}
    }

    public void loadConfig() {
        autoRotate = getConfig().getBoolean("auto-rotate");
        signCheck = getConfig().getBoolean("sign-check");
        sittingHeightAdj = getConfig().getDouble("sitting-height-adj");
        distance = getConfig().getDouble("distance");
        maxChairWidth = getConfig().getInt("max-chair-width");
        notifyplayer = getConfig().getBoolean("notify-player");
        invertedStairCheck = getConfig().getBoolean("upside-down-check");
        invertedStepCheck = getConfig().getBoolean("upper-step-check");
        ignoreIfBlockInHand = getConfig().getBoolean("ignore-if-item-in-hand");
        
        disabledRegions = new HashSet<String>(getConfig().getStringList("disabledWGRegions"));
        
        sitEffectsEnabled = getConfig().getBoolean("sit-effects.enabled", false);
        sitEffectInterval = getConfig().getInt("sit-effects.interval",20);
        sitMaxHealth = getConfig().getInt("sit-effects.healing.max-percent",100);
        sitHealthPerInterval = getConfig().getInt("sit-effects.healing.amount",1);
        
        msgSitting = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.sitting"));
        msgStanding = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.standing"));
        msgOccupied = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.occupied"));
        msgNoPerm = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.no-permission"));
        msgEnabled = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.enabled"));
        msgDisabled = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.disabled"));
        msgReloaded = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.reloaded"));

        allowedBlocks = new ArrayList<ChairBlock>();
        for (String s : getConfig().getStringList("sit-block-settings")) {
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
        for (String type : getConfig().getStringList("valid-signs")) {            
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
