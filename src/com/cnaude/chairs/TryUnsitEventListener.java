package com.cnaude.chairs;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class TryUnsitEventListener implements Listener {
	
    public Chairs plugin;

    public TryUnsitEventListener(Chairs plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
    	Player player = event.getPlayer();
    	if (plugin.getPlayerSitData().isSitting(player))
    	{
    		plugin.getPlayerSitData().unSitPlayer(player, false, true);
    	}
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
    	final Player player = event.getPlayer();
    	if (plugin.getPlayerSitData().isSitting(player))
    	{
    		event.setCancelled(true);
    	}
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
    	Player player = event.getEntity();
    	if (plugin.getPlayerSitData().isSitting(player))
    	{
    		plugin.getPlayerSitData().unSitPlayer(player, false, false);
    	}
    }
    
    private HashSet<String> queueUnsit = new HashSet<String>();
    @EventHandler(priority=EventPriority.LOWEST)
    public void onExitVehicle(VehicleExitEvent e)
    {
    	if (e.getVehicle().getPassenger() instanceof Player)
    	{
    		final Player player = (Player) e.getVehicle().getPassenger();
    		if (plugin.getPlayerSitData().isSitting(player))
    		{
    			e.setCancelled(true);
    			if (!queueUnsit.contains(player.getName()))
    			{
    				queueUnsit.add(player.getName());
    				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
    				{
    					public void run()
    					{
    						queueUnsit.remove(player.getName());
    						plugin.getPlayerSitData().unSitPlayer(player, true, false);
    					}
    				});
    			}
    		}
    	}
    }
    
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event)
    {
    	Block b = event.getBlock();
    	if (plugin.getPlayerSitData().isBlockOccupied(b))
    	{
    		Player player = plugin.getPlayerSitData().getPlayerOnChair(b);
    		plugin.getPlayerSitData().unSitPlayer(player, true, false);
    	}
    }


}
