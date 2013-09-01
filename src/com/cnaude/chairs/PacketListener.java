package com.cnaude.chairs;

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
								pluginInstance.unSit(player);
							}
						}
					}
				}
		}).syncStart();
	}
	
	

        

}
