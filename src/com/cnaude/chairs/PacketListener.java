package com.cnaude.chairs;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftArrow;
import org.bukkit.entity.Entity;

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
					System.out.println("Checking packet");
					if (!e.isCancelled())
					{
						final String playername = e.getPlayer().getName();
						if (e.getPacket().getBooleans().getValues().get(1))
						{
							e.getAsyncMarker().incrementProcessingDelay();
							Bukkit.getScheduler().scheduleSyncDelayedTask(pluginInstance, new Runnable()
							{
								public void run()
								{
									Entity arrow = pluginInstance.sit.get(playername);
									if (arrow != null)
									{
										net.minecraft.server.v1_6_R2.EntityArrow nmsarrow = ((CraftArrow) arrow).getHandle();
										nmsarrow.motX = 0;
										nmsarrow.motY = 0;
										nmsarrow.motZ = 0;
										nmsarrow.boundingBox.b = -1;
									}
									pm.getAsynchronousManager().signalPacketTransmission(e);
								}
							});
						}
							  
					}
				}
		}).start();
	}

}
