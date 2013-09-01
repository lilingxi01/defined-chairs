package com.cnaude.chairs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class PacketListener {
	
	private ProtocolManager pm;
	private Chairs pluginInstance;
	public PacketListener(ProtocolManager pm, Chairs plugin)
	{
		this.pm = pm;
		this.pluginInstance = plugin;
		playerDismountListener();
	}
	
	
	private void playerDismountListener()
	{
		pm.getAsynchronousManager().registerAsyncHandler(
				new PacketAdapter(PacketAdapter
						.params(pluginInstance, Packets.Client.PLAYER_INPUT)
						.clientSide()
						.listenerPriority(ListenerPriority.HIGHEST)
						.optionIntercept()
		)
		{
				@Override
				public void onPacketReceiving(final PacketEvent e) 
				{
					if (!e.isCancelled())
					{
						final Player player = e.getPlayer();
						if (e.getPacket().getBooleans().getValues().get(1))
						{
							//hacks to avoid nope error
							final Entity arrow = pluginInstance.sit.get(player.getName());
							if (arrow != null)
							{
								net.minecraft.server.v1_6_R2.EntityArrow nmsarrow = ((CraftArrow) arrow).getHandle();
								nmsarrow.motX = 0;
								nmsarrow.motY = 0;
								nmsarrow.motZ = 0;
								nmsarrow.boundingBox.b = -1;
							}
							//teleport player to correct location
							
							//unsit player
							Bukkit.getScheduler().scheduleSyncDelayedTask(pluginInstance, new Runnable()
							{
								public void run()
								{
									unSit(player);
								}
							},1);
						}
							  
					}
				}
		}).syncStart();
	}
	
	
    private void unSit(Player player) {
    	if (pluginInstance.sit.containsKey(player.getName()))
    	{
    		pluginInstance.sit.get(player.getName()).remove();
    		pluginInstance.sitblock.remove(pluginInstance.sitblockbr.get(player.getName()));
    		pluginInstance.sitblockbr.remove(player.getName());
    		pluginInstance.sit.remove(player.getName());
    		if (pluginInstance.notifyplayer && !pluginInstance.msgStanding.isEmpty()) {
            	player.sendMessage(pluginInstance.msgStanding);
        	}
    	}
    }
    
    
    private Location getTeleportLoc(Player player)
    {
    	Block sittingon = pluginInstance.sitblockbr.get(player.getName());
    	sittingon.getLocation();
    	player.getLocation().getYaw();
    	Location to = player.getLineOfSight(null, 5).get(0).getLocation();
    	return to;
    }
    

}
