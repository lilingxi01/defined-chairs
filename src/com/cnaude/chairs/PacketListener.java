package com.cnaude.chairs;

import org.bukkit.Location;
import org.bukkit.block.Block;
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
							//just eject player if he is sitting on chair
							if (pluginInstance.sit.containsKey(player.getName()))
							{
								player.eject();
								unSit(player);
							}
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
